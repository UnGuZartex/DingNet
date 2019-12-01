package application.Environment;

import application.Application;
import application.Environment.GeneralSensor.PolynomialSensor.PolynomialSensor;
import application.Environment.GeneralSensor.Sensor;
import iot.GlobalClock;
import iot.mqtt.TransmissionWrapper;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PollutionEnvironment {
    private List<Sensor> Sensors = new ArrayList<>();
    private GlobalClock clock;

    public PollutionEnvironment(GlobalClock clock){

        readSensorsFromJSON();
        this.clock = clock;
    }

    private void readSensorsFromJSON() {
        Sensors.add(new PolynomialSensor(2,null,this, new GeoPosition(50,50)));
        Sensors.add(new PolynomialSensor(2,null,this, new GeoPosition(50,50)));
        Sensors.add(new PolynomialSensor(2,null,this, new GeoPosition(50,50)));
        Sensors.add(new PolynomialSensor(2,null,this, new GeoPosition(50,50)));
        Sensors.add(new PolynomialSensor(2,null,this, new GeoPosition(50,50)));

    }

    public double getDataFromSensors(GeoPosition position){
        double total = 0;
        for(Sensor sensor:Sensors){
            double distance = getDistanceToSensor(sensor,position);
            if (distance == 0.0){
                return sensor.generateData(clock.getTime())/255;
            }
            total += sensor.generateData(clock.getTime())/distance;
        }
        System.out.println("Total: " + total);

        total /= Sensors.size();
        System.out.println("Total: " + total);

        total /= 255;
        System.out.println("Total: " + total);
        return total;
    }

    private double getDistanceToSensor(Sensor sensor, GeoPosition position) {
        Random random = new Random();
        return random.nextInt(10);
    }
}
