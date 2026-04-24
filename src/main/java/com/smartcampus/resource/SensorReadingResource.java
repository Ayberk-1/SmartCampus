
package com.smartcampus.resource;

import com.smartcampus.dao.GenericDAO;
import com.smartcampus.dao.MockDatabase;
import com.smartcampus.exception.DataNotFoundException;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private String sensorId;

    private GenericDAO<SensorReading> readingDAO =
            new GenericDAO<>(MockDatabase.READINGS);

    private GenericDAO<Sensor> sensorDAO =
            new GenericDAO<>(MockDatabase.SENSORS);

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    private Sensor getSensorOrThrow() {
        return sensorDAO.getById(sensorId);
    }

    private void validateSensorStatus(Sensor sensor) {
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor is under maintenance");
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {

        getSensorOrThrow();

        List<SensorReading> readings = readingDAO.getAll()
                .stream()
                .filter(r -> sensorId.equals(r.getSensorId()))
                .collect(Collectors.toList());

        return Response.ok(readings).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {

        Sensor sensor = getSensorOrThrow();

        validateSensorStatus(sensor);

        reading.setSensorId(sensorId);
        
        if (reading.getId() == null || reading.getId().isEmpty()) {
            throw new BadRequestException("Reading ID must be provided");
        }
        readingDAO.add(reading);

        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED)
            .entity(reading)
            .header("Location", "/api/v1/sensors/" + sensorId + "/readings/" + reading.getId())
            .build();
    }
}