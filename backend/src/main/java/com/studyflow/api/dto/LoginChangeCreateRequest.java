package com.studyflow.api.dto;

import com.studyflow.domain.LoginChangeType;

public record LoginChangeCreateRequest(
        LoginChangeType changeType,
        String proposedEmail) {
}
