package EnvironmentAPI.GeneralSensor;


import org.jxmapviewer.viewer.GeoPosition;

public abstract class Sensor{
    private int radius;
    private GeoPosition position;

    protected Sensor(int radius,GeoPosition position) {
        this.radius = radius;
        this.position = position;
    }

    public abstract double generateData(long timeinNano) ;

    public GeoPosition getPosition(){
        return position;
    }

}
