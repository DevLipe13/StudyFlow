package com.studyflow.api.dto;

import com.studyflow.domain.LoginChangeRequest;
import com.studyflow.domain.LoginChangeStatus;
import com.studyflow.domain.LoginChangeType;
import java.time.Instant;
import java.util.UUID;

public record LoginChangeResponse(
        UUID id,
        UUID requesterUserId,
        LoginChangeType changeType,
        LoginChangeStatus status,
        String proposedEmail,
        UUID reviewedByUserId,
        Instant reviewedAt,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt,
        String rejectionReason) {

    public static LoginChangeResponse from(LoginChangeRequest request) {
        return new LoginChangeResponse(
                request.id,
                request.requesterUserId,
                request.changeType,
                request.status,
                request.proposedEmail,
                request.reviewedByUserId,
                request.reviewedAt,
                request.completedAt,
                request.createdAt,
                request.updatedAt,
                request.rejectionReason);
    }
}
