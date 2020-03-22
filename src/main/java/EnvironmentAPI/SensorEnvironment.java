package EnvironmentAPI;

import EnvironmentAPI.GeneralSources.Source;
import EnvironmentAPI.Sensor.Sensor;
import EnvironmentAPI.util.EnvSettings;
import EnvironmentAPI.util.SensorFactory;
import gui.util.GUISettings;
import org.jxmapviewer.viewer.GeoPosition;
import util.MapHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class representing a SensorEnvironment, this is a environment that is defined by the values the
 * sensors measure, this is an estimate the sensors can make by working together.
 *
 * @author Yentl.kinoo@student.kuleuven.be
 */
public class SensorEnvironment {
    private List<Sensor> sensors = new ArrayList<>();

    private PollutionEnvironment poll =  new PollutionEnvironment(GUISettings.POLLUTION_GRID_SQUARES, EnvSettings.DIFFUSION_FACTOR, 1);

    public PollutionEnvironment getPoll() {
        return poll;
    }

    public void reset() {
        this.sensors.clear();

    }

    public void addSensor(Sensor sensor) {
        sensors.add(sensor);
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    /**
     * Gets the average pollution between 2 points on the map, with a given interpollation value.
     * @param begin first point
     * @param end second point
     * @param interpollationDistance distance between segments to calculate pollution from
     * @return an average value of the pollution between the two points.
     */
    public double getDataBetweenPoints(GeoPosition begin, GeoPosition end, double interpollationDistance) {
        double totalPollution = 0;
        totalPollution += getDataFromSensors(begin);
        totalPollution += getDataFromSensors(end);
        double totalDistance = MapHelper.distance(begin, end);
        int amount = (int) (totalDistance / interpollationDistance);
        double phi1 = Math.toRadians(begin.getLatitude());
        double lambda1 = Math.toRadians(begin.getLongitude());
        double phi2 = Math.toRadians(end.getLatitude());
        double lambda2 = Math.toRadians(end.getLongitude());


        double xDelta = (phi2 - phi1) / amount;
        double yDelta = (lambda2 - lambda1) / amount;
        for (int i = 0; i < amount; i++) {
            double x = phi1 + i * xDelta;
            double y = lambda1 + i * yDelta;
            double xc = Math.toDegrees(x);
            double yc = Math.toDegrees(y);
            GeoPosition newPoint = new GeoPosition(xc, yc);
            totalPollution += getDataFromSensors(newPoint);

        }
        return totalPollution/(amount+2);
    }

    /**
     * Method to get pollution at a certain moment in time, only useful if you need information about the future! Otherwise use the normal function
     * @param position The position of which the pollution needs to be found
     * @param timeInMs The time of which the pollution needs to be found
     * @return the pollution at the given time and position.
     */
    @Deprecated
    public double getDataFromSensors(GeoPosition position, long timeInMs) {
        SensorEnvironment environment = new SensorEnvironment();
        for (Source source : this.getPoll().getSources()){
            environment.getPoll().addSource(source);
        }
        for (Sensor sensor : this.getSensors()) {
            environment.addSensor(SensorFactory.createSensor(environment.getPoll(), sensor.getEnvironment() , sensor.getPosition(), sensor.getMaxValue(), sensor.getNoiseRatio()));
        }
        long i = 0;
        while (i <= timeInMs) {
            environment.getPoll().doStep(i*1000000,environment.getSensors().get(0).getEnvironment());
            i++;
        }

        return environment.getDataFromSensors(position);
    }

    /**
     * Return the maximum value of all sensors, the guessed pollution is determined by the max of these
     * sensors.
     * @return the maximum of all sensors.
     */
    public double getMaxOfSensors(){
        List<Double> maxValues = new ArrayList<>();
        for(Sensor source : sensors){
            maxValues.add(source.getMaxValue());
        }
        return Collections.max(maxValues);
    }

    /**
     * Gets the data at a given position at the current time (read current instance of pollutionenv)
     * @param position the position to get the pollution from
     * @return pollution at the current time.
     */
    public double getDataFromSensors(GeoPosition position ) {
        if(sensors.isEmpty()){
            return 0.0;
        }
        double total = 0;

        List<Double> allDistances = getAllDistances(position);
        for(int i = 0; i < sensors.size(); i++){
            if (allDistances.get(i) == 0.0){
                return sensors.get(i).generateData()/getMaxOfSensors();
            }
            total += sensors.get(i).generateData()*allDistances.get(i);
        }

        total /= getMaxOfSensors();
        return total ;
    }

    /**
     * Get all distances to this point of sensors in this env
     * @param position position to determine the distance to
     * @return A list of distances to this position
     */
    private List<Double> getAllDistances(GeoPosition position) {
        List<Double> AllDistances = new ArrayList<>();
        double total = 0;
        for(Sensor sensor : sensors){
            double inverseDistance = 1/getDistanceToSensor(sensor,position);
            AllDistances.add(inverseDistance);
            total += inverseDistance;
        }

        for(int i = 0; i < AllDistances.size(); i++){
            AllDistances.set(i, AllDistances.get(i)/total);
        }

        return AllDistances;
    }

    /**
     * Calculate the distance to a sensor
     * @param sensor the sensor to determine its distance to
     * @param position position to determine the distance from
     * @return the distance between sensor and position
     */
    private double getDistanceToSensor(Sensor sensor, GeoPosition position) {
        return MapHelper.distance(position, sensor.getPosition());
    }


}
