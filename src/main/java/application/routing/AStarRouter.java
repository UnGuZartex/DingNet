package application.routing;

import application.routing.heuristic.RoutingHeuristic;
import application.routing.heuristic.RoutingHeuristic.HeuristicEntry;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.GeoPosition;
import util.GraphStructure;

import java.util.*;
import java.util.stream.Collectors;


/**
 * An class which implements the A* routing algorithm, assuming the used heuristic is consistent.
 */
public class AStarRouter implements PathFinder {

    // The maximum amount of distance the closest waypoint should be to a given GeoPosition (in km)
    @SuppressWarnings("FieldCanBeLocal")
    private final double DISTANCE_THRESHOLD_POSITIONS = 0.05;

    // The heuristic used in the A* algorithm
    private RoutingHeuristic heuristic;


    public AStarRouter(RoutingHeuristic heuristic) {
        this.heuristic = heuristic;
    }


    @Override
    public List<GeoPosition> retrievePath(GraphStructure graph, GeoPosition begin, GeoPosition end) {
        long beginWaypointId = graph.getClosestWayPointWithinRange(begin, DISTANCE_THRESHOLD_POSITIONS)
            .orElseThrow(() -> new IllegalStateException("The mote position retrieved from the message is not located at a waypoint."));
        long endWaypointId = graph.getClosestWayPointWithinRange(end, DISTANCE_THRESHOLD_POSITIONS)
            .orElseThrow(() -> new IllegalStateException("The destination position retrieved from the message is not located at a waypoint."));

        Set<Long> visitedConnections = new HashSet<>();

        PriorityQueue<FringeEntry> fringe = new PriorityQueue<>();
        // Initialize the fringe by adding the first outgoing connections
        graph.getConnections().entrySet().stream()
            .filter(entry -> entry.getValue().getFrom() == beginWaypointId)
            .forEach(entry -> {
                fringe.add(new FringeEntry(
                    List.of(entry.getKey()),
                    this.heuristic.calculateHeuristic(new HeuristicEntry(graph, entry.getValue(), end))
                ));
                visitedConnections.add(entry.getKey());
            });


        // Actual A* algorithm
        while (!fringe.isEmpty()) {
            FringeEntry current = fringe.poll();
            long lastWaypointId = graph.getConnection(current.getLastConnectionId()).getTo();

            // Are we at the destination?
            if (lastWaypointId == endWaypointId) {
                return this.getPath(current.connections, graph);
            }


            // Explore the different outgoing connections from the last connection in the list
            // -> Add the new possible paths (together with their new heuristic values) to the fringe
            graph.getOutgoingConnectionsById(lastWaypointId).stream()
                .filter(connId -> !visitedConnections.contains(connId)) // Filter out connections which we have already considered (since these were visited in a better path first)
                .forEach(connId -> {
                    List<Long> extendedPath = new ArrayList<>(current.connections);
                    extendedPath.add(connId);

                    double newHeuristicValue = current.heuristicValue
                        + this.heuristic.calculateHeuristic(new HeuristicEntry(graph, graph.getConnection(connId), end));

                    fringe.add(new FringeEntry(extendedPath, newHeuristicValue));
                    visitedConnections.add(connId);
                });
        }

        throw new RuntimeException(String.format("Could not find a path from {%s} to {%s}", begin.toString(), end.toString()));
    }


    /**
     * Convert a list of connection Ids to a list of the respective GeoPositions of those connections.
     * @param connectionIds The list of connections Ids which are used for the conversion.
     * @param graph The graph which contains all the connections and waypoints.
     * @return A list of GeoPositions which correspond to the connections in {@code connectionIds}.
     */
    private List<GeoPosition> getPath(List<Long> connectionIds, GraphStructure graph) {
        List<GeoPosition> points = connectionIds.stream()
            .map(o -> graph.getConnections().get(o).getFrom())
            .map(graph::getWayPoint)
            .collect(Collectors.toList());

        // Don't forget the final waypoint
        long lastWaypointId = graph.getConnections().get(connectionIds.get(connectionIds.size()-1)).getTo();
        points.add(graph.getWayPoint(lastWaypointId));

        return points;
    }


    /**
     * Class used in the priority queue, providing an order for the A* algorithm based on the
     * accumulated heuristic values for the evaluated path.
     */
    private static class FringeEntry implements Comparable<FringeEntry> {
        List<Long> connections;
        double heuristicValue;

        FringeEntry(List<Long> connections, double heuristicValue) {
            this.connections = connections;
            this.heuristicValue = heuristicValue;
        }

        long getLastConnectionId() {
            return connections.get(connections.size() - 1);
        }

        @Override
        public int compareTo(@NotNull FringeEntry fringeEntry) {
            return Double.compare(this.heuristicValue, fringeEntry.heuristicValue);
        }
    }
}
