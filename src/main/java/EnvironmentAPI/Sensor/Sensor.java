package EnvironmentAPI.Sensor;

import EnvironmentAPI.PollutionEnvironment;
import EnvironmentAPI.util.OpenSimplex2S;
import iot.Environment;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.Random;

public class Sensor {

    private GeoPosition position;
    private double maxValue;
    private int NoiseRatio;
    private PollutionEnvironment poll;
    private Environment environment;
    private OpenSimplex2S noise;



    public Sensor(PollutionEnvironment poll, Environment environment, GeoPosition position, double maxValue, int NoiseRatio){
        this.position = position;
        this.maxValue = maxValue;
        this.NoiseRatio = NoiseRatio;
        this.poll = poll;
        this.environment = environment;
        Random rand = new Random();
        this.noise = new OpenSimplex2S(rand.nextLong());
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

    public Environment getEnvironment() { return environment;}

    public double generateData(){
        return NoiseRatio*noise.noise3_XYBeforeZ(position.getLatitude(), position.getLongitude(), environment.getClock().getTime().toSecondOfDay()) + poll.getDensity(position, environment, maxValue);
    }
}
