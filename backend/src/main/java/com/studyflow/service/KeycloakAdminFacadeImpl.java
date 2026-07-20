package com.studyflow.service;

import com.studyflow.domain.EducationLevel;
import com.studyflow.domain.ProfileType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

@ApplicationScoped
public class KeycloakAdminFacadeImpl implements KeycloakAdminFacade {

    @Inject
    Keycloak keycloak;

    @ConfigProperty(name = "studyflow.keycloak.realm")
    String realm;

    @Override
    public String createUser(String email, String password, String name, ProfileType profile, Map<String, String> attributes) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(email);
        user.setEmail(email);
        user.setEmailVerified(true);
        user.setFirstName(name);
        if (attributes != null && !attributes.isEmpty()) {
            user.setAttributes(attributes.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> List.of(e.getValue()))));
        }

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(false);
        credential.setValue(password);
        user.setCredentials(Collections.singletonList(credential));

        String keycloakId;
        try (Response response = keycloak.realm(realm).users().create(user)) {
            if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                throw new BusinessException(409, "Falha ao criar usuario no Keycloak: HTTP " + response.getStatus());
            }
            URI location = response.getLocation();
            if (location == null) {
                throw new BusinessException(500, "Keycloak nao retornou localizacao do usuario");
            }
            String path = location.getPath();
            keycloakId = path.substring(path.lastIndexOf('/') + 1);
        }

        assignRealmRole(keycloakId, profile.name());
        return keycloakId;
    }

    @Override
    public void setEnabled(String keycloakId, boolean enabled) {
        UserResource resource = keycloak.realm(realm).users().get(keycloakId);
        UserRepresentation user = resource.toRepresentation();
        user.setEnabled(enabled);
        resource.update(user);
    }

    @Override
    public void updateEmail(String keycloakId, String email) {
        UserResource resource = keycloak.realm(realm).users().get(keycloakId);
        UserRepresentation user = resource.toRepresentation();
        user.setEmail(email);
        user.setUsername(email);
        resource.update(user);
    }

    @Override
    public void updatePassword(String keycloakId, String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(false);
        credential.setValue(password);
        keycloak.realm(realm).users().get(keycloakId).resetPassword(credential);
    }

    @Override
    public void updateProfile(
            String keycloakId,
            String name,
            String cpf,
            LocalDate birthDate,
            EducationLevel educationLevel,
            ProfileType profile) {
        UserResource resource = keycloak.realm(realm).users().get(keycloakId);
        UserRepresentation user = resource.toRepresentation();
        user.setFirstName(name);

        Map<String, List<String>> attributes = new HashMap<>();
        if (user.getAttributes() != null) {
            attributes.putAll(user.getAttributes());
        }
        attributes.put("cpf", List.of(cpf));
        attributes.put("birthDate", List.of(birthDate.toString()));
        attributes.put("educationLevel", List.of(educationLevel.name()));
        attributes.put("profile", List.of(profile.name()));
        user.setAttributes(attributes);
        resource.update(user);

        replaceRealmProfileRole(keycloakId, profile.name());
    }

    private void assignRealmRole(String keycloakId, String roleName) {
        RoleRepresentation role = keycloak.realm(realm).roles().get(roleName).toRepresentation();
        keycloak.realm(realm).users().get(keycloakId).roles().realmLevel().add(List.of(role));
    }

    private void replaceRealmProfileRole(String keycloakId, String roleName) {
        var realmLevel = keycloak.realm(realm).users().get(keycloakId).roles().realmLevel();
        List<RoleRepresentation> current = realmLevel.listAll();
        List<RoleRepresentation> toRemove = current.stream()
                .filter(role -> "estudante".equals(role.getName()) || "admin".equals(role.getName()))
                .toList();
        if (!toRemove.isEmpty()) {
            realmLevel.remove(toRemove);
        }
        assignRealmRole(keycloakId, roleName);
    }
}
