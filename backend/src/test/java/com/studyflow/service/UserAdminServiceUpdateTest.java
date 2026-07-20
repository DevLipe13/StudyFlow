package com.studyflow.service;

import com.studyflow.api.dto.AdminUpdateUserRequest;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceUpdateTest {

    @Mock
    UserRepository userRepository;

    @Mock
    KeycloakAdminFacade keycloakAdminFacade;

    @InjectMocks
    UserAdminService userAdminService;

    @Test
    void updateChangesAllowedFields() {
        UUID userId = UUID.randomUUID();
        User user = sampleUser(userId);
        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(user));
        when(userRepository.findActiveByCpf("39053344705")).thenReturn(Optional.empty());

        var response = userAdminService.update(
                userId,
                new AdminUpdateUserRequest(
                        "Updated Name",
                        "390.533.447-05",
                        LocalDate.of(1992, 3, 10),
                        EducationLevel.posGraduacao,
                        ProfileType.admin));

        assertEquals("Updated Name", response.name());
        assertEquals("39053344705", response.cpf());
        assertEquals(EducationLevel.posGraduacao, response.educationLevel());
        assertEquals(ProfileType.admin, response.profile());
        verify(keycloakAdminFacade)
                .updateProfile(
                        user.keycloakId,
                        "Updated Name",
                        "39053344705",
                        LocalDate.of(1992, 3, 10),
                        EducationLevel.posGraduacao,
                        ProfileType.admin);
    }

    @Test
    void updateRejectsDuplicateCpfFromAnotherUser() {
        UUID userId = UUID.randomUUID();
        User user = sampleUser(userId);
        User other = sampleUser(UUID.randomUUID());
        other.cpf = "39053344705";

        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(user));
        when(userRepository.findActiveByCpf("39053344705")).thenReturn(Optional.of(other));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userAdminService.update(
                        userId,
                        new AdminUpdateUserRequest(null, "390.533.447-05", null, null, null)));

        assertEquals(409, ex.getStatus());
    }

    @Test
    void updateThrowsWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userAdminService.update(userId, new AdminUpdateUserRequest("Name", null, null, null, null)));

        assertEquals(404, ex.getStatus());
    }

    private User sampleUser(UUID id) {
        User user = new User();
        user.id = id;
        user.keycloakId = "kc-" + id;
        user.name = "Original";
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
