package com.studyflow.service;

import com.studyflow.api.dto.AdminCreateUserRequest;
import com.studyflow.api.dto.AdminUpdateUserRequest;
import com.studyflow.api.dto.UserResponse;
import com.studyflow.domain.User;
import com.studyflow.domain.validation.CpfValidator;
import com.studyflow.domain.validation.EmailValidator;
import com.studyflow.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class UserAdminService {

    @Inject
    UserRepository userRepository;

    @Inject
    KeycloakAdminFacade keycloakAdminFacade;

    public List<UserResponse> list(boolean includeDisabled) {
        List<User> users = includeDisabled ? userRepository.listAllOrdered() : userRepository.listActive();
        return users.stream().map(user -> UserResponse.from(user, true)).toList();
    }

    public UserResponse getById(UUID userId) {
        return UserResponse.from(findAnyUser(userId), true);
    }

    @Transactional
    public UserResponse create(AdminCreateUserRequest request) {
        validateCreate(request);
        String normalizedCpf = CpfValidator.normalize(request.cpf());
        String email = request.email().trim();

        userRepository.findActiveByEmail(email).ifPresent(u -> {
            throw new BusinessException(409, "E-mail ja cadastrado");
        });
        userRepository.findActiveByCpf(normalizedCpf).ifPresent(u -> {
            throw new BusinessException(409, "CPF ja cadastrado");
        });

        Map<String, String> attributes = new HashMap<>();
        attributes.put("cpf", normalizedCpf);
        attributes.put("birthDate", request.birthDate().toString());
        attributes.put("educationLevel", request.educationLevel().name());
        attributes.put("profile", request.profile().name());

        String keycloakId = keycloakAdminFacade.createUser(
                email,
                request.password(),
                request.name().trim(),
                request.profile(),
                attributes);

        try {
            Instant now = Instant.now();
            User user = new User();
            user.id = UUID.randomUUID();
            user.keycloakId = keycloakId;
            user.name = request.name().trim();
            user.email = email;
            user.cpf = normalizedCpf;
            user.birthDate = request.birthDate();
            user.educationLevel = request.educationLevel();
            user.profile = request.profile();
            user.createdAt = now;
            user.updatedAt = now;
            userRepository.persist(user);
            return UserResponse.from(user);
        } catch (RuntimeException e) {
            compensateKeycloakDisable(keycloakId);
            throw new BusinessException(409, "Falha ao persistir usuario na base local");
        }
    }

    @Transactional
    public UserResponse update(UUID userId, AdminUpdateUserRequest request) {
        User user = findAnyUser(userId);

        if (request.name() != null && !request.name().isBlank()) {
            user.name = request.name().trim();
        }
        if (request.cpf() != null && !request.cpf().isBlank()) {
            if (!CpfValidator.isValid(request.cpf())) {
                throw new BusinessException(400, "CPF invalido");
            }
            String normalizedCpf = CpfValidator.normalize(request.cpf());
            userRepository.findActiveByCpf(normalizedCpf).ifPresent(existing -> {
                if (!existing.id.equals(userId)) {
                    throw new BusinessException(409, "CPF ja cadastrado");
                }
            });
            user.cpf = normalizedCpf;
        }
        if (request.birthDate() != null) {
            user.birthDate = request.birthDate();
        }
        if (request.educationLevel() != null) {
            user.educationLevel = request.educationLevel();
        }
        if (request.profile() != null) {
            user.profile = request.profile();
        }

        try {
            keycloakAdminFacade.updateProfile(
                    user.keycloakId,
                    user.name,
                    user.cpf,
                    user.birthDate,
                    user.educationLevel,
                    user.profile);
        } catch (BusinessException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new BusinessException(409, "Falha ao sincronizar perfil no Keycloak");
        }

        user.updatedAt = Instant.now();
        return UserResponse.from(user);
    }

    @Transactional
    public void disable(UUID targetUserId, UUID adminUserId) {
        if (targetUserId.equals(adminUserId)) {
            throw new BusinessException(400, "Nao e permitido desabilitar a propria conta");
        }

        User user = findActiveUser(targetUserId);
        boolean keycloakDisabled = false;
        try {
            keycloakAdminFacade.setEnabled(user.keycloakId, false);
            keycloakDisabled = true;
            user.deletedAt = Instant.now();
            user.updatedAt = Instant.now();
        } catch (BusinessException e) {
            if (keycloakDisabled) {
                compensateKeycloakEnable(user.keycloakId);
            }
            throw e;
        } catch (RuntimeException e) {
            if (keycloakDisabled) {
                compensateKeycloakEnable(user.keycloakId);
            }
            throw new BusinessException(409, "Falha ao desabilitar usuario");
        }
    }

    @Transactional
    public void enable(UUID userId) {
        User user = findAnyUser(userId);
        if (!user.isDisabled()) {
            return;
        }

        userRepository.findActiveByEmail(user.email).ifPresent(existing -> {
            if (!existing.id.equals(userId)) {
                throw new BusinessException(409, "E-mail ja em uso por outro usuario ativo");
            }
        });
        userRepository.findActiveByCpf(user.cpf).ifPresent(existing -> {
            if (!existing.id.equals(userId)) {
                throw new BusinessException(409, "CPF ja em uso por outro usuario ativo");
            }
        });

        boolean keycloakEnabled = false;
        try {
            keycloakAdminFacade.setEnabled(user.keycloakId, true);
            keycloakEnabled = true;
            user.deletedAt = null;
            user.updatedAt = Instant.now();
        } catch (BusinessException e) {
            if (keycloakEnabled) {
                compensateKeycloakDisable(user.keycloakId);
            }
            throw e;
        } catch (RuntimeException e) {
            if (keycloakEnabled) {
                compensateKeycloakDisable(user.keycloakId);
            }
            throw new BusinessException(409, "Falha ao reabilitar usuario");
        }
    }

    private void validateCreate(AdminCreateUserRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BusinessException(400, "Nome e obrigatorio");
        }
        if (!EmailValidator.isValid(request.email())) {
            throw new BusinessException(400, "E-mail invalido");
        }
        if (!CpfValidator.isValid(request.cpf())) {
            throw new BusinessException(400, "CPF invalido");
        }
        if (request.birthDate() == null) {
            throw new BusinessException(400, "Data de nascimento e obrigatoria");
        }
        if (request.educationLevel() == null) {
            throw new BusinessException(400, "Formacao e obrigatoria");
        }
        if (request.profile() == null) {
            throw new BusinessException(400, "Perfil e obrigatorio");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new BusinessException(400, "Senha e obrigatoria");
        }
    }

    private User findAnyUser(UUID userId) {
        return userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new BusinessException(404, "Usuario nao encontrado"));
    }

    private User findActiveUser(UUID userId) {
        User user = findAnyUser(userId);
        if (user.isDisabled()) {
            throw new BusinessException(404, "Usuario nao encontrado");
        }
        return user;
    }

    private void compensateKeycloakDisable(String keycloakId) {
        try {
            keycloakAdminFacade.setEnabled(keycloakId, false);
        } catch (RuntimeException ignored) {
            // best-effort
        }
    }

    private void compensateKeycloakEnable(String keycloakId) {
        try {
            keycloakAdminFacade.setEnabled(keycloakId, true);
        } catch (RuntimeException ignored) {
            // best-effort
        }
    }
}
