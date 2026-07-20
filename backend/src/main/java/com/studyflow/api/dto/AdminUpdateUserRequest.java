package com.studyflow.api.dto;

import com.studyflow.domain.EducationLevel;
import com.studyflow.domain.ProfileType;
import java.time.LocalDate;

public record AdminUpdateUserRequest(
        String name,
        String cpf,
        LocalDate birthDate,
        EducationLevel educationLevel,
        ProfileType profile) {
}
