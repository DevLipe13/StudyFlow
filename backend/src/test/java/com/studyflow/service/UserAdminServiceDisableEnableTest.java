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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceDisableEnableTest {

    @Mock
    UserRepository userRepository;

    @Mock
    KeycloakAdminFacade keycloakAdminFacade;

    @InjectMocks
    UserAdminService userAdminService;

    @Test
    void disableSyncsKeycloakThenSoftDeletes() {
        UUID targetId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        User target = activeUser(targetId, "kc-target");

        when(userRepository.findByIdOptional(targetId)).thenReturn(Optional.of(target));

        userAdminService.disable(targetId, adminId);

        verify(keycloakAdminFacade).setEnabled("kc-target", false);
        assertNotNull(target.deletedAt);
    }

    @Test
    void disableRejectsSelfDisable() {
        UUID userId = UUID.randomUUID();

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userAdminService.disable(userId, userId));

        assertEquals(400, ex.getStatus());
    }

    @Test
    void disableThrowsWhenKeycloakFails() {
        UUID targetId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        User target = activeUser(targetId, "kc-target");

        when(userRepository.findByIdOptional(targetId)).thenReturn(Optional.of(target));
        doThrow(new RuntimeException("keycloak down"))
                .when(keycloakAdminFacade)
                .setEnabled("kc-target", false);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userAdminService.disable(targetId, adminId));

        assertEquals(409, ex.getStatus());
        assertNull(target.deletedAt);
    }

    @Test
    void enableSyncsKeycloakThenClearsSoftDelete() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "kc-user");
        user.deletedAt = Instant.now();

        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(user));

        userAdminService.enable(userId);

        verify(keycloakAdminFacade).setEnabled("kc-user", true);
        assertNull(user.deletedAt);
    }

    @Test
    void enableIsIdempotentForActiveUser() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "kc-user");

        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(user));

        userAdminService.enable(userId);

        verify(keycloakAdminFacade, org.mockito.Mockito.never()).setEnabled("kc-user", true);
    }

    private User activeUser(UUID id, String keycloakId) {
        User user = new User();
        user.id = id;
        user.keycloakId = keycloakId;
        user.name = "User";
        user.email = "user@example.com";
        user.cpf = "52998224725";
        user.birthDate = LocalDate.of(1990, 1, 1);
        user.educationLevel = EducationLevel.medio;
        user.profile = ProfileType.estudante;
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        return user;
    }
}
