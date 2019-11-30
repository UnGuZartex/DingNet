package util;

import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.*;


/**
 * Class which represents a path of a mote.
 */
public class Path implements Iterable<GeoPosition> {
    // A list with waypoints of the path
    private List<GeoPosition> points;

    // The Graph which contains all the waypoints and connections
    private GraphStructure graphStructure;


    public Path(GraphStructure graph) {
        this(new ArrayList<>(), graph);
    }

    public Path(List<GeoPosition> points, GraphStructure graph) {
        this.points = new LinkedList<>(points);
        this.graphStructure = graph;
    }

    public List<GeoPosition> getWayPoints() {
        return this.points;
    }

    @NotNull
    public Iterator<GeoPosition> iterator() {
        return getWayPoints().iterator();
    }


    /**
     * Set the path to a given list of positions.
     * @param positions The list of GeoPositions.
     */
    public void setPath(List<GeoPosition> positions) {
        this.points = positions;
    }


    /**
     * Check if the path contains any waypoints.
     * @return True if no waypoints are present in the path.
     */
    public boolean isEmpty() {
        return points.isEmpty();
    }


    /**
     * Get the first position in the path, if present.
     * @return Either the first position of the path if present, otherwise an empty Optional.
     */
    public Optional<GeoPosition> getSource() {
        return isEmpty() ? Optional.empty() : Optional.of(points.get(0));
    }

    /**
     * Get the last position in the path, if present.
     * @return Either the last position of the path if present, otherwise an empty Optional.
     */
    public Optional<GeoPosition> getDestination() {
        return isEmpty() ? Optional.empty() : Optional.of(points.get(points.size()-1));
    }




    /**
     * Add a waypoint at the end of this path.
     * @param point The waypoint which is added at the end of the path.
     */
    public void addPosition(GeoPosition point) {
        this.points.add(point);
    }


    /**
     * Add a list of waypoints to the path
     * @param points A list of GeoPositions to be added.
     */
    public void addPositions(@NotNull List<GeoPosition> points) {
        this.points.addAll(points);
    }




    // NOTE: The following functions are only used during setup/saving of the configuration, not at runtime

    /**
     * Retrieve the used connections in this path.
     * @return A list of connection Ids of the connections in this path.
     */
    public List<Long> getConnectionsByID() {
        var connectionsMap = graphStructure.getConnections();
        List<Long> connections = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int index = i;

            long connectionId = connectionsMap.entrySet().stream()
                .filter(o -> o.getValue().getFrom() == graphStructure.getClosestWayPoint(points.get(index))
                    && o.getValue().getTo() == graphStructure.getClosestWayPoint(points.get(index+1)))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow();

            connections.add(connectionId);
        }

        return connections;
    }


    /**
     * Remove all the waypoints in the path from the given waypoint (including the given waypoint itself).
     * @param waypointId The Id of the waypoint from which the path should be shortened.
     */
    public void shortenPathFromWayPoint(long waypointId) {
        int index = 0;

        for (var wp : this.points) {
            if (wp.equals(graphStructure.getWayPoint(waypointId))) {
                break;
            }
            index++;
        }

        this.points = this.points.subList(0, index);
    }


    /**
     * Remove waypoints in the path from the given connection (including waypoints in this connection).
     * @param connectionId The Id of the connection.
     */
    public void shortenPathFromConnection(long connectionId) {
        var connection = graphStructure.getConnection(connectionId);
        int index = 0;

        for (int i = 0; i < points.size() - 1; i++) {
            if (graphStructure.getWayPoint(connection.getFrom()).equals(points.get(i))
                && graphStructure.getWayPoint(connection.getTo()).equals(points.get(i + 1))) {
                break;
            }
            index++;
        }

        this.points = this.points.subList(0, index);
    }
}
