
package com.smartcampus.resource;

import com.smartcampus.dao.GenericDAO;
import com.smartcampus.dao.MockDatabase;
import com.smartcampus.model.Room;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.exception.DataNotFoundException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import javax.ws.rs.core.Response;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private GenericDAO<Room> roomDAO
            = new GenericDAO<>(MockDatabase.ROOMS);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Room> getAllRooms() {
        return roomDAO.getAll();
    }

    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = roomDAO.getById(roomId); // now throws exception if not found

        return Response.ok(room).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addRoom(Room room) {
        if (room.getId() == null || room.getId().isEmpty()) {
            throw new BadRequestException("Room ID must be provided");
        }
        roomDAO.add(room);

        return Response.status(Response.Status.CREATED)
            .entity(room)
            .header("Location", "/api/v1/rooms/" + room.getId())
            .build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {

        Room room = roomDAO.getById(roomId);

        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room cannot be deleted because sensors are assigned");
        }

        roomDAO.delete(roomId);

        return Response.noContent().build(); // 204
    }
}
