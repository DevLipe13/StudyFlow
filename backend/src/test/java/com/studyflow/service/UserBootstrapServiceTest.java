package com.studyflow.service;

import com.studyflow.api.dto.UserBootstrapRequest;
import com.studyflow.domain.EducationLevel;
import com.studyflow.domain.ProfileType;
import com.studyflow.domain.User;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserBootstrapServiceTest {

    private static final String KEYCLOAK_ID = "kc-123";
    private static final String EMAIL = "student@example.com";
    private static final String CPF = "529.982.247-25";

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserBootstrapService userBootstrapService;

    UserBootstrapRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new UserBootstrapRequest(
                "Student Name",
                CPF,
                LocalDate.of(2000, 1, 15),
                EducationLevel.graduacao);
    }

    @Test
    void bootstrapCreatesEstudanteProfile() {
        when(userRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.empty());
        when(userRepository.findActiveByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.findActiveByCpf("52998224725")).thenReturn(Optional.empty());

        BootstrapResult result = userBootstrapService.bootstrap(KEYCLOAK_ID, EMAIL, validRequest);

        assertTrue(result.created());
        assertEquals(ProfileType.estudante, result.user().profile());
        assertEquals("52998224725", result.user().cpf());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).persist(captor.capture());
        assertEquals(ProfileType.estudante, captor.getValue().profile);
    }

    @Test
    void bootstrapReturnsExistingUserWithoutCreating() {
        User existing = activeUser();
        when(userRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.of(existing));

        BootstrapResult result = userBootstrapService.bootstrap(KEYCLOAK_ID, EMAIL, validRequest);

        assertFalse(result.created());
        assertEquals(existing.id, result.user().id());
        verify(userRepository, never()).persist(any(User.class));
    }

    @Test
    void bootstrapRejectsDuplicateEmail() {
        when(userRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.empty());
        when(userRepository.findActiveByEmail(EMAIL)).thenReturn(Optional.of(activeUser()));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userBootstrapService.bootstrap(KEYCLOAK_ID, EMAIL, validRequest));

        assertEquals(409, ex.getStatus());
    }

    @Test
    void bootstrapRejectsDuplicateCpf() {
        when(userRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.empty());
        when(userRepository.findActiveByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.findActiveByCpf("52998224725")).thenReturn(Optional.of(activeUser()));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userBootstrapService.bootstrap(KEYCLOAK_ID, EMAIL, validRequest));

        assertEquals(409, ex.getStatus());
    }

    @Test
    void bootstrapRejectsDisabledExistingUser() {
        User disabled = activeUser();
        disabled.deletedAt = Instant.now();
        when(userRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.of(disabled));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userBootstrapService.bootstrap(KEYCLOAK_ID, EMAIL, validRequest));

        assertEquals(403, ex.getStatus());
    }

    private User activeUser() {
        User user = new User();
        user.id = UUID.randomUUID();
        user.keycloakId = KEYCLOAK_ID;
        user.name = "Student Name";
        user.email = EMAIL;
        user.cpf = "52998224725";
        user.birthDate = LocalDate.of(2000, 1, 15);
        user.educationLevel = EducationLevel.graduacao;
        user.profile = ProfileType.estudante;
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        return user;
    }
}
