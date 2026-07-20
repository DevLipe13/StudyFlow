package com.studyflow.repository;

import com.studyflow.domain.LoginChangeRequest;
import com.studyflow.domain.LoginChangeStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class LoginChangeRequestRepository implements PanacheRepositoryBase<LoginChangeRequest, UUID> {

    public List<LoginChangeRequest> findByRequester(UUID requesterUserId) {
        return list("requesterUserId = ?1 order by createdAt desc", requesterUserId);
    }

    public List<LoginChangeRequest> findByStatus(LoginChangeStatus status) {
        return list("status = ?1 order by createdAt asc", status);
    }
}
