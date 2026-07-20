package com.studyflow.api;

import com.studyflow.api.dto.AdminCreateUserRequest;
import com.studyflow.api.dto.UserResponse;
import com.studyflow.domain.EducationLevel;
import com.studyflow.domain.ProfileType;
import com.studyflow.domain.User;
import com.studyflow.security.CurrentUserService;
import com.studyflow.service.UserAdminService;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminResourceTest {

    @Mock
    UserAdminService userAdminService;

    @Mock
    CurrentUserService currentUserService;

    @InjectMocks
    UserAdminResource userAdminResource;

    @Test
    void listDelegatesToService() {
        when(userAdminService.list(true)).thenReturn(List.of());

        assertEquals(0, userAdminResource.list(true).size());
        verify(userAdminService).list(true);
    }

    @Test
    void createReturnsCreatedStatus() {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "Nome",
                "user@example.com",
                "529.982.247-25",
                LocalDate.of(1990, 1, 1),
                EducationLevel.medio,
                ProfileType.estudante,
                "password123");
        UserResponse created = sampleResponse();
        when(userAdminService.create(request)).thenReturn(created);

        Response response = userAdminResource.create(request);

        assertEquals(201, response.getStatus());
        assertEquals(created, response.getEntity());
    }

    @Test
    void disableUsesCurrentAdminId() {
        UUID targetId = UUID.randomUUID();
        User admin = new User();
        admin.id = UUID.randomUUID();
        when(currentUserService.requireUser()).thenReturn(admin);

        Response response = userAdminResource.disable(targetId);

        assertEquals(204, response.getStatus());
        verify(userAdminService).disable(targetId, admin.id);
    }

    @Test
    void enableDelegatesToService() {
        UUID userId = UUID.randomUUID();

        Response response = userAdminResource.enable(userId);

        assertEquals(204, response.getStatus());
        verify(userAdminService).enable(userId);
    }

    private UserResponse sampleResponse() {
        return new UserResponse(
                UUID.randomUUID(),
                "kc-1",
                "Nome",
                "user@example.com",
                "52998224725",
                LocalDate.of(1990, 1, 1),
                EducationLevel.medio,
                ProfileType.estudante,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"),
                false);
    }
}
