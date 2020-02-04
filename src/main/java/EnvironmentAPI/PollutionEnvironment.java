package EnvironmentAPI;


import EnvironmentAPI.GeneralSensor.Sensor;
import iot.GlobalClock;
import org.jxmapviewer.viewer.GeoPosition;
import util.MapHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PollutionEnvironment {
    private List<Sensor> Sensors = new ArrayList<>();
    private static GlobalClock clock = null;



    public static void setClock(GlobalClock clockToSet){
        clock = clockToSet;
    }


    public void addSensor(Sensor sensor){
        Sensors.add(sensor);
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

    public double getMaxOfSensors(){
        List<Double> maxValues = new ArrayList<>();
        for(Sensor sensor:Sensors){
            maxValues.add(sensor.getMaxValue());
        }
        return Collections.max(maxValues);
    }
    public double getDataFromSensors(GeoPosition position) {
        if(Sensors.isEmpty()){
            return 0.0;
        }
        double total = 0;

        List<Double> allDistances = getAllDistances(position);
        for(int i = 0; i < Sensors.size(); i++){
            if (allDistances.get(i) == 0.0){
                return Sensors.get(i).generateData(clock.getTime().toNanoOfDay())/getMaxOfSensors();
            }
            total += Sensors.get(i).generateData(clock.getTime().toNanoOfDay())*allDistances.get(i);
        }

        total /= getMaxOfSensors();
        return total;
    }

    private List<Double> getAllDistances(GeoPosition position) {
        List<Double> AllDistances = new ArrayList<>();
        double total = 0;
        for(Sensor sensor:Sensors){
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
       this.Sensors.clear();

    }

    public List<Sensor> getSensors() {
        return Sensors;
    }
}
