package EnvironmentAPI.GeneralSensor;


import datagenerator.iaqsensor.TimeUnit;
import org.jxmapviewer.viewer.GeoPosition;

import java.sql.Time;


public abstract class Sensor{
    private GeoPosition position;


    protected TimeUnit timeUnit;



    protected double maxValue;

    protected Sensor(GeoPosition position, double maxValue, TimeUnit unit) {
        this.position = position;
        this.maxValue = maxValue;
        this.timeUnit = unit;
    }

    public void setPosition(GeoPosition position) {
        this.position = position;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }


    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
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
