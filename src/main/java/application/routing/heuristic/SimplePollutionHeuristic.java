package application.routing.heuristic;

import EnvironmentAPI.PollutionEnvironment;
import application.pollution.PollutionGrid;
import org.jxmapviewer.viewer.GeoPosition;
import util.MapHelper;


/**
 * A simple routing heuristic which also takes the pollution over a given connection into account.
 */
public class SimplePollutionHeuristic implements RoutingHeuristic {
    private final PollutionGrid pollutionGrid;
    private final PollutionEnvironment env;

    public SimplePollutionHeuristic(PollutionGrid pollutionGrid, PollutionEnvironment env) {
        this.pollutionGrid = pollutionGrid;
        this.env = env;
    }

    @Override
    public double calculateHeuristic(HeuristicEntry entry) {
        GeoPosition begin = entry.graph.getWayPoint(entry.connection.getFrom());
        GeoPosition end = entry.graph.getWayPoint(entry.connection.getTo());

      //  double pollutionValue = this.pollutionGrid.getPollutionLevel(MapHelper.meanPosition(begin, end)).getPollutionFactor();
        double pollutionValue = this.env.getDataBetweenPoints(begin,end, 0.2);

        double factor = (0.0 <= pollutionValue && pollutionValue < 0.2) ? 1 :
                        (0.2 <= pollutionValue && pollutionValue < 0.4) ? 2 :
                        (0.4 <= pollutionValue && pollutionValue < 0.6) ? 3 : 10;

        // The lower the pollution level, the better the heuristic
        return factor * MapHelper.distance(begin, end);
    }
}
