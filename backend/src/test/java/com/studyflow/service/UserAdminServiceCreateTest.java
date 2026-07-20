package com.studyflow.service;

import com.studyflow.api.dto.AdminCreateUserRequest;
import com.studyflow.domain.EducationLevel;
import com.studyflow.domain.ProfileType;
import com.studyflow.domain.User;
import com.studyflow.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceCreateTest {

    @Mock
    UserRepository userRepository;

    @Mock
    KeycloakAdminFacade keycloakAdminFacade;

    @InjectMocks
    UserAdminService userAdminService;

    AdminCreateUserRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new AdminCreateUserRequest(
                "Admin Created",
                "newuser@example.com",
                "529.982.247-25",
                LocalDate.of(1995, 5, 20),
                EducationLevel.medio,
                ProfileType.admin,
                "SecurePass123!");
    }

    @Test
    void createPersistsUserAfterKeycloak() {
        when(userRepository.findActiveByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(userRepository.findActiveByCpf("52998224725")).thenReturn(Optional.empty());
        when(keycloakAdminFacade.createUser(
                eq("newuser@example.com"),
                eq("SecurePass123!"),
                eq("Admin Created"),
                eq(ProfileType.admin),
                any(Map.class)))
                .thenReturn("kc-new");

        var response = userAdminService.create(validRequest);

        assertEquals("kc-new", response.keycloakId());
        assertEquals(ProfileType.admin, response.profile());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).persist(captor.capture());
        assertEquals("52998224725", captor.getValue().cpf);
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(userRepository.findActiveByEmail("newuser@example.com"))
                .thenReturn(Optional.of(existingUser()));

        BusinessException ex = assertThrows(BusinessException.class, () -> userAdminService.create(validRequest));

        assertEquals(409, ex.getStatus());
    }

    @Test
    void createThrowsWhenKeycloakFails() {
        when(userRepository.findActiveByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(userRepository.findActiveByCpf("52998224725")).thenReturn(Optional.empty());
        when(keycloakAdminFacade.createUser(any(), any(), any(), any(), any()))
                .thenThrow(new BusinessException(409, "Failed to create Keycloak user: HTTP 409"));

        BusinessException ex = assertThrows(BusinessException.class, () -> userAdminService.create(validRequest));

        assertEquals(409, ex.getStatus());
    }

    private User existingUser() {
        User user = new User();
        user.id = UUID.randomUUID();
        user.keycloakId = "kc-existing";
        user.email = "newuser@example.com";
        user.cpf = "52998224725";
        user.name = "Existing";
        user.birthDate = LocalDate.of(1990, 1, 1);
        user.educationLevel = EducationLevel.medio;
        user.profile = ProfileType.estudante;
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        return user;
    }
}
