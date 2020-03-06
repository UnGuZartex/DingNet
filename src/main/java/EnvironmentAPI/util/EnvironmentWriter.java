package EnvironmentAPI.util;

import EnvironmentAPI.GeneralSources.Source;
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
    public static void saveEnvironment(List<Source> toSaveList, File file){
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            // root element
            Element rootElement = doc.createElement("Environment");
            doc.appendChild(rootElement);
            Element sourceElement = doc.createElement("Sources");
            rootElement.appendChild(sourceElement);
            for(Source source :toSaveList){
                Element type = doc.createElement(source.getType());
                sourceElement.appendChild(type);
                Element Position = doc.createElement("Position");
                Position.appendChild(doc.createTextNode(source.getPosition().toString().replace("[", "").replace("]", "")));
                type.appendChild(Position);
                Element TimeUnit = doc.createElement("TimeUnit");
                TimeUnit.appendChild(doc.createTextNode(source.getTimeUnit().name()));
                type.appendChild(TimeUnit);
                switch(source.getType()){
                    case "FunctionSource":
                        Element Function = doc.createElement("Function");
                        Function.appendChild(doc.createTextNode(String.valueOf(source.getDefiningFeatures())));
                        type.appendChild(Function);
                        break;
                    case "PolynomialSource":
                        System.out.println(source.getType());
                        @SuppressWarnings("unchecked")
                        List<Pair<Double,Double>> pointsKnown = (List<Pair<Double, Double>>) source.getDefiningFeatures();
                        for(Pair<Double,Double> point:pointsKnown){
                            Element Point = doc.createElement("Point");
                            Point.appendChild(doc.createTextNode(point.toString().replace("(","").replace(")","")));
                            type.appendChild(Point);
                        }
                        break;
                    default:
                        System.out.println("Invalid type: " + source.getType());
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

