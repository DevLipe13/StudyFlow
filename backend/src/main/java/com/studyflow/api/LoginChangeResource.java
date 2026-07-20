package com.studyflow.api;

import com.studyflow.api.dto.LoginChangeCompleteRequest;
import com.studyflow.api.dto.LoginChangeCreateRequest;
import com.studyflow.api.dto.LoginChangeRejectRequest;
import com.studyflow.api.dto.LoginChangeResponse;
import com.studyflow.domain.User;
import com.studyflow.security.CurrentUserService;
import com.studyflow.service.LoginChangeService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/login-change-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoginChangeResource {

    @Inject
    LoginChangeService loginChangeService;

    @Inject
    CurrentUserService currentUserService;

    @POST
    public Response create(LoginChangeCreateRequest request) {
        User requester = currentUserService.requireUser();
        LoginChangeResponse created = loginChangeService.create(requester.id, request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    public List<LoginChangeResponse> list() {
        if (currentUserService.hasRole("admin")) {
            return loginChangeService.listForAdmin();
        }
        User requester = currentUserService.requireUser();
        return loginChangeService.listForRequester(requester.id);
    }

    @POST
    @Path("/{requestId}/approve")
    @RolesAllowed("admin")
    public LoginChangeResponse approve(@PathParam("requestId") UUID requestId) {
        User admin = currentUserService.requireUser();
        return loginChangeService.approve(requestId, admin.id);
    }

    @POST
    @Path("/{requestId}/reject")
    @RolesAllowed("admin")
    public LoginChangeResponse reject(@PathParam("requestId") UUID requestId, LoginChangeRejectRequest body) {
        User admin = currentUserService.requireUser();
        String reason = body != null ? body.rejectionReason() : null;
        return loginChangeService.reject(requestId, admin.id, reason);
    }

    @POST
    @Path("/{requestId}/complete")
    public LoginChangeResponse complete(
            @PathParam("requestId") UUID requestId,
            LoginChangeCompleteRequest request) {
        User requester = currentUserService.requireUser();
        return loginChangeService.complete(requestId, requester.id, request);
    }
}
