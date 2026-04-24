/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.dao.GenericDAO;
import com.smartcampus.dao.MockDatabase;
import com.smartcampus.exception.DataNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.Room;
import com.smartcampus.exception.LinkedResourceNotFoundException; 
import java.util.ArrayList;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private GenericDAO<Sensor> sensorDAO
            = new GenericDAO<>(MockDatabase.SENSORS);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Sensor> getSensors(@QueryParam("type") String type) {

        if (type == null) {
            return sensorDAO.getAll();
        }

        return sensorDAO.getAll()
                .stream()
                .filter(sensor -> sensor.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    
@POST
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public Response addSensor(Sensor sensor) {
    if (sensor == null) {
        throw new BadRequestException("Request body cannot be empty");
    }
    
    if (sensor.getId() == null || sensor.getRoomId() == null) {
        throw new BadRequestException("Sensor ID and roomId are required");
    }
    
    GenericDAO<Room> roomDAO = new GenericDAO<>(MockDatabase.ROOMS);

    Room room;
    try {
        room = roomDAO.getById(sensor.getRoomId());
    } catch (DataNotFoundException e) {
        throw new LinkedResourceNotFoundException("Room does not exist");
    }
    
    sensorDAO.add(sensor);
    
    if (room.getSensorIds() == null) {
        room.setSensorIds(new ArrayList<>());
    }
    room.getSensorIds().add(sensor.getId());

    return Response.status(Response.Status.CREATED)
        .entity(sensor)
        .header("Location", "/api/v1/sensors/" + sensor.getId())
        .build();
}

    
    
   @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        try {
        sensorDAO.getById(sensorId);
        } catch (DataNotFoundException e) {
            throw new DataNotFoundException("Sensor with ID " + sensorId + " not found.");
        }
        return new SensorReadingResource(sensorId);
    }

}