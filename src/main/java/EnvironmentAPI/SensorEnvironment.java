package EnvironmentAPI;



import EnvironmentAPI.Sensor.Sensor;
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

    public double getDataBetweenPoints(GeoPosition begin, GeoPosition end, double interpollationDistance, long timeInNano) {
        double totalPollution = 0;
        totalPollution += getDataFromSensors(begin,timeInNano);
        totalPollution += getDataFromSensors(end,timeInNano);
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
            totalPollution += getDataFromSensors(newPoint,timeInNano);

        }
        return totalPollution/(amount+2);
    }



    public double getMaxOfSensors(){
        List<Double> maxValues = new ArrayList<>();
        for(Sensor source : sensors){
            maxValues.add(source.getMaxValue());
        }
        return Collections.max(maxValues);
    }
    public double getDataFromSensors(GeoPosition position, long timeInNano ) {
        if(sensors.isEmpty()){
            return 0.0;
        }
        double total = 0;

        List<Double> allDistances = getAllDistances(position);
        for(int i = 0; i < sensors.size(); i++){
            if (allDistances.get(i) == 0.0){
                return sensors.get(i).generateData(timeInNano)/getMaxOfSensors();
            }
            total += sensors.get(i).generateData(timeInNano)*allDistances.get(i);
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

    public void removeSensor(Sensor sensor) {
        this.sensors.remove(sensor);
    }

    public void addSensor(Sensor sensor) {
        sensors.add(sensor);
    }
}
