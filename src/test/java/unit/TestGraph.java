package unit;

import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;
import util.Connection;
import util.GraphStructure;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestGraph {

    @Test
    void addWayPoint() {
        GraphStructure graph = new GraphStructure();

        graph.addWayPoint(new GeoPosition(1, 1));
        graph.addWayPoint(new GeoPosition(2, 2));

        Collection<GeoPosition> points = graph.getWayPoints().values();
        assertTrue(points.contains(new GeoPosition(1,1)));
        assertTrue(points.contains(new GeoPosition(2,2)));
    }

    @Test
    void addConnection() {
        GraphStructure graph = new GraphStructure();


        graph.addWayPoint(new GeoPosition(1, 1));
        graph.addWayPoint(new GeoPosition(2, 2));

        Map<Long, GeoPosition> wayPoints = graph.getWayPoints();
        Long idPoint1 = wayPoints.entrySet().stream().filter(e -> e.getValue().equals(new GeoPosition(1,1))).map(Map.Entry::getKey).findFirst().orElse(-1L);
        Long idPoint2 = wayPoints.entrySet().stream().filter(e -> e.getValue().equals(new GeoPosition(2,2))).map(Map.Entry::getKey).findFirst().orElse(-1L);
        graph.addConnection(new Connection(idPoint1, idPoint2));

        assertFalse(graph.getConnections().isEmpty());
        assertEquals(graph.getConnections().values().iterator().next().getFrom(), idPoint1);
        assertEquals(graph.getConnections().values().iterator().next().getTo(), idPoint2);
    }

    @Test
    void addConnectionFail1() {
        GraphStructure graph = new GraphStructure();


        graph.addWayPoint(new GeoPosition(1, 1));
        graph.addWayPoint(new GeoPosition(2, 2));

        assertThrows(IllegalArgumentException.class, () -> graph.addConnection(new Connection(1, 1)));
        assertThrows(IllegalArgumentException.class, () -> graph.addConnection(new Connection(2, 2)));
    }

    @Test
    void addConnectionFail2() {
        GraphStructure graph = new GraphStructure();


        graph.addWayPoint(new GeoPosition(1, 1));
        graph.addWayPoint(new GeoPosition(2, 2));
        graph.addWayPoint(new GeoPosition(5, 5));

        graph.addConnection(new Connection(2,3));
        assertThrows(IllegalStateException.class, () -> graph.addConnection(new Connection(1, 4)));
        assertThrows(IllegalStateException.class, () -> graph.addConnection(new Connection(3, 0)));
        graph.addConnection(new Connection(1,2));
    }

    @Test
    void initialization() {
        Map<Long, GeoPosition> wayPoints = new HashMap<>();
        wayPoints.put(1L, new GeoPosition(50,50));
        wayPoints.put(5L, new GeoPosition(5,5));

        GraphStructure graph = new GraphStructure(wayPoints, new HashMap<>());

        assertEquals(graph.getWayPoint(1L), new GeoPosition(50,50));
        assertEquals(graph.getWayPoint(5L), new GeoPosition(5,5));
        assertNull(graph.getWayPoint(2L));

        graph.addWayPoint(new GeoPosition(10,10));

        assertEquals(graph.getWayPoint(6L), new GeoPosition(10,10));
        assertNull(graph.getWayPoint(2L));
    }

    @Test
    void Proximity() {
        Map<Long, GeoPosition> wayPoints = new HashMap<>();
        wayPoints.put(1L, new GeoPosition(50,50));
        wayPoints.put(2L, new GeoPosition(5,5));
        wayPoints.put(3L, new GeoPosition(35, 35));
        wayPoints.put(4L, new GeoPosition(48, 48));

        GraphStructure graph = new GraphStructure(wayPoints, new HashMap<>());

        assertEquals(graph.getClosestWayPoint(new GeoPosition(51,51)), 1L);
        assertEquals(graph.getClosestWayPoint(new GeoPosition(15,5)), 2L);
        assertEquals(graph.getClosestWayPoint(new GeoPosition(48,49)), 4L);
        assertEquals(graph.getClosestWayPoint(new GeoPosition(32,38)), 3L);
    }

    @Test
    void OutgoingConnections() {
        Map<Long, GeoPosition> wayPoints = new HashMap<>();
        wayPoints.put(1L, new GeoPosition(50,50));
        wayPoints.put(2L, new GeoPosition(5,5));
        wayPoints.put(3L, new GeoPosition(35, 35));
        wayPoints.put(4L, new GeoPosition(48, 48));

        Map<Long, Connection> connections = new HashMap<>();
        connections.put(1L, new Connection(1L, 2L));
        connections.put(2L, new Connection(1L, 3L));
        connections.put(3L, new Connection(4L, 2L));
        connections.put(4L, new Connection(4L, 1L));
        connections.put(5L, new Connection(3L, 4L));

        GraphStructure graph = new GraphStructure(wayPoints, connections);

        List<Connection> toCheck = graph.getOutgoingConnections(1L);
        assertFalse(toCheck.isEmpty());
        assertTrue(toCheck.contains(new Connection(1L, 2L)));
        assertTrue(toCheck.contains(new Connection(1L, 3L)));

        toCheck = graph.getOutgoingConnections(2L);
        assertTrue(toCheck.isEmpty());

        toCheck = graph.getOutgoingConnections(4L);
        assertTrue(toCheck.contains(new Connection(4L, 1L)));
        assertTrue(toCheck.contains(new Connection(4L, 2L)));

        List<Long> toCheck2 = graph.getOutgoingConnectionsById(3L);
        assertFalse(toCheck2.isEmpty());
        assertTrue(toCheck2.contains(5L));
    }
}
