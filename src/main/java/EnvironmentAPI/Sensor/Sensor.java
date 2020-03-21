package EnvironmentAPI.Sensor;

import EnvironmentAPI.PollutionEnvironment;
import iot.Environment;
import org.jxmapviewer.viewer.GeoPosition;

public class Sensor {

    private GeoPosition position;
    private double maxValue;
    private int NoiseRatio;
    private PollutionEnvironment poll;
    private Environment environment;



    public Sensor(PollutionEnvironment poll, Environment environment, GeoPosition position, double maxValue, int NoiseRatio){
        this.position = position;
        this.maxValue = maxValue;
        this.NoiseRatio = NoiseRatio;
        this.poll = poll;
        this.environment = environment;
    }


    public GeoPosition getPosition() {
        return position;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public int getNoiseRatio() {
        return NoiseRatio;
    }

    public double generateData(){
        return poll.getDensity(position, environment, maxValue);
    }
}
