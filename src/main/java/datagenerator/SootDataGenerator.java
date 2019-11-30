package datagenerator;

import util.Pair;

import java.time.LocalTime;
import java.util.Random;

/**
 * A class representing a sensor for soot.
 */
public class SootDataGenerator implements SensorDataGenerator {

    private static final int DEFAULT_SEED = 1;
    private static Random random = new Random(DEFAULT_SEED);

    public static double generateData(double x, double y) {
        if (x < 210 && y < 230)
            return (double) 97 - 10 + (x + y) / 250 + 0.3 * random.nextGaussian();
        else if (x < 1100 && y < 1100)
            return 98 - 10 + Math.log10((x + y) / 50) + 0.3 * random.nextGaussian();
        else if (x < 1400 && y < 1700)
            return 95 - 4 + 3 * Math.cos(Math.PI * (x + y) / (150 * 8)) + 1.5 * Math.sin(Math.PI * (x + y) / (150 * 6)) + 0.3 * random.nextGaussian();
        else
            return 85 - 2 + (x + y) / 200 + 0.1 * random.nextGaussian();
    }
    public double nonStaticDataGeneration(double x, double y) {
        return ParticulateMatterDataGenerator.generateData(x,y);
    }

    @Override
    public void reset() {
        random = new Random(DEFAULT_SEED);
    }

    /**
     * A function generating senor data for soot.
     * @param x The x position of the measurement.
     * @param y The y position of the measurement.
     * @param time The time of the measurement.
     * @return A measurement of soot at the given position and time.
     */
    public byte[] generateData(int x, int y, LocalTime time) {
        double result = SootDataGenerator.generateData(x, y);
        return new byte[]{(byte)Math.floorMod((int) Math.round(result),255)};
    }
    public byte[] generateData(Pair<Integer, Integer> pos, LocalTime time) {
        return this.generateData(pos.getLeft(), pos.getRight(), time);
    }
}
