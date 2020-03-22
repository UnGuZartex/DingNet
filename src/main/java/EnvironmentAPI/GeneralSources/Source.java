package EnvironmentAPI.GeneralSources;


import datagenerator.iaqsensor.TimeUnit;
import org.jxmapviewer.viewer.GeoPosition;

/**
 * Abstract class defining a source.
 *
 * @author Yentl.kinoo@student.kuleuven.be
 */
public abstract class Source {
    private GeoPosition position;

    protected TimeUnit timeUnit;

    protected Source(GeoPosition position, TimeUnit unit) {
        this.position = position;
        this.timeUnit = unit;
    }

    public void setPosition(GeoPosition position) {
        this.position = position;
    }


    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }


    public GeoPosition getPosition(){
        return position;
    }


    public abstract String getType();

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public abstract Object getDefiningFeatures();

    public abstract double generateData(double timeinNano) ;

    @Override
    public String toString() {
        return this.getType() + " @" + getPosition();
    }

}
