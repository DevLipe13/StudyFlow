package com.studyflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "login_change_request")
public class LoginChangeRequest {

    @Id
    public UUID id;

    @Column(name = "requester_user_id", nullable = false)
    public UUID requesterUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    public LoginChangeType changeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public LoginChangeStatus status;

    @Column(name = "proposed_email")
    public String proposedEmail;

    @Column(name = "reviewed_by_user_id")
    public UUID reviewedByUserId;

    @Column(name = "reviewed_at")
    public Instant reviewedAt;

    @Column(name = "completed_at")
    public Instant completedAt;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Column(name = "rejection_reason")
    public String rejectionReason;
}
