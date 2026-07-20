package com.studyflow.security;

import com.studyflow.domain.EducationLevel;
import com.studyflow.domain.ProfileType;
import com.studyflow.domain.User;
import com.studyflow.repository.UserRepository;
import com.studyflow.service.BusinessException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    JsonWebToken jwt;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    CurrentUserService currentUserService;

    @Test
    void requireUserReturnsActiveUser() {
        when(jwt.getSubject()).thenReturn("kc-1");
        User user = activeUser("kc-1");
        when(userRepository.findByKeycloakId("kc-1")).thenReturn(Optional.of(user));

        assertEquals(user, currentUserService.requireUser());
    }

    @Test
    void requireUserRejectsSoftDeletedUser() {
        when(jwt.getSubject()).thenReturn("kc-disabled");
        User user = activeUser("kc-disabled");
        user.deletedAt = Instant.now();
        when(userRepository.findByKeycloakId("kc-disabled")).thenReturn(Optional.of(user));

        BusinessException ex = assertThrows(BusinessException.class, currentUserService::requireUser);

        assertEquals(403, ex.getStatus());
        assertEquals("Usuario desabilitado", ex.getMessage());
    }

    @Test
    void requireUserThrowsWhenMissing() {
        when(jwt.getSubject()).thenReturn("kc-missing");
        when(userRepository.findByKeycloakId("kc-missing")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, currentUserService::requireUser);

        assertEquals(404, ex.getStatus());
    }

    private User activeUser(String keycloakId) {
        User user = new User();
        user.id = UUID.randomUUID();
        user.keycloakId = keycloakId;
        user.name = "Ana";
        user.email = "ana@example.com";
        user.cpf = "52998224725";
        user.birthDate = LocalDate.of(2000, 1, 1);
        user.educationLevel = EducationLevel.medio;
        user.profile = ProfileType.estudante;
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        return user;
    }
}
