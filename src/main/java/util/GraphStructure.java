package util;

import iot.Environment;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.*;
import java.util.stream.Collectors;

public class GraphStructure {
    private Map<Long, GeoPosition> wayPoints;
    private Map<Long, Connection> connections;

    private long newWayPointID;
    private long newConnectionID;


    public GraphStructure() {
        init(new HashMap<>(), new HashMap<>());
    }

    public GraphStructure(Map<Long, GeoPosition> wayPoints, Map<Long, Connection> connections) {
        init(wayPoints, connections);
    }

    private void init(Map<Long, GeoPosition> wayPoints, Map<Long, Connection> connections) {
        this.wayPoints = wayPoints;
        this.connections = connections;

        newWayPointID = wayPoints.keySet().stream()
            .max(Long::compare)
            .orElse(0L) + 1;
        newConnectionID = connections.keySet().stream()
            .max(Long::compare)
            .orElse(0L) + 1;
    }


    /**
     * Add a waypoint to the graph.
     * @param pos The waypoint.
     */
    public void addWayPoint(GeoPosition pos) {
        this.addWayPoint(this.newWayPointID, pos);
    }

    /**
     * Add a connection to the graph.
     * @param connection The connection.
     * @throws IllegalArgumentException if the source and destination of the connection is the same waypoint.
     */
    public void addConnection(Connection connection) {
        if (connection.getTo() == connection.getFrom()) {
            throw new IllegalArgumentException(String.format("Cannot have circular connections (Waypoint %d -> Waypoint %d).",
                connection.getFrom(), connection.getTo()));
        }
        this.addConnection(this.newConnectionID, connection);
    }


    /**
     * Add a waypoint to the graph.
     * @param id The Id of the new waypoint.
     * @param position The waypoint itself.
     * @throws IllegalStateException if a waypoint already exists with the given Id.
     */
    private void addWayPoint(long id, GeoPosition position) {
        if (wayPoints.containsKey(id)) {
            throw new IllegalStateException(String.format("WayPoint with id=%d exists already.", id));
        }

        wayPoints.put(id, position);
        if (id >= this.newWayPointID) {
            this.newWayPointID = id + 1;
        }
    }


    /**
     * Add a connection to the graph.
     * @param id The Id of the new connection.
     * @param connection The connection itself.
     * @throws IllegalStateException if a connection already exists with the given Id.
     */
    private void addConnection(long id, Connection connection) {
        if (connections.containsKey(id)) {
            throw new IllegalStateException(String.format("Connection with id=%d exists already.", id));
        } else if (!wayPoints.containsKey(connection.getFrom())) {
            throw new IllegalStateException(String.format("Could not add connection: waypoint with id=%d does not exist yet.", connection.getFrom()));
        } else if (!wayPoints.containsKey(connection.getTo())) {
            throw new IllegalStateException(String.format("Could not add connection: waypoint with id=%d does not exist yet.", connection.getTo()));
        }

        connections.put(id, connection);
        if (id >= this.newConnectionID) {
            this.newConnectionID = id + 1;
        }
    }



    // region Basic getters/setters

    public GeoPosition getWayPoint(long wayPointId) {
        return wayPoints.get(wayPointId);
    }

    public Map<Long, GeoPosition> getWayPoints() {
        return wayPoints;
    }


    public Connection getConnection(long connectionId) {
        return this.connections.get(connectionId);
    }


    public Map<Long, Connection> getConnections() {
        return connections;
    }

    // endregion




    /**
     * Gets the Id of the closest waypoint to a given location.
     * @param pos The location.
     * @return The Id of the closest waypoint, or a runtime exception in case no waypoints are present.
     * @throws IllegalStateException if no waypoints are present in the graph.
     */
    public long getClosestWayPoint(GeoPosition pos) {
        return this.getClosestWayPointWithDistance(pos).getLeft();
    }

    /**
     * Get the closest waypoint to a given location, if the closest waypoint is withing a specified range of that location.
     * @param pos The location.
     * @param range The maximum range between the closest waypoint and the position (expressed in km).
     * @return The closest waypoint Id if it is within the specified range, otherwise an empty Optional.
     * @throws IllegalStateException if no waypoints are present in the graph.
     */
    public Optional<Long> getClosestWayPointWithinRange(GeoPosition pos, double range) {
        var wp = this.getClosestWayPointWithDistance(pos);
        if (wp.getRight() <= range) {
            return Optional.of(wp.getLeft());
        }
        return Optional.empty();
    }

