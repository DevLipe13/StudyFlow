package com.studyflow.api;

import com.studyflow.api.dto.AdminCreateUserRequest;
import com.studyflow.api.dto.AdminUpdateUserRequest;
import com.studyflow.api.dto.UserResponse;
import com.studyflow.domain.User;
import com.studyflow.security.CurrentUserService;
import com.studyflow.service.UserAdminService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
public class UserAdminResource {

    @Inject
    UserAdminService userAdminService;

    @Inject
    CurrentUserService currentUserService;

    @GET
    public List<UserResponse> list(@QueryParam("includeDisabled") @DefaultValue("false") boolean includeDisabled) {
        return userAdminService.list(includeDisabled);
    }

    @POST
    public Response create(AdminCreateUserRequest request) {
        UserResponse created = userAdminService.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/{userId}")
    public UserResponse get(@PathParam("userId") UUID userId) {
        return userAdminService.getById(userId);
    }

    @PATCH
    @Path("/{userId}")
    public UserResponse update(@PathParam("userId") UUID userId, AdminUpdateUserRequest request) {
        return userAdminService.update(userId, request);
    }

    @POST
    @Path("/{userId}/disable")
    public Response disable(@PathParam("userId") UUID userId) {
        User admin = currentUserService.requireUser();
        userAdminService.disable(userId, admin.id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{userId}/enable")
    public Response enable(@PathParam("userId") UUID userId) {
        userAdminService.enable(userId);
        return Response.noContent().build();
    }
}
