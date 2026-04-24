
package com.smartcampus.dao;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 *
 * @author Asus
 */
public class MockDatabase {
    public static final List<Room> ROOMS = Collections.synchronizedList(new ArrayList<>());
    public static final List<Sensor> SENSORS = Collections.synchronizedList(new ArrayList<>());
    public static final List<SensorReading> READINGS = Collections.synchronizedList(new ArrayList<>());

    static {

        // Initialise Rooms
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("ENG-101", "Engineering Lab", 30);

        ROOMS.add(r1);
        ROOMS.add(r2);


        // Initialise Sensors
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 21.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001", "CO2", "ACTIVE", 400, "LIB-301");
        Sensor s3 = new Sensor("OCC-001", "Occupancy", "MAINTENANCE", 0, "ENG-101");

        SENSORS.add(s1);
        SENSORS.add(s2);
        SENSORS.add(s3);


        // Link sensors to rooms
        r1.getSensorIds().add("TEMP-001");
        r1.getSensorIds().add("CO2-001");
        r2.getSensorIds().add("OCC-001");


        // Initialise Sensor Readings
        READINGS.add(new SensorReading("R1", "TEMP-001", System.currentTimeMillis(), 21.5));
        READINGS.add(new SensorReading("R2", "TEMP-001", System.currentTimeMillis(), 22.0));
        READINGS.add(new SensorReading("R3", "CO2-001", System.currentTimeMillis(), 405));

    }
}
