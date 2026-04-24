
package com.smartcampus.exception;

import com.smartcampus.model.ErrorMessage;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {

        ErrorMessage error = new ErrorMessage(
                exception.getMessage(),
                409,
                "Room still contains sensors"
        );

        return Response.status(Response.Status.CONFLICT)
                .entity(error)
                .build();
    }
}