package datagenerator.GeneralSensor;

import datagenerator.SensorDataGenerator;
import util.Pair;

import java.time.LocalTime;

public abstract class Sensor implements SensorDataGenerator {
    protected Sensor() {
    }

    public byte[] generateData(int x, int y, LocalTime time) {
        return new byte[0];
    }

    public byte[] generateData(Pair<Integer, Integer> pos, LocalTime time) {
        return generateData(pos.getLeft(), pos.getRight(), time);
    }

    public double nonStaticDataGeneration(double x, double y) {
        return 0;
    }

    public void reset() {

    }

}
