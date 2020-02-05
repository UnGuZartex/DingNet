package EnvironmentAPI.util;

import EnvironmentAPI.GeneralSensor.Sensor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import util.Pair;


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.List;

public class EnvironmentWriter {
    public static void saveEnvironment(List<Sensor> toSaveList, File file){
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            // root element
            Element rootElement = doc.createElement("Environment");
            doc.appendChild(rootElement);
            Element sensorElement = doc.createElement("Sensors");
            rootElement.appendChild(sensorElement);
            for(Sensor sensor:toSaveList){
                Element type = doc.createElement(sensor.getType());
                sensorElement.appendChild(type);
                Element Position = doc.createElement("Position");
                Position.appendChild(doc.createTextNode(sensor.getPosition().toString().replace("[", "").replace("]", "")));
                type.appendChild(Position);
                Element MaximumValue = doc.createElement("MaximumValue");
                MaximumValue.appendChild(doc.createTextNode(String.valueOf(sensor.getMaxValue())));
                type.appendChild(MaximumValue);
                Element TimeUnit = doc.createElement("TimeUnit");
                TimeUnit.appendChild(doc.createTextNode(sensor.getTimeUnit().name()));
                type.appendChild(TimeUnit);
                switch(sensor.getType()){
                    case "FunctionSensor":
                        Element Function = doc.createElement("Function");
                        Function.appendChild(doc.createTextNode(String.valueOf(sensor.getDefiningFeatures())));
                        type.appendChild(Function);
                        break;
                    case "PolynomialSensor":
                        System.out.println(sensor.getType());
                        @SuppressWarnings("unchecked")
                        List<Pair<Double,Double>> pointsKnown = (List<Pair<Double, Double>>) sensor.getDefiningFeatures();
                        for(Pair<Double,Double> point:pointsKnown){
                            Element Point = doc.createElement("Point");
                            Point.appendChild(doc.createTextNode(point.toString().replace("(","").replace(")","")));
                            type.appendChild(Point);
                        }
                        break;
                    default:
                        System.out.println("Invalid type: " + sensor.getType());
                }
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);


        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }


}

