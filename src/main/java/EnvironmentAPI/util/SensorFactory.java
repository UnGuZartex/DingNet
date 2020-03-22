package EnvironmentAPI.util;

import EnvironmentAPI.PollutionEnvironment;
import EnvironmentAPI.Sensor.Sensor;
import iot.Environment;
import org.jxmapviewer.viewer.GeoPosition;


public class SensorFactory {
    public static Sensor createSensor(PollutionEnvironment poll, Environment environment, GeoPosition position, double maxValue, int NoiseRatio){
        return new Sensor(poll, environment, position, maxValue, NoiseRatio);
    }
}
