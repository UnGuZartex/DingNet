package application.pollution;

import util.Pair;

import java.util.Comparator;
import java.util.List;


/**
 * Class used to track the pollution level, specified by a value in the range [0,1] (good to bad respectively)
 */
public class PollutionLevel {
    // The pollution level
    private double level;

    public PollutionLevel(double level) {
        if (level < 0 || level > 1) {
            throw new IllegalArgumentException("The pollution level should be in the range [0,1]");
        }
        this.level = level;
    }

    public double getPollutionFactor() {
        return level;
    }


    public static PollutionLevel getMediumPollution(List<Pair<Double, PollutionLevel>> measurements) {
        return getMediumPollution(measurements, 1);
    }

    /**
     * Calculates the medium pollution level based on the provided air quality measurements
     * and their distance to the position in question.
     * @param measurements A list of air quality measurements and their distances to the desired position.
     * @param amtPoints The amount of measurement points that are used to calculate the mean pollution.
     * @return A pollution level for the position.
     */
    public static PollutionLevel getMediumPollution(List<Pair<Double, PollutionLevel>> measurements, int amtPoints) {
        if (measurements.isEmpty()) {
            // In case no measurements are present yet, choose a default value of 0
            return new PollutionLevel(0);
        }

        measurements.sort(Comparator.comparing(Pair::getLeft));
        if (measurements.size() > amtPoints) {
            measurements = measurements.subList(0, amtPoints);
        }

        /*
         Formula used (by example):
            Point 1 -> distance = 100, pollutionLevel = 0.9
            Point 2 -> distance = 500, pollutionLevel = 0.3
            Point 3 -> distance = 400, pollutionLevel = 0.2

            Resulting pollutionLevel =
                0.9 * (1 / 100) / ((1/100) + (1/500) + (1/400)) +
                0.3 * (1 / 500) / ((1/100) + (1/500) + (1/400)) +
                0.2 * (1 / 400) / ((1/100) + (1/500) + (1/400))
                                     ~= 0.68
         */

        double totalInverted = measurements.stream()
            .mapToDouble(o -> 1 / o.getLeft())
            .sum();

        double resultingPollutionLevel = measurements.stream()
            .mapToDouble(e -> (1 / e.getLeft()) / totalInverted  * e.getRight().getPollutionFactor())
            .sum();

        return new PollutionLevel(resultingPollutionLevel);
    }
}
