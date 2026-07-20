package com.studyflow.service;

import com.studyflow.api.dto.LoginChangeCompleteRequest;
import com.studyflow.api.dto.LoginChangeCreateRequest;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginChangeServiceTest {

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
    void createPersistsPendingRequest() {
        when(userRepository.findByIdOptional(requesterId)).thenReturn(Optional.of(estudante));

        var response = loginChangeService.create(
                requesterId,
                new LoginChangeCreateRequest(LoginChangeType.email, "new@example.com"));

        assertEquals(LoginChangeStatus.pending, response.status());
        assertEquals("new@example.com", response.proposedEmail());

        ArgumentCaptor<LoginChangeRequest> captor = ArgumentCaptor.forClass(LoginChangeRequest.class);
        verify(loginChangeRequestRepository).persist(captor.capture());
        assertEquals(LoginChangeType.email, captor.getValue().changeType);
    }

    @Test
    void approveMovesPendingToApproved() {
        LoginChangeRequest entity = pendingRequest();
        when(loginChangeRequestRepository.findByIdOptional(entity.id)).thenReturn(Optional.of(entity));

        UUID adminId = UUID.randomUUID();
        var response = loginChangeService.approve(entity.id, adminId);

        assertEquals(LoginChangeStatus.approved, response.status());
        assertEquals(adminId, response.reviewedByUserId());
    }

    @Test
    void completeAppliesEmailAfterCpfMatch() {
        LoginChangeRequest entity = approvedRequest(LoginChangeType.email);
        when(loginChangeRequestRepository.findByIdOptional(entity.id)).thenReturn(Optional.of(entity));
        when(userRepository.findByIdOptional(requesterId)).thenReturn(Optional.of(estudante));

        var response = loginChangeService.complete(
                entity.id,
                requesterId,
                new LoginChangeCompleteRequest("529.982.247-25", "new@example.com", null));

        assertEquals(LoginChangeStatus.completed, response.status());
        verify(keycloakAdminFacade).updateEmail(estudante.keycloakId, "new@example.com");
        assertEquals("new@example.com", estudante.email);
    }

    @Test
    void completeRejectsWrongCpf() {
        LoginChangeRequest entity = approvedRequest(LoginChangeType.password);
        when(loginChangeRequestRepository.findByIdOptional(entity.id)).thenReturn(Optional.of(entity));
        when(userRepository.findByIdOptional(requesterId)).thenReturn(Optional.of(estudante));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> loginChangeService.complete(
                        entity.id,
                        requesterId,
                        new LoginChangeCompleteRequest("390.533.447-05", null, "NewPass123!")));

        assertEquals(400, ex.getStatus());
        verify(keycloakAdminFacade, never()).updatePassword(any(), any());
    }

    @Test
    void completeRejectsWhenNotApproved() {
        LoginChangeRequest entity = pendingRequest();
        when(loginChangeRequestRepository.findByIdOptional(entity.id)).thenReturn(Optional.of(entity));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> loginChangeService.complete(
                        entity.id,
                        requesterId,
                        new LoginChangeCompleteRequest("529.982.247-25", "new@example.com", null)));

        assertEquals(409, ex.getStatus());
    }

    @Test
    void createRejectsAdminProfile() {
        User admin = estudante(requesterId);
        admin.profile = ProfileType.admin;
        when(userRepository.findByIdOptional(requesterId)).thenReturn(Optional.of(admin));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> loginChangeService.create(
                        requesterId,
                        new LoginChangeCreateRequest(LoginChangeType.password, null)));

        assertEquals(403, ex.getStatus());
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

    private LoginChangeRequest pendingRequest() {
        LoginChangeRequest request = new LoginChangeRequest();
        request.id = UUID.randomUUID();
        request.requesterUserId = requesterId;
        request.changeType = LoginChangeType.email;
        request.status = LoginChangeStatus.pending;
        request.proposedEmail = "new@example.com";
        request.createdAt = Instant.now();
        request.updatedAt = Instant.now();
        return request;
    }

    private LoginChangeRequest approvedRequest(LoginChangeType type) {
        LoginChangeRequest request = pendingRequest();
        request.changeType = type;
        request.status = LoginChangeStatus.approved;
        request.reviewedAt = Instant.now();
        request.reviewedByUserId = UUID.randomUUID();
        return request;
    }
}
