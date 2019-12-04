package application.Environment;

import application.Environment.GeneralSensor.PolynomialSensor.PolynomialSensor;
import application.Environment.GeneralSensor.Sensor;
import iot.GlobalClock;
import org.jxmapviewer.viewer.GeoPosition;
import util.MapHelper;

import java.util.ArrayList;
import java.util.List;

public class PollutionEnvironment {
    private List<Sensor> Sensors = new ArrayList<>();
    private GlobalClock clock;

    public PollutionEnvironment(GlobalClock clock){

        readSensorsFromJSON();
        this.clock = clock;
    }

    private void readSensorsFromJSON() {
        Sensors.add(new PolynomialSensor(2,null,this, new GeoPosition(50.873566,4.696793)));
        Sensors.add(new PolynomialSensor(2,null,this, new GeoPosition(50.879845,4.700518)));
        Sensors.add(new PolynomialSensor(2,null,this, new GeoPosition(50.883023,4.704790)));
        Sensors.add(new PolynomialSensor(2,null,this, new GeoPosition(50.883259,4.689375)));
        Sensors.add(new PolynomialSensor(2,null,this, new GeoPosition(50.875508,4.691571)));

    }

    public double getDataFromSensors(GeoPosition position){
        double total = 0;
        List<Double> allDistances = getAllDistances(position);
        System.out.println(allDistances);
        for(int i = 0; i < Sensors.size(); i++){
            if (allDistances.get(i) == 0.0){
                return Sensors.get(i).generateData(clock.getTime())/255;
            }
            total += Sensors.get(i).generateData(clock.getTime())*allDistances.get(i);
        }
        System.out.println("Total: " + total);

        total /= 255;
        System.out.println("Total: " + total);
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
}
