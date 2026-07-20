package com.studyflow.service;

import com.studyflow.api.dto.LoginChangeCompleteRequest;
import com.studyflow.api.dto.LoginChangeCreateRequest;
import com.studyflow.api.dto.LoginChangeResponse;
import com.studyflow.domain.LoginChangeRequest;
import com.studyflow.domain.LoginChangeStatus;
import com.studyflow.domain.LoginChangeType;
import com.studyflow.domain.ProfileType;
import com.studyflow.domain.User;
import com.studyflow.domain.validation.CpfValidator;
import com.studyflow.domain.validation.EmailValidator;
import com.studyflow.repository.LoginChangeRequestRepository;
import com.studyflow.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class LoginChangeService {

    @Inject
    LoginChangeRequestRepository loginChangeRequestRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    KeycloakAdminFacade keycloakAdminFacade;

    public List<LoginChangeResponse> listForAdmin() {
        return loginChangeRequestRepository.findByStatus(LoginChangeStatus.pending).stream()
                .map(LoginChangeResponse::from)
                .toList();
    }

    public List<LoginChangeResponse> listForRequester(UUID requesterUserId) {
        return loginChangeRequestRepository.findByRequester(requesterUserId).stream()
                .map(LoginChangeResponse::from)
                .toList();
    }

    @Transactional
    public LoginChangeResponse create(UUID requesterUserId, LoginChangeCreateRequest request) {
        User requester = requireActiveEstudante(requesterUserId);
        validateCreate(request);

        Instant now = Instant.now();
        LoginChangeRequest entity = new LoginChangeRequest();
        entity.id = UUID.randomUUID();
        entity.requesterUserId = requester.id;
        entity.changeType = request.changeType();
        entity.status = LoginChangeStatus.pending;
        entity.proposedEmail = requiresEmail(request.changeType()) ? request.proposedEmail().trim() : null;
        entity.createdAt = now;
        entity.updatedAt = now;

        loginChangeRequestRepository.persist(entity);
        return LoginChangeResponse.from(entity);
    }

    @Transactional
    public LoginChangeResponse approve(UUID requestId, UUID adminUserId) {
        LoginChangeRequest entity = findRequest(requestId);
        if (entity.status != LoginChangeStatus.pending) {
            throw new BusinessException(409, "Solicitacao nao esta pendente");
        }

        entity.status = LoginChangeStatus.approved;
        entity.reviewedByUserId = adminUserId;
        entity.reviewedAt = Instant.now();
        entity.updatedAt = Instant.now();
        return LoginChangeResponse.from(entity);
    }

    @Transactional
    public LoginChangeResponse reject(UUID requestId, UUID adminUserId, String rejectionReason) {
        LoginChangeRequest entity = findRequest(requestId);
        if (entity.status != LoginChangeStatus.pending) {
            throw new BusinessException(409, "Solicitacao nao esta pendente");
        }

        entity.status = LoginChangeStatus.rejected;
        entity.reviewedByUserId = adminUserId;
        entity.reviewedAt = Instant.now();
        entity.rejectionReason = rejectionReason;
        entity.updatedAt = Instant.now();
        return LoginChangeResponse.from(entity);
    }

    @Transactional
    public LoginChangeResponse complete(UUID requestId, UUID requesterUserId, LoginChangeCompleteRequest request) {
        LoginChangeRequest entity = findRequest(requestId);
        if (!entity.requesterUserId.equals(requesterUserId)) {
            throw new BusinessException(403, "Nao permitido concluir esta solicitacao");
        }
        if (entity.status != LoginChangeStatus.approved) {
            throw new BusinessException(409, "Solicitacao nao esta aprovada");
        }

        User user = requireActiveEstudante(requesterUserId);
        validateComplete(entity, request);

        String normalizedCpf = CpfValidator.normalize(request.cpf());
        if (!user.cpf.equals(normalizedCpf)) {
            throw new BusinessException(400, "CPF nao confere");
        }

        String previousEmail = user.email;
        try {
            applyLoginChange(entity, user, request);
            entity.status = LoginChangeStatus.completed;
            entity.completedAt = Instant.now();
            entity.updatedAt = Instant.now();
            user.updatedAt = Instant.now();
            return LoginChangeResponse.from(entity);
        } catch (BusinessException e) {
            compensateEmail(user, previousEmail);
            throw e;
        } catch (RuntimeException e) {
            compensateEmail(user, previousEmail);
            throw new BusinessException(409, "Falha ao aplicar alteracao de login");
        }
    }

    private void applyLoginChange(LoginChangeRequest entity, User user, LoginChangeCompleteRequest request) {
        switch (entity.changeType) {
            case email -> {
                String newEmail = request.newEmail().trim();
                ensureEmailAvailable(newEmail, user.id);
                keycloakAdminFacade.updateEmail(user.keycloakId, newEmail);
                user.email = newEmail;
            }
            case password -> keycloakAdminFacade.updatePassword(user.keycloakId, request.newPassword());
            case emailAndPassword -> {
                String newEmail = request.newEmail().trim();
                ensureEmailAvailable(newEmail, user.id);
                keycloakAdminFacade.updateEmail(user.keycloakId, newEmail);
                try {
                    keycloakAdminFacade.updatePassword(user.keycloakId, request.newPassword());
                } catch (RuntimeException e) {
                    keycloakAdminFacade.updateEmail(user.keycloakId, user.email);
                    throw e;
                }
                user.email = newEmail;
            }
        }
    }

    private void ensureEmailAvailable(String newEmail, UUID userId) {
        userRepository.findActiveByEmail(newEmail).ifPresent(existing -> {
            if (!existing.id.equals(userId)) {
                throw new BusinessException(409, "E-mail ja cadastrado");
            }
        });
    }

    private void compensateEmail(User user, String previousEmail) {
        if (previousEmail == null || previousEmail.equals(user.email)) {
            return;
        }
        try {
            keycloakAdminFacade.updateEmail(user.keycloakId, previousEmail);
            user.email = previousEmail;
        } catch (RuntimeException ignored) {
            // best-effort
        }
    }

    private User requireActiveEstudante(UUID userId) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new BusinessException(404, "Usuario nao encontrado"));
        if (user.isDisabled()) {
            throw new BusinessException(403, "Usuario desabilitado");
        }
        if (user.profile != ProfileType.estudante) {
            throw new BusinessException(403, "Apenas estudantes podem solicitar alteracao de login");
        }
        return user;
    }

    private LoginChangeRequest findRequest(UUID requestId) {
        return loginChangeRequestRepository.findByIdOptional(requestId)
                .orElseThrow(() -> new BusinessException(404, "Solicitacao nao encontrada"));
    }

    private void validateCreate(LoginChangeCreateRequest request) {
        if (request.changeType() == null) {
            throw new BusinessException(400, "Tipo de alteracao e obrigatorio");
        }
        if (requiresEmail(request.changeType())) {
            if (request.proposedEmail() == null || !EmailValidator.isValid(request.proposedEmail())) {
                throw new BusinessException(400, "E-mail proposto e obrigatorio para este tipo");
            }
        }
    }

    private void validateComplete(LoginChangeRequest entity, LoginChangeCompleteRequest request) {
        if (!CpfValidator.isValid(request.cpf())) {
            throw new BusinessException(400, "CPF invalido");
        }
        switch (entity.changeType) {
            case email -> {
                if (request.newEmail() == null || !EmailValidator.isValid(request.newEmail())) {
                    throw new BusinessException(400, "Novo e-mail e obrigatorio");
                }
                requireMatchingProposedEmail(entity.proposedEmail, request.newEmail());
            }
            case password -> {
                if (request.newPassword() == null || request.newPassword().isBlank()) {
                    throw new BusinessException(400, "Nova senha e obrigatoria");
                }
            }
            case emailAndPassword -> {
                if (request.newEmail() == null || !EmailValidator.isValid(request.newEmail())) {
                    throw new BusinessException(400, "Novo e-mail e obrigatorio");
                }
                if (request.newPassword() == null || request.newPassword().isBlank()) {
                    throw new BusinessException(400, "Nova senha e obrigatoria");
                }
                requireMatchingProposedEmail(entity.proposedEmail, request.newEmail());
            }
        }
    }

    private void requireMatchingProposedEmail(String proposedEmail, String newEmail) {
        if (proposedEmail == null || !proposedEmail.equalsIgnoreCase(newEmail.trim())) {
            throw new BusinessException(400, "Novo e-mail deve coincidir com o e-mail aprovado");
        }
    }

    private boolean requiresEmail(LoginChangeType changeType) {
        return changeType == LoginChangeType.email || changeType == LoginChangeType.emailAndPassword;
    }
}
