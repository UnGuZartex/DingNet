package unit;

import iot.Characteristic;
import iot.Environment;
import iot.networkentity.Mote;
import iot.networkentity.MoteSensor;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;
import util.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestMote {

    @Test
    void happyDay() {
        Environment environment = new Environment(new Characteristic[200][200], new GeoPosition(10, 10), 1, new HashMap<>(), new HashMap<>());
        Mote mote = new Mote(1L, 10, 10, 10, 12, new ArrayList<>(), 20, new Path(environment.getGraph()), 1, environment);

        assertEquals(mote.getEUI(), 1);
        assertEquals(mote.getEnergyLevel(), 20);
        assertEquals(mote.getFrameCounter(), 0);
        assertEquals(mote.getMovementSpeed(), 1);
        assertTrue(mote.getPath().isEmpty());
        assertTrue(mote.getSensors().isEmpty());
        assertTrue(mote.isArrivedToDestination());
    }

    @Test
    void setters() {
        Environment environment = new Environment(new Characteristic[200][200], new GeoPosition(10, 10), 1, new HashMap<>(), new HashMap<>());
        Mote mote = new Mote(10L, 150, 150, 8, 12, new ArrayList<>(), 100, new Path(environment.getGraph()), 5, environment);

        // Maybe move this to some separate test for networkentity
        assertEquals(mote.getSF(), 12);
        mote.setSF(8);
        assertEquals(mote.getSF(), 8);
        assertThrows(IllegalArgumentException.class, () -> mote.setSF(20));
        assertEquals(mote.getSF(), 8);

        assertEquals(mote.getMovementSpeed(), 5);
        mote.setMovementSpeed(2.5);
        assertEquals(mote.getMovementSpeed(), 2.5);

        assertEquals(mote.getEnergyLevel(), 100);
        mote.setEnergyLevel(mote.getEnergyLevel() - 10);
        assertEquals(mote.getEnergyLevel(), 90);

        assertTrue(mote.getSensors().isEmpty());
        mote.setSensors(List.of(MoteSensor.CARBON_DIOXIDE));
        assertTrue(mote.getSensors().contains(MoteSensor.CARBON_DIOXIDE));

        assertTrue(mote.getPath().getWayPoints().isEmpty());
        mote.setPath(List.of(new GeoPosition(10.5, 9.8), new GeoPosition(10.2, 9.8)));
        List<GeoPosition> wayPoints = mote.getPath().getWayPoints();
        assertEquals(wayPoints.size(), 2);
        assertEquals(wayPoints.get(0), new GeoPosition(10.5, 9.8));
        assertEquals(wayPoints.get(1), new GeoPosition(10.2, 9.8));
        assertFalse(mote.isArrivedToDestination());
    }
}
