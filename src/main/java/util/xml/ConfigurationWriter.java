package util.xml;

import iot.Environment;
import iot.SimulationRunner;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import iot.networkentity.MoteSensor;
import iot.networkentity.UserMote;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import util.Connection;
import util.GraphStructure;
import util.Path;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ConfigurationWriter {
    private static IdRemapping idRemapping = new IdRemapping();


    public static void saveConfigurationToFile(File file, SimulationRunner simulationRunner) {
        idRemapping.reset();

        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Environment environment = simulationRunner.getEnvironment();
            GraphStructure graph = environment.getGraph();

            // root element
            Element rootElement = doc.createElement("configuration");
            doc.appendChild(rootElement);


            // ---------------
            //      Map
            // ---------------
            Element map = doc.createElement("map");
            Element region = doc.createElement("region");
            Element origin = doc.createElement("origin");
            map.appendChild(origin);

            Element MapZeroLatitude = doc.createElement("latitude");
            MapZeroLatitude.appendChild(doc.createTextNode(Double.toString(environment.getMapOrigin().getLatitude())));
            origin.appendChild(MapZeroLatitude);

            Element MapZeroLongitude = doc.createElement("longitude");
            MapZeroLongitude.appendChild(doc.createTextNode(Double.toString(environment.getMapOrigin().getLongitude())));
            origin.appendChild(MapZeroLongitude);
            region.appendChild(origin);

            Element size = doc.createElement("size");
            Element width = doc.createElement("width");
            width.appendChild(doc.createTextNode(Integer.toString(environment.getMaxXpos() + 1)));
            Element height = doc.createElement("height");
            height.appendChild(doc.createTextNode(Integer.toString(environment.getMaxYpos() + 1)));
            size.appendChild(width);
            size.appendChild(height);
            region.appendChild(size);

            map.appendChild(region);


            // ---------------
            // Characteristics
            // ---------------

            Element characteristics = doc.createElement("characteristics");
            Element regionProperty = doc.createElement("regionProperty");
            regionProperty.setAttribute("numberOfZones", Integer.toString(environment.getNumberOfZones()));
            characteristics.appendChild(regionProperty);

            int amountOfSquares = (int) Math.sqrt(environment.getNumberOfZones());
            LinkedList<Element> row = new LinkedList<>();
            for (int i = 0; i < amountOfSquares; i++) {
                row.add(doc.createElement("row"));
                row.getLast().appendChild(doc.createTextNode(environment.getCharacteristic(0, (int) Math.round(i * ((double) environment.getMaxXpos()) / amountOfSquares) + 1
                ).name()));
                for (int j = 1; j < amountOfSquares; j++) {

                    row.getLast().appendChild(doc.createTextNode("-" + environment.getCharacteristic((int) Math.round(j * ((double) environment.getMaxXpos()) / amountOfSquares) + 1
                        , (int) Math.round(i * ((double) environment.getMaxYpos()) / amountOfSquares) + 1).name()));
                }
                characteristics.appendChild(row.getLast());
            }



            // ---------------
            //    WayPoints
            // ---------------

            Element wayPointsElement = doc.createElement("wayPoints");
            var wayPoints = graph.getWayPoints();

            for (var me : wayPoints.entrySet()) {
                Element wayPointElement = doc.createElement("wayPoint");
                long newId = idRemapping.addWayPoint(me.getKey(), me.getValue());

                wayPointElement.setAttribute("id", Long.toString(newId));
                var wayPoint = me.getValue();
                wayPointElement.appendChild(doc.createTextNode(wayPoint.getLatitude() + "," + wayPoint.getLongitude()));
                wayPointsElement.appendChild(wayPointElement);
            }



            // ---------------
            //   Connections
            // ---------------

            Element connectionsElement = doc.createElement("connections");
            var connections = graph.getConnections();

            for (var me : connections.entrySet()) {
                Element connectionElement = doc.createElement("connection");
                long originalId = me.getKey();
                Connection conn = me.getValue();

                long newFromId = idRemapping.getNewWayPointId(conn.getFrom());
                long newToId = idRemapping.getNewWayPointId(conn.getTo());

                // Not really necesary here to make a new connection, but it's a bit awkward to put null here
                long newId = idRemapping.addConnection(originalId, new Connection(newFromId, newToId));

                connectionElement.setAttribute("id", Long.toString(newId));
                connectionElement.setAttribute("src", Long.toString(newFromId));
                connectionElement.setAttribute("dst", Long.toString(newToId));

                connectionsElement.appendChild(connectionElement);
            }



            // ---------------
            //      Motes
            // ---------------

            Element motes = doc.createElement("motes");

            for (Mote mote : environment.getMotes()) {
                if (mote instanceof UserMote) {
                    motes.appendChild(new UserMoteWriter(doc, (UserMote) mote, environment).buildMoteElement());
                } else {
                    motes.appendChild(new MoteWriter(doc, mote, environment).buildMoteElement());
                }
            }


            // ---------------
            //    Gateways
            // ---------------

            Element gateways = doc.createElement("gateways");

            for (Gateway gateway : environment.getGateways()) {
                Element gatewayElement = doc.createElement("gateway");

                Element devEUI = doc.createElement("devEUI");
                devEUI.appendChild(doc.createTextNode(Long.toUnsignedString(gateway.getEUI())));

                Element location = doc.createElement("location");
                Element xPos = doc.createElement("xPos");
                xPos.appendChild(doc.createTextNode(Integer.toString(gateway.getXPosInt())));
                Element yPos = doc.createElement("yPos");
                yPos.appendChild(doc.createTextNode(Integer.toString(gateway.getYPosInt())));
                location.appendChild(xPos);
                location.appendChild(yPos);

                Element transmissionPower = doc.createElement("transmissionPower");
                transmissionPower.appendChild(doc.createTextNode(Integer.toString(gateway.getTransmissionPower())));

                Element spreadingFactor = doc.createElement("spreadingFactor");
                spreadingFactor.appendChild(doc.createTextNode(Integer.toString(gateway.getSF())));

                gatewayElement.appendChild(devEUI);
                gatewayElement.appendChild(location);
                gatewayElement.appendChild(transmissionPower);
                gatewayElement.appendChild(spreadingFactor);
                gateways.appendChild(gatewayElement);
            }


            // ---------------
            //    Data dump
            // ---------------

            rootElement.appendChild(map);
            rootElement.appendChild(characteristics);
            rootElement.appendChild(motes);
            rootElement.appendChild(gateways);
            rootElement.appendChild(wayPointsElement);
            rootElement.appendChild(connectionsElement);

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static class MoteWriter {
        Document doc;
        Mote mote;
        GraphStructure graph;
        Environment environment;

        MoteWriter(Document doc, Mote mote, Environment environment) {
            this.doc = doc;
            this.mote = mote;
            this.graph = environment.getGraph();
            this.environment = environment;
        }

        Element generateDevEUIElement() {
            Element devEUI =  doc.createElement("devEUI");
            devEUI.appendChild(doc.createTextNode(Long.toUnsignedString(mote.getEUI())));
            return devEUI;
        }

        Element generateLocationElement() {
            Element location = doc.createElement("location");
            Element wayPoint = doc.createElement("waypoint");

            GeoPosition position = environment.getMapHelper().toGeoPosition(mote.getOriginalPosInt());
            wayPoint.setAttribute("id", Long.toString(idRemapping.getNewWayPointId(graph.getClosestWayPoint(position))));
            location.appendChild(wayPoint);

            return location;
        }

        Element generateTransmissionPowerElement() {
            Element transmissionPower = doc.createElement("transmissionPower");
            transmissionPower.appendChild(doc.createTextNode(Integer.toString(mote.getTransmissionPower())));
            return transmissionPower;
        }

        Element generateSpreadingFactorElement() {
            Element spreadingFactor = doc.createElement("spreadingFactor");
            spreadingFactor.appendChild(doc.createTextNode(Integer.toString(mote.getSF())));
            return spreadingFactor;
        }

        Element generateEnergyLevelElement() {
            Element energyLevel = doc.createElement("energyLevel");
            energyLevel.appendChild(doc.createTextNode(Integer.toString(mote.getEnergyLevel())));
            return energyLevel;
        }

        Element generateMovementSpeedElement() {
            Element movementSpeed = doc.createElement("movementSpeed");
            movementSpeed.appendChild(doc.createTextNode(Double.toString(mote.getMovementSpeed())));
            return movementSpeed;
        }

        Element generateStartMovementSpeedElement() {
            Element startMovementOffset = doc.createElement("startMovementOffset");
            startMovementOffset.appendChild(doc.createTextNode(Integer.toString(mote.getStartMovementOffset())));
            return startMovementOffset;
        }

        Element generatePeriodSendingPacketElement() {
            Element periodSendingPacket = doc.createElement("periodSendingPacket");
            periodSendingPacket.appendChild(doc.createTextNode(""+mote.getPeriodSendingPacket()));
            return periodSendingPacket;
        }

        Element generateStartSendingOffsetElement() {
            Element startSendingOffset = doc.createElement("startSendingOffset");
            startSendingOffset.appendChild(doc.createTextNode(""+mote.getStartSendingOffset()));
            return startSendingOffset;
        }

        Element generateMoteSensorsElement() {
            Element sensors = doc.createElement("sensors");
            for (MoteSensor sensor : mote.getSensors()) {
                Element sensorElement = doc.createElement("sensor");
                sensorElement.setAttribute("SensorType", sensor.name());
                sensors.appendChild(sensorElement);
            }
            return sensors;
        }

        Element generatePathElement() {
            Element pathElement = doc.createElement("path");
            Path path = mote.getPath();
            for (Long id : path.getConnectionsByID()) {
                Element connectionElement = doc.createElement("connection");
                connectionElement.setAttribute("id", Long.toString(idRemapping.getNewConnectionId(id)));
                pathElement.appendChild(connectionElement);
            }
            return pathElement;
        }


        void addMoteDetails(Element element) {
            List.of(generateDevEUIElement(), generateLocationElement(), generateTransmissionPowerElement(), generateSpreadingFactorElement(),
                generateEnergyLevelElement(), generateMovementSpeedElement(), generateStartMovementSpeedElement(), generatePeriodSendingPacketElement(),
                generateStartSendingOffsetElement(), generateMoteSensorsElement(), generatePathElement())
                .forEach(element::appendChild);
        }

        public Element buildMoteElement() {
            Element moteElement = doc.createElement("mote");
            addMoteDetails(moteElement);
            return moteElement;
        }
    }

    private static class UserMoteWriter extends MoteWriter {
        UserMoteWriter(Document doc, UserMote mote, Environment environment) {
            super(doc, mote, environment);
        }

        Element generateIsActiveElement() {
            Element isActive = doc.createElement("userMoteState");
            isActive.appendChild(doc.createTextNode(Boolean.toString(((UserMote) mote).isActive())));
            return isActive;
        }

        Element generateDestinationElement() {
            GeoPosition destinationPos = ((UserMote) mote).getDestination();
            Element destination = doc.createElement("destination");
            destination.setAttribute("id", Long.toString(idRemapping.getNewWayPointId(graph.getClosestWayPoint(destinationPos))));
            return destination;
        }

        void addUserMoteDetails(Element element) {
            List.of(generateIsActiveElement(), generateDestinationElement())
                .forEach(element::appendChild);
        }

        @Override
        public Element generatePathElement() {
            // Empty element since user motes get their path from the routing application
            // (Currently, a starting path for user motes is not supported)
            return doc.createElement("path");
        }

        @Override
        public Element buildMoteElement() {
            Element moteElement = doc.createElement("userMote");
            addMoteDetails(moteElement);
            addUserMoteDetails(moteElement);
            return moteElement;
        }
    }
}
