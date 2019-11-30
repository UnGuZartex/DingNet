package datagenerator;

import util.Pair;

import java.time.LocalTime;
import java.util.Random;

/**
 * A class representing a sensor for ozone.
 */
public class OzoneDataGenerator implements SensorDataGenerator {

    private static final int DEFAULT_SEED = 1;
    private static Random random = new Random(DEFAULT_SEED);

    @Override
    public void reset() {
        random = new Random(DEFAULT_SEED);
    }

    public static double generateData(double x, double y) {
        if (x < 200 && y < 200)
            return (double) 97 - 30 + (x + y) / 250 + 0.3 * random.nextGaussian();
        else if (x < 1000 && y < 1000)
            return 98 - 30 + Math.log10((x + y) / 50) + 0.3 * random.nextGaussian();
        else if (x < 1200 && y < 1200)
            return 95 - 24.5 + 3 * Math.cos(Math.PI * (x + y) / (150 * 8)) + 0.3 * random.nextGaussian();
        else
            return 85 - 24 + (x + y) / 200 + 0.1 * random.nextGaussian();
    }
    public double nonStaticDataGeneration(double x, double y) {
        return ParticulateMatterDataGenerator.generateData(x,y);
    }
    /**
     * A function generating senor data for ozone.
     * @param x The x position of the measurement.
     * @param y The y position of the measurement.
     * @param time The time of the measurement.
     * @return A measurement of ozone at the given position and time.
     */
    public byte[] generateData(int x, int y, LocalTime time) {
        double result = OzoneDataGenerator.generateData(x,  y);
        return new byte[]{(byte)Math.floorMod((int) Math.round(result),255)};
    }
    public byte[] generateData(Pair<Integer, Integer> pos, LocalTime time) {
        return this.generateData(pos.getLeft(), pos.getRight(), time);
    }
}
