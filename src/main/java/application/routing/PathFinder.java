package application.routing;

import org.jxmapviewer.viewer.GeoPosition;
import util.GraphStructure;

import java.util.List;

public interface PathFinder {
    /**
     * Retrieve a path from a given starting position to an end destination using a graph with available connections.
     * @param graph The graph containing all the connections.
     * @param begin The starting position.
     * @param end The destination position.
     * @return A list of positions containing the path to the destination
     * @throws RuntimeException When no path existed between the starting and ending position.
     */
    List<GeoPosition> retrievePath(GraphStructure graph, GeoPosition begin, GeoPosition end);
}
