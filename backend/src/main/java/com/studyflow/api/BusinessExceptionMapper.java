package com.studyflow.api;

import com.studyflow.service.BusinessException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

    @Override
    public Response toResponse(BusinessException exception) {
        return Response.status(exception.getStatus())
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("message", exception.getMessage()))
                .build();
    }
}
