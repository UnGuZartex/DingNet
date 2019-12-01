package application.Environment.GeneralSensor;


import application.Environment.PollutionEnvironment;
import org.jxmapviewer.viewer.GeoPosition;

import java.time.LocalTime;

public abstract class Sensor{
    private int radius;
    private PollutionEnvironment environment;
    private GeoPosition position;

    protected Sensor(int radius, PollutionEnvironment environment,GeoPosition position) {
        this.radius = radius;
        this.environment = environment;
        this.position = position;
    }

    public double generateData(LocalTime time) {
        return 0.0;
    }

    public GeoPosition getPosition(){
        return position;
    }

}
