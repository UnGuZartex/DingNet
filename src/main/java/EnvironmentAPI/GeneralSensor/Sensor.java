package EnvironmentAPI.GeneralSensor;


import EnvironmentAPI.PollutionEnvironment;
import org.jxmapviewer.viewer.GeoPosition;

public abstract class Sensor{
    private int radius;
    private PollutionEnvironment environment;
    private GeoPosition position;

    protected Sensor(int radius, PollutionEnvironment environment,GeoPosition position) {
        this.radius = radius;
        this.environment = environment;
        this.position = position;
    }

    public double generateData(long timeinNano) {
        return 0.0;
    }

    public GeoPosition getPosition(){
        return position;
    }

}
