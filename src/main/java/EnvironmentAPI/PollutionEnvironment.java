package EnvironmentAPI;

import EnvironmentAPI.GeneralSensor.PolynomialSensor.PolynomialSensor;
import EnvironmentAPI.GeneralSensor.Sensor;
import iot.GlobalClock;
import org.apache.commons.lang3.time.StopWatch;
import org.jxmapviewer.viewer.GeoPosition;
import util.MapHelper;

import java.util.ArrayList;
import java.util.List;

public class PollutionEnvironment {
    private List<Sensor> Sensors = new ArrayList<>();
    private final static StopWatch stopwatch = new StopWatch();

    public PollutionEnvironment(GlobalClock clock) {

        readSensorsFromJSON();
        stopwatch.reset();
        stopwatch.start();
    }

    private void readSensorsFromJSON() {
        Sensors.add(new PolynomialSensor(2, null, this, new GeoPosition(50.873566, 4.696793)));
        Sensors.add(new PolynomialSensor(2, null, this, new GeoPosition(50.879845, 4.700518)));
        Sensors.add(new PolynomialSensor(2, null, this, new GeoPosition(50.883023, 4.704790)));
        Sensors.add(new PolynomialSensor(2, null, this, new GeoPosition(50.883259, 4.689375)));
        Sensors.add(new PolynomialSensor(2, null, this, new GeoPosition(50.875508, 4.691571)));

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
            System.out.println(amount);

            System.out.println(totalPollution);
            System.out.println(xc + "," + yc);
        }
        return totalPollution/(amount+2);
    }

    public double getDataFromSensors(GeoPosition position){
        double total = 0;
        List<Double> allDistances = getAllDistances(position);
        //System.out.println(allDistances);
        for(int i = 0; i < Sensors.size(); i++){
            if (allDistances.get(i) == 0.0){
                return Sensors.get(i).generateData(stopwatch.getNanoTime())/255;
            }
            total += Sensors.get(i).generateData(stopwatch.getNanoTime())*allDistances.get(i);
        }
        //System.out.println("Total: " + total);

        total /= 255;
        //System.out.println("Total: " + total);
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

    public void Stop(){
        stopwatch.stop();
    }
}