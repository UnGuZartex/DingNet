package EnvironmentAPI.GeneralSources;


import EnvironmentAPI.util.EnvSettings;
import EnvironmentAPI.util.OpenSimplex2S;
import datagenerator.iaqsensor.TimeUnit;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.Random;


public abstract class Source {
    private GeoPosition position;


    protected TimeUnit timeUnit;



    protected double maxValue;






    protected int NoiseRatio;

    protected Source(GeoPosition position, TimeUnit unit) {
        this.position = position;
        this.timeUnit = unit;
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


    public abstract double generateData(double timeinNano) ;

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
