package com.studyflow.service;

import com.studyflow.api.dto.LoginChangeCompleteRequest;
import com.studyflow.domain.EducationLevel;
import com.studyflow.domain.LoginChangeRequest;
import com.studyflow.domain.LoginChangeStatus;
import com.studyflow.domain.LoginChangeType;
import com.studyflow.domain.ProfileType;
import com.studyflow.domain.User;
import com.studyflow.repository.LoginChangeRequestRepository;
import com.studyflow.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginChangeServiceKeycloakTest {

    @Mock
    LoginChangeRequestRepository loginChangeRequestRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    KeycloakAdminFacade keycloakAdminFacade;

    @InjectMocks
    LoginChangeService loginChangeService;

    UUID requesterId;
    User estudante;

    @BeforeEach
    void setUp() {
        requesterId = UUID.randomUUID();
        estudante = estudante(requesterId);
    }

    @Test
    void completeRejectsEmailMismatchWithApprovedProposal() {
        LoginChangeRequest entity = approved(LoginChangeType.email, "approved@example.com");
        when(loginChangeRequestRepository.findByIdOptional(entity.id)).thenReturn(Optional.of(entity));
        when(userRepository.findByIdOptional(requesterId)).thenReturn(Optional.of(estudante));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> loginChangeService.complete(
                        entity.id,
                        requesterId,
                        new LoginChangeCompleteRequest("529.982.247-25", "other@example.com", null)));

        assertEquals(400, ex.getStatus());
        assertEquals("Novo e-mail deve coincidir com o e-mail aprovado", ex.getMessage());
        verify(keycloakAdminFacade, never()).updateEmail(estudante.keycloakId, "other@example.com");
    }

    @Test
    void completeEmailAndPasswordRollsBackEmailWhenPasswordFails() {
        LoginChangeRequest entity = approved(LoginChangeType.emailAndPassword, "new@example.com");
        when(loginChangeRequestRepository.findByIdOptional(entity.id)).thenReturn(Optional.of(entity));
        when(userRepository.findByIdOptional(requesterId)).thenReturn(Optional.of(estudante));
        when(userRepository.findActiveByEmail("new@example.com")).thenReturn(Optional.empty());
        doThrow(new RuntimeException("password sync failed"))
                .when(keycloakAdminFacade)
                .updatePassword(estudante.keycloakId, "NewPass123!");

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> loginChangeService.complete(
                        entity.id,
                        requesterId,
                        new LoginChangeCompleteRequest("529.982.247-25", "new@example.com", "NewPass123!")));

        assertEquals(409, ex.getStatus());
        verify(keycloakAdminFacade).updateEmail(estudante.keycloakId, "new@example.com");
        verify(keycloakAdminFacade).updateEmail(estudante.keycloakId, "student@example.com");
        assertEquals("student@example.com", estudante.email);
        assertEquals(LoginChangeStatus.approved, entity.status);
    }

    private User estudante(UUID id) {
        User user = new User();
        user.id = id;
        user.keycloakId = "kc-" + id;
        user.name = "Student";
        user.email = "student@example.com";
        user.cpf = "52998224725";
        user.birthDate = LocalDate.of(2000, 1, 1);
        user.educationLevel = EducationLevel.graduacao;
        user.profile = ProfileType.estudante;
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        return user;
    }

    private LoginChangeRequest approved(LoginChangeType type, String proposedEmail) {
        LoginChangeRequest request = new LoginChangeRequest();
        request.id = UUID.randomUUID();
        request.requesterUserId = requesterId;
        request.changeType = type;
        request.status = LoginChangeStatus.approved;
        request.proposedEmail = proposedEmail;
        request.createdAt = Instant.now();
        request.updatedAt = Instant.now();
        request.reviewedAt = Instant.now();
        request.reviewedByUserId = UUID.randomUUID();
        return request;
    }
}
