package com.studyflow.service;

import com.studyflow.domain.EducationLevel;
import com.studyflow.domain.ProfileType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakAdminFacadeImplTest {

    @Mock
    Keycloak keycloak;

    @Mock
    RealmResource realmResource;

    @Mock
    UsersResource usersResource;

    @Mock
    RolesResource rolesResource;

    @Mock
    RoleResource roleResource;

    @Mock
    UserResource userResource;

    @Mock
    RoleMappingResource roleMappingResource;

    @Mock
    RoleScopeResource roleScopeResource;

    KeycloakAdminFacadeImpl facade;

    @BeforeEach
    void setUp() {
        facade = new KeycloakAdminFacadeImpl();
        facade.keycloak = keycloak;
        facade.realm = "studyflow";

        when(keycloak.realm("studyflow")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        org.mockito.Mockito.lenient().when(realmResource.roles()).thenReturn(rolesResource);
    }

    @Test
    void createUserAssignsRealmRoleViaRoleMappingApi() {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(201);
        when(response.getLocation()).thenReturn(URI.create("http://keycloak/users/kc-created"));
        when(usersResource.create(org.mockito.ArgumentMatchers.any(UserRepresentation.class))).thenReturn(response);

        RoleRepresentation estudanteRole = new RoleRepresentation();
        estudanteRole.setName("estudante");
        when(rolesResource.get("estudante")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(estudanteRole);
        when(usersResource.get("kc-created")).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        String id = facade.createUser(
                "new@example.com",
                "SecurePass123!",
                "New User",
                ProfileType.estudante,
                Map.of("cpf", "52998224725"));

        assertEquals("kc-created", id);
        verify(roleScopeResource).add(List.of(estudanteRole));
    }

    @Test
    void createUserThrowsWhenKeycloakReturnsConflict() {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(409);
        when(usersResource.create(org.mockito.ArgumentMatchers.any(UserRepresentation.class))).thenReturn(response);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> facade.createUser(
                        "dup@example.com",
                        "pass",
                        "Dup",
                        ProfileType.estudante,
                        Collections.emptyMap()));

        assertEquals(409, ex.getStatus());
    }

    @Test
    void updateProfileSyncsAttributesAndReplacesRoles() {
        UserRepresentation representation = new UserRepresentation();
        representation.setAttributes(Map.of("cpf", List.of("11111111111")));

        when(usersResource.get("kc-1")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(representation);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        RoleRepresentation existing = new RoleRepresentation();
        existing.setName("estudante");
        when(roleScopeResource.listAll()).thenReturn(List.of(existing));

        RoleRepresentation adminRole = new RoleRepresentation();
        adminRole.setName("admin");
        when(rolesResource.get("admin")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(adminRole);

        facade.updateProfile(
                "kc-1",
                "Updated",
                "39053344705",
                LocalDate.of(1991, 2, 3),
                EducationLevel.graduacao,
                ProfileType.admin);

        ArgumentCaptor<UserRepresentation> captor = ArgumentCaptor.forClass(UserRepresentation.class);
        verify(userResource).update(captor.capture());
        assertEquals("Updated", captor.getValue().getFirstName());
        assertEquals(List.of("39053344705"), captor.getValue().getAttributes().get("cpf"));
        assertEquals(List.of("admin"), captor.getValue().getAttributes().get("profile"));

        verify(roleScopeResource).remove(anyList());
        verify(roleScopeResource).add(List.of(adminRole));
    }
}
