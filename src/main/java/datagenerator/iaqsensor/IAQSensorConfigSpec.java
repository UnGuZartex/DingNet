package datagenerator.iaqsensor;

import com.uchuhimo.konf.ConfigSpec;
import com.uchuhimo.konf.RequiredItem;

import java.util.List;

public class IAQSensorConfigSpec {

    public static final ConfigSpec SPEC = new ConfigSpec("IAQSensor");

    public static final RequiredItem<Integer> row = new RequiredItem<>(SPEC, "row") {};

    public static final RequiredItem<Integer> columns = new RequiredItem<>(SPEC, "columns") {};

    public static final RequiredItem<Integer> width = new RequiredItem<>(SPEC, "width") {};

    public static final RequiredItem<Integer> height = new RequiredItem<>(SPEC, "height") {};

    public static final RequiredItem<AirQualityLevel> defaultLevel = new RequiredItem<>(SPEC, "defaultLevel") {};

    public static final RequiredItem<TimeUnit> timeUnit= new RequiredItem<>(SPEC, "timeUnit") {};

    public static final RequiredItem<List<Cell>> cells = new RequiredItem<>(SPEC, "cell") {};


}
