package application.routing.heuristic;

import org.jxmapviewer.viewer.GeoPosition;
import util.MapHelper;


/**
 * A routing heuristic based on the shortest path.
 */
public class DistanceHeuristic implements RoutingHeuristic {
    @Override
    public double calculateHeuristic(HeuristicEntry entry) {
        GeoPosition begin = entry.graph.getWayPoint(entry.connection.getFrom());
        GeoPosition end = entry.graph.getWayPoint(entry.connection.getTo());

        return MapHelper.distance(begin, end);
    }
}
