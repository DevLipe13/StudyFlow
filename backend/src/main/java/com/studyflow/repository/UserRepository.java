package com.studyflow.repository;

import com.studyflow.domain.User;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, UUID> {

    public Optional<User> findByKeycloakId(String keycloakId) {
        return find("keycloakId", keycloakId).firstResultOptional();
    }

    public Optional<User> findActiveByEmail(String email) {
        return find("email = ?1 and deletedAt is null", email).firstResultOptional();
    }

    public Optional<User> findActiveByCpf(String cpf) {
        return find("cpf = ?1 and deletedAt is null", cpf).firstResultOptional();
    }

    public List<User> listActive() {
        return list("deletedAt is null order by name");
    }

    public List<User> listAllOrdered() {
        return list("order by name");
    }
}
