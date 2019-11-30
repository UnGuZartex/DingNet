package datagenerator.iaqsensor;

import com.uchuhimo.konf.BaseConfig;
import com.uchuhimo.konf.Config;
import datagenerator.SensorDataGenerator;
import iot.Environment;
import util.Pair;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 *      -------------
 *      | 1 | 2 | 3 |
 *      -------------
 *      | 4 | 5 | 6 |
 *      -------------
 *      | 7 | 8 | 9 |
 *      -------------
 */
public class IAQDataGeneratorSingleton implements SensorDataGenerator {

    private static final String configFile = "/sensorsConfigurations/sensorConfig.toml";

    private static IAQDataGeneratorSingleton instance;

    private final int row;

    private final int columns;

    private final int width;

    private final int height;

    private final AirQualityLevel defaultLevel;

    private final TimeUnit timeUnit;

    private Map<Integer, List<Cell>> map;

    private IAQDataGeneratorSingleton() {
        Config config = new BaseConfig();
        config.addSpec(IAQSensorConfigSpec.SPEC);
        config = config.from().toml.inputStream(this.getClass().getResourceAsStream(configFile));
        row = config.get(IAQSensorConfigSpec.row);
        columns = config.get(IAQSensorConfigSpec.columns);
        width = Environment.getMapWidth();
        height = Environment.getMapHeight();
        defaultLevel = config.get(IAQSensorConfigSpec.defaultLevel);
        timeUnit = config.get(IAQSensorConfigSpec.timeUnit);
        map = config.get(IAQSensorConfigSpec.cells).stream().collect(Collectors.groupingBy(Cell::getCellNumber));
        map.forEach((e, v) -> v.sort((c1, c2) -> Double.compare(c2.getFromTime(), c1.getFromTime())));
    }

    public static IAQDataGeneratorSingleton getInstance() {
        if (instance == null) {
            instance = new IAQDataGeneratorSingleton();
        }
        return instance;
    }

    @Override
    public byte[] generateData(int x, int y, LocalTime time) {
        //`(height - y)` because in the simulator environment the origin is in the bottom left corner
        int moteRow = (height - y) / (height/row);
        int moteCol = x/ (width/columns);
        int cell = moteRow * columns + moteCol;
        var level = map.getOrDefault(cell, new LinkedList<>()).stream()
            .filter(c -> c.getFromTime() < timeUnit.convertFromNano(time.toNanoOfDay()))
            .findFirst()// the list of cell is ordered for time
            .map(Cell::getLevel)
            .orElse(defaultLevel)
            .getCod();
        return new byte[]{level};
    }

    @Override
    public byte[] generateData(Pair<Integer, Integer> pos, LocalTime time) {
        return generateData(pos.getLeft(), pos.getRight(), time);
    }

    @Override
    public double nonStaticDataGeneration(double x, double y) {
        return 0.0;
    }

    // Debugging function
    public int calcSquare(int x, int y) {
        int moteRow = (height - y) / (height/row);
        int moteCol = x/ (width/columns);
        return moteRow * columns + moteCol;
    }


    @Override
    public void reset() {

    }
}
