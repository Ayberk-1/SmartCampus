package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {
    
    @GET
    public Response getApiInfo() {

        Map<String, Object> response = new HashMap<>();

        response.put("version", "v1");
        response.put("contact", "campus-it@westminster.ac.uk");

        Map<String, Object> endpoints = new HashMap<>();

        Map<String, Object> rooms = new HashMap<>();

        rooms.put("list", Map.of(
            "href", "/api/v1/rooms",
            "method", "GET"
        ));

        rooms.put("create", Map.of(
            "href", "/api/v1/rooms",
            "method", "POST"
        ));

        rooms.put("detail", Map.of(
            "href", "/api/v1/rooms/{roomId}",
            "method", "GET"
        ));

        rooms.put("delete", Map.of(
            "href", "/api/v1/rooms/{roomId}",
            "method", "DELETE"
        ));
        

        Map<String, Object> sensors = new HashMap<>();

        sensors.put("list", Map.of(
            "href", "/api/v1/sensors",
            "method", "GET"
        ));

        sensors.put("create", Map.of(
            "href", "/api/v1/sensors",
            "method", "POST"
        ));

        sensors.put("filter", Map.of(
            "href", "/api/v1/sensors?type={type}",
            "method", "GET"
        ));

        sensors.put("readings", Map.of(
            "href", "/api/v1/sensors/{sensorId}/readings",
            "method", "GET/POST"
        ));
        
        endpoints.put("rooms", rooms);
        endpoints.put("sensors", sensors);

        response.put("resources", endpoints);

        return Response.ok(response).build();
    }
}
