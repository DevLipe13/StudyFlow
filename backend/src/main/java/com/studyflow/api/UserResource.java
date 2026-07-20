package com.studyflow.api;

import com.studyflow.api.dto.UserBootstrapRequest;
import com.studyflow.api.dto.UserResponse;
import com.studyflow.domain.User;
import com.studyflow.security.CurrentUserService;
import com.studyflow.service.BootstrapResult;
import com.studyflow.service.UserBootstrapService;
import com.studyflow.service.UserQueryService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    UserQueryService userQueryService;

    @Inject
    UserBootstrapService userBootstrapService;

    @Inject
    CurrentUserService currentUserService;

    @GET
    @Path("/me")
    public UserResponse me() {
        User user = userQueryService.getByKeycloakId(jwt.getSubject());
        return UserResponse.from(user);
    }

    @POST
    @Path("/me/bootstrap")
    public Response bootstrap(UserBootstrapRequest request) {
        BootstrapResult result = userBootstrapService.bootstrap(
                jwt.getSubject(),
                currentUserService.getEmail(),
                request);
        Response.Status status = result.created() ? Response.Status.CREATED : Response.Status.OK;
        return Response.status(status).entity(result.user()).build();
    }
}
