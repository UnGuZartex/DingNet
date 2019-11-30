package datagenerator;

import iot.SimulationRunner;
import util.Converter;
import util.MapHelper;
import util.Pair;

import java.time.LocalTime;

public class GPSDataGenerator implements SensorDataGenerator {

    public GPSDataGenerator() {}

    @Override
    public byte[] generateData(int x, int y, LocalTime time) {
        MapHelper mapHelper = SimulationRunner.getInstance().getEnvironment().getMapHelper();
        return Converter.toByteArray(mapHelper.toGeoPosition(x, y));
    }

    public byte[] generateData(Pair<Integer, Integer> pos, LocalTime time) {
        return this.generateData(pos.getLeft(), pos.getRight(), time);
    }

    public double nonStaticDataGeneration(double x, double y) {
        return 0.0;
    }

    @Override
    public void reset() {

    }
}
