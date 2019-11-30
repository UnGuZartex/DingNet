package application.pollution;

import org.jxmapviewer.viewer.GeoPosition;
import util.MapHelper;
import util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PollutionGrid {
    // FIXME synchronized is necessary here, otherwise concurrent modification exceptions are thrown
    //  (even though the GUI updating should happen synchronously with invokeAndWait)

    // The pollution measurements: for each device, the most recent measurement (location + pollution level) is stored
    private Map<Long, Pair<GeoPosition, PollutionLevel>> pollutionMeasurements;


    public PollutionGrid() {
        pollutionMeasurements = new HashMap<>();
    }


    /**
     * Add a measurement to the pollution grid
     * @param deviceEUI The device from which the measurement originated.
     * @param position The position at which the measurement was taken.
     * @param level The pollution level measured by the device in the given position.
     */
    public void addMeasurement(long deviceEUI, GeoPosition position, PollutionLevel level) {
        synchronized (this) {
            this.pollutionMeasurements.put(deviceEUI, new Pair<>(position, level));
        }
    }

    /**
     * Calculates the pollution level in a given position (approximate level based on available measurements)
     * @param position The position at which the pollution should be calculated.
     * @return The level of pollution at {@code position}.
     */
    public PollutionLevel getPollutionLevel(GeoPosition position) {
        synchronized (this) {
            var pollutionAtPosition = pollutionMeasurements.values().stream()
                .filter(o -> o.getLeft().equals(position))
                .map(Pair::getRight)
                .findFirst();
            if (pollutionAtPosition.isPresent()) {
                return pollutionAtPosition.get();
            }

            // Calculate some mean pollution based on the distance of other measurements
            // NOTE: this does not take the time of the measurement into account, only the distance
            List<Pair<Double, PollutionLevel>> distances = pollutionMeasurements.values().stream()
                .map(e -> new Pair<>(MapHelper.distance(e.getLeft(), position), e.getRight()))
                .collect(Collectors.toList());

            return PollutionLevel.getMediumPollution(distances);
        }
    }

    /**
     * Removes all stored pollution measurements.
     */
    public void clean() {
        pollutionMeasurements = new HashMap<>();
    }
}
