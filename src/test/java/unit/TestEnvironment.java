
package unit;

import iot.Characteristic;
import iot.Environment;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import iot.strategy.response.gateway.DummyResponse;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;
import util.Connection;
import util.Path;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestEnvironment {

    private Mote generateDummyMote(Environment environment, long id) {
        return new Mote(id, 0, 0, 20, 12, new ArrayList<>(), 10000, new Path(environment.getGraph()), 1, 1, 1, 1, environment);
    }

    private Gateway generateDummyGateway(Environment environment, long id) {
        return new Gateway(id, 0, 0, 20, 12, new DummyResponse(), environment);
    }


    @Test
    void happyDay() {
        Environment environment = new Environment(new Characteristic[1][1], new GeoPosition(5, 5), 1, new HashMap<>(), new HashMap<>());

        assertTrue(environment.getMotes().isEmpty());
        assertTrue(environment.getGateways().isEmpty());
        assertEquals(environment.getClock().getTime(), LocalTime.of(0,0,0));
        assertNull(environment.getCharacteristic(0,0));
        assertEquals(environment.getMapCenter(), new GeoPosition(5,5));
        assertEquals(environment.getMaxXpos(), 0);
        assertEquals(environment.getMaxYpos(), 0);
        assertEquals(environment.getNumberOfRuns(), 1);
        assertEquals(environment.getNumberOfZones(), 1);
    }

    @Test
    void waypointsConnections() {
        Environment environment = new Environment(new Characteristic[1][1], new GeoPosition(5, 5), 1,
            new HashMap<>(Map.of(1L, new GeoPosition(6,6), 3L, new GeoPosition(10,5))), new HashMap<>(Map.of(4L, new Connection(1L, 3L))));

        assertTrue(environment.getGraph().connectionExists(1L, 3L));
        assertEquals(environment.getGraph().getWayPoint(1L), new GeoPosition(6,6));
        assertNull(environment.getGraph().getWayPoint(2L));
        assertEquals(environment.getGraph().getWayPoint(3L), new GeoPosition(10, 5));
    }

    @Test
    void characteristics() {
        Characteristic[][] characteristics = new Characteristic[500][500];
        for (int i = 0; i < 500; i++) {
            for (int j = 0; j < 100; j++) {
                characteristics[i][j] = Characteristic.Plain;
            }
            for (int j = 100; j < 300; j++) {
                characteristics[i][j] = Characteristic.Forest;
            }
            for (int j = 300; j < 500; j++) {
                characteristics[i][j] = Characteristic.City;
            }
        }

        Environment environment = new Environment(characteristics, new GeoPosition(10, 10), 25, new HashMap<>(), new HashMap<>());

        assertEquals(environment.getNumberOfZones(), 25);
        assertEquals(environment.getMaxXpos(), 499);
        assertEquals(environment.getMaxYpos(), 499);

        assertEquals(environment.getCharacteristic(50, 50), Characteristic.Plain);
        assertEquals(environment.getCharacteristic(499, 299), Characteristic.Forest);
        assertEquals(environment.getCharacteristic(250, 300), Characteristic.City);

        environment.setCharacteristics(Characteristic.Forest, 20, 20);
        assertEquals(environment.getCharacteristic(20, 20), Characteristic.Forest);
    }

    @Test
    void rainyDay() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Environment(new Characteristic[0][0], new GeoPosition(0,0), 1, new HashMap<>(), new HashMap<>());
        });
    }

    @Test
    void addMotes() {
        Environment environment = new Environment(new Characteristic[1][1], new GeoPosition(0,0), 1, new HashMap<>(), new HashMap<>());

        Mote dummyMote1 = generateDummyMote(environment, 1);
        Mote dummyMote2 = generateDummyMote(environment, 2);

        environment.addMote(dummyMote1);
        environment.addMote(dummyMote2);

        var motes = environment.getMotes();
        assertEquals(motes.size(), 2);
        assertTrue(motes.contains(dummyMote1));
        assertTrue(motes.contains(dummyMote2));
    }

    @Test
    void addGateways() {
        Environment environment = new Environment(new Characteristic[1][1], new GeoPosition(0,0), 1, new HashMap<>(), new HashMap<>());

        Gateway dummyGateway1 = generateDummyGateway(environment, 1);
        Gateway dummyGateway2 = generateDummyGateway(environment, 200);

        environment.addGateway(dummyGateway1);
        environment.addGateway(dummyGateway2);

        var gateways = environment.getGateways();
        assertEquals(gateways.size(), 2);
        assertTrue(gateways.contains(dummyGateway1));
        assertTrue(gateways.contains(dummyGateway2));
    }

    @Test
    void addMoteGatewayMultipleEnv() {
        Environment environment1 = new Environment(new Characteristic[1][1], new GeoPosition(0,0), 1, new HashMap<>(), new HashMap<>());
        Environment environment2 = new Environment(new Characteristic[1][1], new GeoPosition(0,0), 1, new HashMap<>(), new HashMap<>());

        Gateway gw = generateDummyGateway(environment2, 1);
        Mote mote = generateDummyMote(environment2, 908);

        environment2.addMote(mote);
        environment2.addGateway(gw);

        assertTrue(environment1.getMotes().isEmpty());
        assertFalse(environment2.getMotes().isEmpty());
        assertTrue(environment1.getGateways().isEmpty());
        assertFalse(environment2.getGateways().isEmpty());
    }

    @Test
    void moveMote() {
        GeoPosition origin = new GeoPosition(0,0);
        Environment environment = new Environment(new Characteristic[600][600], origin, 1, new HashMap<>(), new HashMap<>());

        GeoPosition destination1 = environment.getMapHelper().toGeoPosition(200, 500);
        GeoPosition destination2 = environment.getMapHelper().toGeoPosition(300, 400);
        Path path = new Path(environment.getGraph());
        path.addPosition(destination1);
        path.addPosition(destination2);

        Mote mote = new Mote(1, 200, 200, 20, 12, List.of(), 10000, path, 1, 0, 1, 0, environment);

        environment.addMote(mote);

        for (int i = 0; i < 300; i++) {
            environment.moveMote(mote, destination1);
        }

        assertEquals(mote.getXPosInt(), 200);
        assertEquals(mote.getYPosInt(), 500);

        for (int i = 0; i < 100; i++) {
            environment.moveMote(mote, destination2);
        }

        assertNotEquals(mote.getXPosInt(), 300);
        assertNotEquals(mote.getYPosInt(), 400);

        for (int i = 0; i < 42; i++) {
            environment.moveMote(mote, destination2);
        }

        assertEquals(mote.getXPosInt(), 300);
        assertEquals(mote.getYPosInt(), 400);
    }

    @Test
    void multipleEnvironments() {
        // TODO
    }
}
