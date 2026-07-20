package com.studyflow.service;

import com.studyflow.domain.EducationLevel;
import com.studyflow.domain.ProfileType;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KeycloakAdminFacadeTest {

    @Test
    void failingFakeFacadeThrowsBusinessExceptionWithStatus() {
        KeycloakAdminFacade facade = new FailingKeycloakAdminFacade();

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> facade.createUser(
                        "user@example.com",
                        "password",
                        "User",
                        ProfileType.estudante,
                        Collections.emptyMap()));

        assertEquals(409, ex.getStatus());
        assertEquals("Falha ao criar usuario no Keycloak: HTTP 409", ex.getMessage());
    }

    private static final class FailingKeycloakAdminFacade implements KeycloakAdminFacade {

        @Override
        public String createUser(
                String email,
                String password,
                String name,
                ProfileType profile,
                Map<String, String> attributes) {
            throw new BusinessException(409, "Falha ao criar usuario no Keycloak: HTTP 409");
        }

        @Override
        public void setEnabled(String keycloakId, boolean enabled) {
            // no-op for test double
        }

        @Override
        public void updateEmail(String keycloakId, String email) {
            // no-op for test double
        }

        @Override
        public void updatePassword(String keycloakId, String password) {
            // no-op for test double
        }

        @Override
        public void updateProfile(
                String keycloakId,
                String name,
                String cpf,
                LocalDate birthDate,
                EducationLevel educationLevel,
                ProfileType profile) {
            // no-op for test double
        }
    }
}
