package com.studyflow.service;

import com.studyflow.domain.User;
import com.studyflow.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserQueryService {

    @Inject
    UserRepository userRepository;

    public User getByKeycloakId(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new BusinessException(404, "Usuario nao encontrado"));
        if (user.isDisabled()) {
            throw new BusinessException(403, "Usuario desabilitado");
        }
        return user;
    }
}
