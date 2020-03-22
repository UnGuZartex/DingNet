package EnvironmentAPI;



import EnvironmentAPI.GeneralSources.Source;
import EnvironmentAPI.Sensor.Sensor;
import EnvironmentAPI.util.SensorFactory;
import gui.util.GUISettings;
import org.jxmapviewer.viewer.GeoPosition;
import util.MapHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SensorEnvironment {
    private List<Sensor> sensors = new ArrayList<>();

    private PollutionEnvironment poll =  new PollutionEnvironment(GUISettings.POLLUTION_GRID_SQUARES, (float) 0.1, 1);

    public PollutionEnvironment getPoll() {
        return poll;
    }

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


    public double getMaxOfSensors(){
        List<Double> maxValues = new ArrayList<>();
        for(Sensor source : sensors){
            maxValues.add(source.getMaxValue());
        }
        return Collections.max(maxValues);
    }
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

    private double getDistanceToSensor(Sensor sensor, GeoPosition position) {
        return MapHelper.distance(position, sensor.getPosition());
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
}
