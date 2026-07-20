package com.studyflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    public UUID id;

    @Column(name = "keycloak_id", nullable = false, unique = true)
    public String keycloakId;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String email;

    @Column(nullable = false, length = 11)
    public String cpf;

    @Column(name = "birth_date", nullable = false)
    public LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level", nullable = false)
    public EducationLevel educationLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ProfileType profile;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Column(name = "deleted_at")
    public Instant deletedAt;

    public boolean isDisabled() {
        return deletedAt != null;
    }
}
