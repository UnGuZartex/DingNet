package EnvironmentAPI.GeneralSensor;


import org.jxmapviewer.viewer.GeoPosition;

public abstract class Sensor{
    private int radius;
    private GeoPosition position;
    protected double maxValue;

    protected Sensor(int radius, GeoPosition position, double maxValue) {
        this.radius = radius;
        this.position = position;
        this.maxValue = maxValue;
    }

    public abstract double generateData(long timeinNano) ;

    public GeoPosition getPosition(){
        return position;
    }

    public double getMaxValue(){
        return maxValue;
    }

}
