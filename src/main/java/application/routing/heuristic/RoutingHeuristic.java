package application.routing.heuristic;

import org.jxmapviewer.viewer.GeoPosition;
import util.Connection;
import util.GraphStructure;


/**
 * An interface used to specify routing heuristics.
 */
public interface RoutingHeuristic {

    /**
     * Calculates a heuristic value based on the given entry.
     * @param entry The entry for the heuristic function on which the heuristic value is calculated.
     * @return A double according to the heuristic (the lower the better).
     */
    double calculateHeuristic(HeuristicEntry entry);

    /**
     * Data class used to store data to calculate a heuristic value associated with that data.
     */
    class HeuristicEntry {
        public GraphStructure graph;
        public Connection connection;
        public GeoPosition destination;

        public HeuristicEntry(GraphStructure graph, Connection connection, GeoPosition destination) {
            this.graph = graph;
            this.connection = connection;
            this.destination = destination;
        }
    }
}