    /**
     * Get the closest waypoint to a given location, including the distance between the waypoint and the location.
     * @param pos The location.
     * @return A pair of the waypoint Id and the distance (in km) to it.
     * @throws IllegalStateException if no waypoints are present in the graph.
     */
    private Pair<Long, Double> getClosestWayPointWithDistance(GeoPosition pos) {
        Map<Long, Double> distances = new HashMap<>();

        for (var me : wayPoints.entrySet()) {
            distances.put(me.getKey(), MapHelper.distance(me.getValue(), pos));
        }

        return distances.entrySet().stream()
            .min(Comparator.comparing(Map.Entry::getValue))
            .map(o -> new Pair<>(o.getKey(), o.getValue()))
            .orElseThrow(IllegalStateException::new);
    }



    /**
     * Retrieve a list of connections which have the given waypoint as source.
     * @param wayPointId The waypoint at which the connections start.
     * @return A list of connections which all start at {@code wayPointId}.
     */
    public List<Connection> getOutgoingConnections(long wayPointId) {
        return connections.values().stream()
            .filter(c -> c.getFrom() == wayPointId)
            .collect(Collectors.toList());
    }


    /**
     * Retrieve a list of connection Ids which have the given waypoint as source.
     * @param wayPointId The waypiont at which the connections start.
     * @return A list of connection Ids which all start at {@code wayPointId}.
     */
    public List<Long> getOutgoingConnectionsById(long wayPointId) {
        return connections.entrySet().stream()
            .filter(c -> c.getValue().getFrom() == wayPointId)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Check whether a connection exists between 2 waypoints.
     * @param fromWayPointId The source waypoint Id.
     * @param toWayPointId The destination waypoint Id.
     * @return True if a connection exists.
     */
    public boolean connectionExists(long fromWayPointId, long toWayPointId) {
        return getConnection(fromWayPointId, toWayPointId).isPresent();
    }


    /**
     * Get the connection which starts and ends at the given waypoint Ids.
     * @param fromWayPointId The waypoint Id at which the connection starts.
     * @param toWayPointId The waypoint Id at which the connection ends.
     * @return Either the connection which has the right source and destination, or an empty Optional if no such connection exists.
     */
    private Optional<Connection> getConnection(long fromWayPointId, long toWayPointId) {
        return connections.values().stream()
            .filter(c -> c.getFrom() == fromWayPointId && c.getTo() == toWayPointId)
            .findFirst();
    }


    /**
     * Delete a waypoint in the graph.
     * Note: this also shortens the paths of motes which make use of this waypoint.
     * @param wayPointId The Id of the waypoint to be deleted.
     * @param environment The environment of the simulation.
     */
    public void deleteWayPoint(long wayPointId, Environment environment) {
        wayPoints.remove(wayPointId);
        var connToDelete  = connections.entrySet().stream()
            .filter(o -> o.getValue().getTo() == wayPointId || o.getValue().getFrom() == wayPointId)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        for (var conn : connToDelete) {
            connections.remove(conn);
        }

        // Make sure to also delete part of the paths of motes which use this waypoint
        environment.removeWayPointFromMotes(wayPointId);
    }


    /**
     * Delete a connection which starts at {@code fromWayPointId} and ends at {@code toWayPointId}.
     * Note: this also shortens the paths of motes which make use of this connection.
     * @param fromWayPointId The waypoint Id at which the connection starts.
     * @param toWayPointId The waypoint Id at which the connection ends.
     * @param environment The environment of the simulation.
     */
    public void deleteConnection(long fromWayPointId, long toWayPointId, Environment environment) {
        var possibleConnections = connections.entrySet().stream()
            .filter(o -> o.getValue().getTo() == toWayPointId && o.getValue().getFrom() == fromWayPointId)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        if (possibleConnections.size() == 0) {
            return;
        }
        assert possibleConnections.size() == 1;

        environment.removeConnectionFromMotes(possibleConnections.get(0));
        connections.remove(possibleConnections.get(0));
    }
}
