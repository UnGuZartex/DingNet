package EnvironmentAPI.util;

import EnvironmentAPI.PollutionEnvironment;
import datagenerator.iaqsensor.TimeUnit;
import iot.SimulationRunner;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import util.Pair;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EnvironmentReader {

    public static void loadEnvironment(File file, SimulationRunner runner) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            Element configuration = doc.getDocumentElement();
            Element Sensors = (Element) configuration.getElementsByTagName("Sensors").item(0);
            for (int i = 0; i < Sensors.getElementsByTagName("PolynomialSensor").getLength(); i++) {
                Element PSensorNode = (Element) Sensors.getElementsByTagName("PolynomialSensor").item(i);
                List<Pair<Double,Double>> Points = new ArrayList<>();
                for (int j = 0; j < PSensorNode.getElementsByTagName("Point").getLength(); j++) {
                    Points.add(ToPoint(PSensorNode.getElementsByTagName("Point").item(j).getTextContent()));
                }
                Double maxValue = Double.valueOf(PSensorNode.getElementsByTagName("MaximumValue").item(0).getTextContent());
                GeoPosition position = ToGeoPos(PSensorNode.getElementsByTagName("Position").item(0).getTextContent());
                TimeUnit unit = ToTimeUnit(PSensorNode.getElementsByTagName("TimeUnit").item(0).getTextContent());

                runner.getEnvironmentAPI().addSensor(SensorFactory.createPolynomialSensor(Points,maxValue,position, unit));
            }
            for (int i = 0; i < Sensors.getElementsByTagName("FunctionSensor").getLength(); i++) {
                Element FSensorNode = (Element) Sensors.getElementsByTagName("FunctionSensor").item(i);
                Double maxValue = Double.valueOf(FSensorNode.getElementsByTagName("MaximumValue").item(0).getTextContent());
                GeoPosition position = ToGeoPos(FSensorNode.getElementsByTagName("Position").item(0).getTextContent());
                String function = FSensorNode.getElementsByTagName("Function").item(0).getTextContent();
                TimeUnit unit = ToTimeUnit(FSensorNode.getElementsByTagName("TimeUnit").item(0).getTextContent());


                runner.getEnvironmentAPI().addSensor(SensorFactory.createFunctionSensor(function,maxValue,position, unit));

            }

        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    private static TimeUnit ToTimeUnit(String timeUnit) {
        switch (timeUnit){
            case "NANOS":
                return TimeUnit.NANOS;
            case "MICROS":
                return TimeUnit.MICROS;
            case "MILLIS":
                return TimeUnit.MILLIS;
            case "SECONDS":
                return TimeUnit.SECONDS;
            case "MINUTES":
                return TimeUnit.MINUTES;
            case "HOURS":
                return TimeUnit.HOURS;
            default:
                throw new IllegalArgumentException("INVALID TIME UNIT: " + timeUnit);

        }

    }

    private static GeoPosition ToGeoPos(String position) {
        String[] longlat = position.split(",");
        return new GeoPosition(Double.parseDouble(longlat[0]), Double.parseDouble(longlat[1]));
    }

    private static Pair<Double,Double> ToPoint(String point){
        String[] points = point.split(",");
        return new Pair<Double,Double>(Double.valueOf(points[0]),Double.valueOf(points[1]));
    }

}
