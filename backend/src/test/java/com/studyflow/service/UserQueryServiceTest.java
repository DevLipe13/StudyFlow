package com.studyflow.service;

import com.studyflow.domain.EducationLevel;
import com.studyflow.domain.ProfileType;
import com.studyflow.domain.User;
import com.studyflow.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserQueryService userQueryService;

    @Test
    void getByKeycloakIdReturnsActiveUser() {
        User user = sampleUser("kc-1", false);
        when(userRepository.findByKeycloakId("kc-1")).thenReturn(Optional.of(user));

        assertEquals(user, userQueryService.getByKeycloakId("kc-1"));
    }

    @Test
    void getByKeycloakIdRejectsDisabledUser() {
        User user = sampleUser("kc-2", true);
        when(userRepository.findByKeycloakId("kc-2")).thenReturn(Optional.of(user));

        BusinessException ex = assertThrows(
                BusinessException.class, () -> userQueryService.getByKeycloakId("kc-2"));

        assertEquals(403, ex.getStatus());
        assertEquals("Usuario desabilitado", ex.getMessage());
    }

    @Test
    void getByKeycloakIdThrowsWhenMissing() {
        when(userRepository.findByKeycloakId("missing")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class, () -> userQueryService.getByKeycloakId("missing"));

        assertEquals(404, ex.getStatus());
    }

    private User sampleUser(String keycloakId, boolean disabled) {
        User user = new User();
        user.id = UUID.randomUUID();
        user.keycloakId = keycloakId;
        user.name = "User";
        user.email = "user@example.com";
        user.cpf = "52998224725";
        user.birthDate = LocalDate.of(1990, 1, 1);
        user.educationLevel = EducationLevel.medio;
        user.profile = ProfileType.estudante;
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        if (disabled) {
            user.deletedAt = Instant.now();
        }
        return user;
    }
}
