package com.studyflow.service;

import com.studyflow.domain.EducationLevel;
import com.studyflow.domain.ProfileType;
import java.time.LocalDate;
import java.util.Map;

public interface KeycloakAdminFacade {

    String createUser(String email, String password, String name, ProfileType profile, Map<String, String> attributes);

    void setEnabled(String keycloakId, boolean enabled);

    void updateEmail(String keycloakId, String email);

    void updatePassword(String keycloakId, String password);

    void updateProfile(
            String keycloakId,
            String name,
            String cpf,
            LocalDate birthDate,
            EducationLevel educationLevel,
            ProfileType profile);
}
