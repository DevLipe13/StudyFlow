package com.studyflow.api.dto;

public record LoginChangeCompleteRequest(
        String cpf,
        String newEmail,
        String newPassword) {
}
