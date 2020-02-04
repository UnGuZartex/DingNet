package EnvironmentAPI.GeneralSensor;


import datagenerator.iaqsensor.TimeUnit;
import org.jxmapviewer.viewer.GeoPosition;


public abstract class Sensor{
    private GeoPosition position;
    protected double maxValue;

    protected Sensor(GeoPosition position, double maxValue) {
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

    public abstract String getType();
    public abstract TimeUnit getTimeUnit();

    public abstract Object getDefiningFeatures();

}
