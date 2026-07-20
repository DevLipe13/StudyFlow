package com.studyflow.api.dto;

import com.studyflow.domain.EducationLevel;
import com.studyflow.domain.ProfileType;
import com.studyflow.domain.User;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String keycloakId,
        String name,
        String email,
        String cpf,
        LocalDate birthDate,
        EducationLevel educationLevel,
        ProfileType profile,
        Instant createdAt,
        Instant updatedAt,
        boolean disabled) {

    public static UserResponse from(User user) {
        return from(user, false);
    }

    public static UserResponse from(User user, boolean maskCpf) {
        String cpfValue = maskCpf ? maskCpf(user.cpf) : user.cpf;
        return new UserResponse(
                user.id,
                user.keycloakId,
                user.name,
                user.email,
                cpfValue,
                user.birthDate,
                user.educationLevel,
                user.profile,
                user.createdAt,
                user.updatedAt,
                user.isDisabled());
    }

    private static String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 2) {
            return cpf;
        }
        String suffix = cpf.substring(cpf.length() - 2);
        return "***.***.***-" + suffix;
    }
}
