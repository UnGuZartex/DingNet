package EnvironmentAPI.util;

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

/**
 * Class to read an environmentfile
 *
 * @author Yentl Kinoo
 */
public class EnvironmentReader {

    public static void loadEnvironment(File file, SimulationRunner runner) {
        try {
            runner.getEnvironmentAPI().getPoll().reset();
            runner.getEnvironmentAPI().reset();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            Element configuration = doc.getDocumentElement();
            Element Sources = (Element) configuration.getElementsByTagName("Sources").item(0);
            for (int i = 0; i < Sources.getElementsByTagName("PolynomialSource").getLength(); i++) {
                Element PSourceNode = (Element) Sources.getElementsByTagName("PolynomialSource").item(i);
                List<Pair<Double,Double>> Points = new ArrayList<>();
                for (int j = 0; j < PSourceNode.getElementsByTagName("Point").getLength(); j++) {
                    Points.add(ToPoint(PSourceNode.getElementsByTagName("Point").item(j).getTextContent()));
                }
                GeoPosition position = ToGeoPos(PSourceNode.getElementsByTagName("Position").item(0).getTextContent());
                if (!runner.getEnvironment().isWithinBounds(position)) {
                    throw new IllegalStateException("Source @" + position + " is in an invalid position.");
                }
                TimeUnit unit = ToTimeUnit(PSourceNode.getElementsByTagName("TimeUnit").item(0).getTextContent());

                runner.getEnvironmentAPI().getPoll().addSource(SourceFactory.createPolynomialSource(Points, position, unit));
            }
            for (int i = 0; i < Sources.getElementsByTagName("FunctionSource").getLength(); i++) {
                Element FSourceNode = (Element) Sources.getElementsByTagName("FunctionSource").item(i);
                GeoPosition position = ToGeoPos(FSourceNode.getElementsByTagName("Position").item(0).getTextContent());
                if (!runner.getEnvironment().isWithinBounds(position)) {
                    throw new IllegalStateException("Source @" + position + " is in an invalid position.");
                }
                String function = FSourceNode.getElementsByTagName("Function").item(0).getTextContent();
                TimeUnit unit = ToTimeUnit(FSourceNode.getElementsByTagName("TimeUnit").item(0).getTextContent());

                runner.getEnvironmentAPI().getPoll().addSource(SourceFactory.createFunctionSource(function, position, unit));

            }

            Element Sensors = (Element) configuration.getElementsByTagName("Sensors").item(0);
            for (int i = 0; i < Sensors.getElementsByTagName("Sensor").getLength(); i++) {
                Element SensorNode = (Element) Sensors.getElementsByTagName("Sensor").item(i);
                GeoPosition position = ToGeoPos(SensorNode.getElementsByTagName("Position").item(0).getTextContent());
                if (!runner.getEnvironment().isWithinBounds(position)) {
                    throw new IllegalStateException("Sensor @" + position + " is in an invalid position.");
                }
                double maxValue = Double.parseDouble(SensorNode.getElementsByTagName("MaxValue").item(0).getTextContent());
                int noiseRatio = Integer.parseInt(SensorNode.getElementsByTagName("NoiseRatio").item(0).getTextContent());

                runner.getEnvironmentAPI().addSensor(SensorFactory.createSensor(runner.getEnvironmentAPI().getPoll(), runner.getEnvironment(), position, maxValue, noiseRatio));
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
