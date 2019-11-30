package util.xml;

import org.jxmapviewer.viewer.GeoPosition;
import util.Connection;

import java.util.HashMap;
import java.util.Map;

public class IdRemapping {

    private Map<Long, Long> IdMappingWayPoints;
    private Map<Long, Long> IdMappingConnections;

    private Map<Long, GeoPosition> wayPoints;
    private Map<Long, Connection> connections;

    private long newWayPointId;
    private long newConnectionId;


    public IdRemapping() {
        this.reset();
    }

    /**
     * Add a waypoint to the remapping.
     * @param originalId The Id which the waypoint currently has.
     * @param pos The waypoint itself.
     * @return The new remapped Id for the given waypoint.
     */
    public long addWayPoint(long originalId, GeoPosition pos) {
        IdMappingWayPoints.put(originalId, newWayPointId);
        wayPoints.put(newWayPointId, pos);

        return newWayPointId++;
    }


    /**
     * Add a connection to the remapping.
     * @param originalId The Id which the connection currently has.
     * @param conn The connection itself.
     * @return The new remapped Id for the given connection.
     */
    public long addConnection(long originalId, Connection conn) {
        IdMappingConnections.put(originalId, newConnectionId);
        connections.put(newConnectionId, conn);

        return newConnectionId++;
    }


    /**
     * Get the new remapped Id for a waypoint.
     * @param originalId The original Id of the waypoint.
     * @return The new remapped Id for the given original Id.
     */
    public long getNewWayPointId(long originalId) {
        return IdMappingWayPoints.get(originalId);
    }

    /**
     * Get the new remapped Id for a connection.
     * @param originalId The original Id of the connection.
     * @return The new remapped Id for the given original Id.
     */
    public long getNewConnectionId(long originalId) {
        return IdMappingConnections.get(originalId);
    }


    /**
     * Get the waypoint corresponding to its new remapped Id.
     * @param newId The new Id of the waypoint.
     * @return The waypoint corresponding to the new Id.
     */
    public GeoPosition getWayPointWithNewId(long newId) {
        return wayPoints.get(newId);
    }

    /**
     * Get the connection corresponding to its new remapped Id.
     * @param originalId The new Id of the connection.
     * @return The connection corresponding to the new Id.
     */
    public GeoPosition getWayPointWithOriginalId(long originalId) {
        return wayPoints.get(IdMappingWayPoints.get(originalId));
    }


    /**
     * Get the connection corresponding to its original Id.
     * @param originalId The original Id of the connection.
     * @return The connection corresponding to the original Id.
     */
    public Connection getConnectionWithOriginalId(long originalId) {
        return connections.get(IdMappingConnections.get(originalId));
    }

    /**
     * Get all the waypoints with their new remapped Ids.
     * @return A map with the new Ids mapped to the waypoints.
     */
    public Map<Long, GeoPosition> getWayPoints() {
        return wayPoints;
    }

    /**
     * Get all the connections with their new remapped Ids.
     * @return A map with the new Ids mapped to the connections.
     */
    public Map<Long, Connection> getConnections() {
        return connections;
    }


    /**
     * Reset all the mappings.
     */
    public void reset() {
        IdMappingWayPoints = new HashMap<>();
        IdMappingConnections = new HashMap<>();
        wayPoints = new HashMap<>();
        connections = new HashMap<>();

        newWayPointId = 1;
        newConnectionId = 1;
    }
}
