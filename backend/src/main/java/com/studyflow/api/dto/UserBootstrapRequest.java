package com.studyflow.api.dto;

import com.studyflow.domain.EducationLevel;
import java.time.LocalDate;

public record UserBootstrapRequest(
        String name,
        String cpf,
        LocalDate birthDate,
        EducationLevel educationLevel) {
}
