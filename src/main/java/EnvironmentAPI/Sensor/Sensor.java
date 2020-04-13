package EnvironmentAPI.Sensor;

import EnvironmentAPI.PollutionEnvironment;
import iot.Environment;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.Random;

/**
 * Class defining a sensor to measure from the pollution environment.
 *
 * @author Yentl.kinoo@student.kuleuven.be
 */
public class Sensor {

    private GeoPosition position;
    private double maxValue;
    private int NoiseRatio;
    private PollutionEnvironment poll;
    private Environment environment;
    private Random noise;



    public Sensor(PollutionEnvironment poll, Environment environment, GeoPosition position, double maxValue, int NoiseRatio){
        this.position = position;
        this.maxValue = maxValue;
        this.NoiseRatio = NoiseRatio;
        this.poll = poll;
        this.environment = environment;
        noise = new Random();
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


    public void setPosition(GeoPosition position) {
        this.position = position;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public void setNoiseRatio(int noiseRatio) {
        this.NoiseRatio = noiseRatio;
    }


    /**
     * Get data from the pollutionenvironment "As If measured at the current time"
     * @return pollution at the sensors position affected by noise
     */
    public double generateData(){
        double value = NoiseRatio*noise.nextGaussian() + poll.getDensity(position, environment, maxValue);
        if (value >= maxValue) {
            return maxValue;
        }
        if (value < 0) {
            return 0;
        }
       return value;
    }


    @Override
    public String toString() {
        return  "Sensor @" + getPosition();
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
