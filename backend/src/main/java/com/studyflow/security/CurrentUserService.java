package com.studyflow.security;

import com.studyflow.domain.User;
import com.studyflow.repository.UserRepository;
import com.studyflow.service.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
public class CurrentUserService {

    @Inject
    JsonWebToken jwt;

    @Inject
    UserRepository userRepository;

    public String getKeycloakId() {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new BusinessException(401, "Sujeito JWT ausente");
        }
        return subject;
    }

    public String getEmail() {
        String email = jwt.getClaim("email");
        if (email == null || email.isBlank()) {
            throw new BusinessException(400, "Claim de e-mail ausente no JWT");
        }
        return email.trim();
    }

    public User requireUser() {
        User user = userRepository.findByKeycloakId(getKeycloakId())
                .orElseThrow(() -> new BusinessException(404, "Usuario nao encontrado"));
        if (user.isDisabled()) {
            throw new BusinessException(403, "Usuario desabilitado");
        }
        return user;
    }

    public boolean hasRole(String role) {
        return jwt.getGroups() != null && jwt.getGroups().contains(role);
    }
}
