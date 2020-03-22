package EnvironmentAPI.util;

import EnvironmentAPI.PollutionEnvironment;
import EnvironmentAPI.Sensor.Sensor;
import iot.Environment;
import org.jxmapviewer.viewer.GeoPosition;

/**
 * Class representing a factory for a sensor (just creates a sensor for now, but useful if advanced mechanics for sensors are needed)
 *
 * @author Yentl.kinoo@student.kuleuven.be
 */
public class SensorFactory {
    public static Sensor createSensor(PollutionEnvironment poll, Environment environment, GeoPosition position, double maxValue, int NoiseRatio){
        return new Sensor(poll, environment, position, maxValue, NoiseRatio);
    }
}
