package com.studyflow.service;

import com.studyflow.api.dto.UserBootstrapRequest;
import com.studyflow.api.dto.UserResponse;
import com.studyflow.domain.ProfileType;
import com.studyflow.domain.User;
import com.studyflow.domain.validation.CpfValidator;
import com.studyflow.domain.validation.EmailValidator;
import com.studyflow.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class UserBootstrapService {

    @Inject
    UserRepository userRepository;

    @Transactional
    public BootstrapResult bootstrap(String keycloakId, String email, UserBootstrapRequest request) {
        var existing = userRepository.findByKeycloakId(keycloakId);
        if (existing.isPresent()) {
            User user = existing.get();
            if (user.isDisabled()) {
                throw new BusinessException(403, "User is disabled");
            }
            return new BootstrapResult(false, UserResponse.from(user));
        }

        validate(request, email);
        String normalizedCpf = CpfValidator.normalize(request.cpf());

        userRepository.findActiveByEmail(email).ifPresent(u -> {
            throw new BusinessException(409, "Email already registered");
        });
        userRepository.findActiveByCpf(normalizedCpf).ifPresent(u -> {
            throw new BusinessException(409, "CPF already registered");
        });

        Instant now = Instant.now();
        User user = new User();
        user.id = UUID.randomUUID();
        user.keycloakId = keycloakId;
        user.name = request.name().trim();
        user.email = email;
        user.cpf = normalizedCpf;
        user.birthDate = request.birthDate();
        user.educationLevel = request.educationLevel();
        user.profile = ProfileType.estudante;
        user.createdAt = now;
        user.updatedAt = now;

        userRepository.persist(user);
        return new BootstrapResult(true, UserResponse.from(user));
    }

    private void validate(UserBootstrapRequest request, String email) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BusinessException(400, "Name is required");
        }
        if (!EmailValidator.isValid(email)) {
            throw new BusinessException(400, "Invalid email");
        }
        if (!CpfValidator.isValid(request.cpf())) {
            throw new BusinessException(400, "Invalid CPF");
        }
        if (request.birthDate() == null) {
            throw new BusinessException(400, "Birth date is required");
        }
        if (request.educationLevel() == null) {
            throw new BusinessException(400, "Education level is required");
        }
    }
}
