
package com.smartcampus.exception;

import com.smartcampus.model.ErrorMessage;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {

        ErrorMessage error = new ErrorMessage(
                exception.getMessage(),
                422,
                "Invalid reference in request"
        );

        return Response.status(422)
                .entity(error)
                .build();
    }
}