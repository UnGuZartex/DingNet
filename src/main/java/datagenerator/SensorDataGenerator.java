package datagenerator;

import util.Pair;

import java.time.LocalTime;

/**
 * An abstract class representing all sensor data generators
 */
public interface SensorDataGenerator {
    /**
     * Generates sensor data based on location and time.
     * @param x x-position of measurement.
     * @param y y-position of measurement.
     * @param time time of measurement.
     * @return sensor data based on location and time.
     */
    byte[] generateData(int x, int y, LocalTime time);
    byte[] generateData(Pair<Integer, Integer> pos, LocalTime time);
    double nonStaticDataGeneration(double x, double y);

    void reset();
}
