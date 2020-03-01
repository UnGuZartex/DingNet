package EnvironmentAPI.GeneralSensor;


import EnvironmentAPI.util.EnvSettings;
import EnvironmentAPI.util.OpenSimplex2S;
import datagenerator.iaqsensor.TimeUnit;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.Random;


public abstract class Sensor{
    private GeoPosition position;


    protected TimeUnit timeUnit;



    protected double maxValue;

    protected Random rand = new Random();
    protected OpenSimplex2S noise;

    protected static int noiseMultiplicator = EnvSettings.NOISE_RATIO;




    protected int NoiseRatio;

    protected Sensor(GeoPosition position, double maxValue, TimeUnit unit, int NoiseRatio) {
        this.position = position;
        this.maxValue = maxValue;
        this.timeUnit = unit;
        noise = new OpenSimplex2S(rand.nextLong());
        this.NoiseRatio = NoiseRatio;
    }

    public void setPosition(GeoPosition position) {
        this.position = position;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }
    public void setNoiseRatio(int noiseRatio) {
        NoiseRatio = noiseRatio;
    }
    public int getNoiseRatio() {
        return NoiseRatio;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
    public static void setNoiseMultiplicator(int noiseRatio) {
        noiseMultiplicator = noiseRatio * EnvSettings.NOISE_RATIO;
    }

    public abstract double generateData(long timeinNano) ;

    public GeoPosition getPosition(){
        return position;
    }

    public double getMaxValue(){
        return maxValue;
    }

    public abstract String getType();

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public abstract Object getDefiningFeatures();

    @Override
    public String toString() {
        return this.getType() + " @" + getPosition();
    }

}
