package util.xml;

import iot.Characteristic;
import iot.Environment;
import iot.SimulationRunner;
import iot.networkentity.*;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import util.Connection;
import util.Pair;
import util.Path;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ConfigurationReader {
    private static IdRemapping idRemapping = new IdRemapping();

    public static void loadConfiguration(File file, SimulationRunner simulationRunner) {
        idRemapping.reset();

        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            Element configuration = doc.getDocumentElement();


            // ---------------
            //      Map
            // ---------------

            Element map = (Element) configuration.getElementsByTagName("map").item(0);
            Element region = (Element) map.getElementsByTagName("region").item(0);
            int width = Integer.parseInt(XMLHelper.readChild(region, "width"));
            int height = Integer.parseInt(XMLHelper.readChild(region, "height"));

            Element origin = (Element) region.getElementsByTagName("origin").item(0);
            GeoPosition mapOrigin = new GeoPosition(
                Double.parseDouble(XMLHelper.readChild(origin, "latitude")),
                Double.parseDouble(XMLHelper.readChild(origin, "longitude"))
            );


            // ---------------
            // Characteristics
            // ---------------

            Element characteristics = (Element) configuration.getElementsByTagName("characteristics").item(0);
            int numberOfZones = Integer.parseInt(((Element) characteristics.getElementsByTagName("regionProperty").item(0)).getAttribute("numberOfZones"));
            long n = Math.round(Math.sqrt(numberOfZones));
            Characteristic[][] characteristicsMap = new Characteristic[width][height];

            for (int i = 0; i < n; i++) {
                String[] characteristicsRow = characteristics.getElementsByTagName("row").item(i).getTextContent().split("-");
                for (int j = 0; j < characteristicsRow.length; j++) {
                    Characteristic characteristic = Characteristic.valueOf(characteristicsRow[j]);

                    double widthSize = ((double) width) / n;
                    double heightSize = ((double) height) / n;
                    for (int x = (int) Math.round(j * widthSize); x < (int) Math.round((j + 1) * widthSize); x++) {
                        for (int y = (int) Math.round(i * heightSize); y < (int) Math.round((i + 1) * heightSize); y++) {
                            characteristicsMap[x][y] = characteristic;
                        }
                    }
                }

            }



            // ---------------
            //    WayPoints
            // ---------------

            Element wayPointsElement = (Element) configuration.getElementsByTagName("wayPoints").item(0);

            for (int i = 0; i < wayPointsElement.getElementsByTagName("wayPoint").getLength(); i++) {
                Element waypoint = (Element) wayPointsElement.getElementsByTagName("wayPoint").item(i);

                double wayPointLatitude = Double.parseDouble(waypoint.getTextContent().split(",")[0]);
                double wayPointLongitude = Double.parseDouble(waypoint.getTextContent().split(",")[1]);

                long ID = Long.parseLong(waypoint.getAttribute("id"));
                idRemapping.addWayPoint(ID, new GeoPosition(wayPointLatitude, wayPointLongitude));
            }



            // ---------------
            //   Connections
            // ---------------

            if (configuration.getElementsByTagName("connections").getLength() != 0) {
                Element connectionsElement = (Element) configuration.getElementsByTagName("connections").item(0);

                var con = connectionsElement.getElementsByTagName("connection");

                for (int i = 0; i < con.getLength(); i++) {
                    Element connectionNode = (Element) con.item(i);

                    long ID = Long.parseLong(connectionNode.getAttribute("id"));
                    idRemapping.addConnection(ID, new Connection(
                        idRemapping.getNewWayPointId(Long.parseLong(connectionNode.getAttribute("src"))),
                        idRemapping.getNewWayPointId(Long.parseLong(connectionNode.getAttribute("dst")))
                    ));
                }
            }

            simulationRunner.setEnvironment(new Environment(characteristicsMap, mapOrigin, numberOfZones,
                idRemapping.getWayPoints(), idRemapping.getConnections()));

            Environment environment = simulationRunner.getEnvironment();

            // ---------------
            //      Motes
            // ---------------

            Element motes = (Element) configuration.getElementsByTagName("motes").item(0);

            for (int i = 0; i < motes.getElementsByTagName("mote").getLength(); i++) {
                Element moteNode = (Element) motes.getElementsByTagName("mote").item(i);
                environment.addMote(new MoteReader(moteNode, environment).buildMote());
            }
            for (int i = 0; i < motes.getElementsByTagName("userMote").getLength(); i++) {
                Element userMoteNode = (Element) motes.getElementsByTagName("userMote").item(i);
                environment.addMote(new UserMoteReader(userMoteNode, environment).buildMote());
            }


            // ---------------
            //    Gateways
            // ---------------

            Element gateways = (Element) configuration.getElementsByTagName("gateways").item(0);
            Element gatewayNode;

            for (int i = 0; i < gateways.getElementsByTagName("gateway").getLength(); i++) {
                gatewayNode = (Element) gateways.getElementsByTagName("gateway").item(i);
                long devEUI = Long.parseUnsignedLong(XMLHelper.readChild(gatewayNode, "devEUI"));
                Element location = (Element) gatewayNode.getElementsByTagName("location").item(0);
                int xPos = Integer.parseInt(XMLHelper.readChild(location, "xPos"));
                int yPos = Integer.parseInt(XMLHelper.readChild(location, "yPos"));

                int transmissionPower = Integer.parseInt(XMLHelper.readChild(gatewayNode, "transmissionPower"));
                int spreadingFactor = Integer.parseInt(XMLHelper.readChild(gatewayNode, "spreadingFactor"));
                environment.addGateway(new Gateway(devEUI, xPos, yPos, transmissionPower, spreadingFactor, environment));
            }
        } catch (ParserConfigurationException | SAXException | IOException e1) {
            e1.printStackTrace();
        }
    }

    private static boolean hasChild(Element root, String childName) {
        return root.getElementsByTagName(childName).getLength() != 0;
    }


    private static class MoteReader {
        protected Element node;
        protected Environment environment;

        MoteReader(Element moteNode, Environment environment) {
            this.node = moteNode;
            this.environment = environment;
        }

        long getDevEUI() {
            return Long.parseUnsignedLong(XMLHelper.readChild(node, "devEUI"));
        }

        Pair<Integer, Integer> getMapCoordinates() {
            Element location = (Element) node.getElementsByTagName("location").item(0);
            Element waypoint = (Element) location.getElementsByTagName("waypoint").item(0);
            GeoPosition position = idRemapping.getWayPointWithOriginalId(Long.parseLong(waypoint.getAttribute("id")));
            return environment.getMapHelper().toMapCoordinate(position);
        }

        int getTransmissionPower() {
            return Integer.parseInt(XMLHelper.readChild(node, "transmissionPower"));
        }

        int getSpreadingFactor() {
            return Integer.parseInt(XMLHelper.readChild(node, "spreadingFactor"));
        }

        int getEnergyLevel() {
            return Integer.parseInt(XMLHelper.readChild(node, "energyLevel"));
        }

        double getMovementSpeed() {
            return Double.parseDouble(XMLHelper.readChild(node, "movementSpeed"));
        }

        List<MoteSensor> getMoteSensors() {
            Element sensors = (Element) node.getElementsByTagName("sensors").item(0);
            List<MoteSensor> moteSensors = new LinkedList<>();
            for (int i = 0; i < sensors.getElementsByTagName("sensor").getLength(); i++) {
                Element sensornode = (Element) sensors.getElementsByTagName("sensor").item(i);
                moteSensors.add(MoteSensor.valueOf(sensornode.getAttribute("SensorType")));
            }

            return moteSensors;
        }

        Path getPath() {
            Path path = new Path(environment.getGraph());
            Element pathElement = (Element) node.getElementsByTagName("path").item(0);
            for (int i = 0; i < pathElement.getElementsByTagName("connection").getLength(); i++) {
                Element connectionElement = (Element) pathElement.getElementsByTagName("connection").item(i);
                Connection connection = idRemapping.getConnectionWithOriginalId(Long.parseLong(connectionElement.getAttribute("id")));

                path.addPosition(idRemapping.getWayPointWithNewId(connection.getFrom()));
                if (i == pathElement.getElementsByTagName("connection").getLength() - 1) {
                    // Add the last destination
                    path.addPosition(idRemapping.getWayPointWithNewId(connection.getTo()));
                }
            }
            return path;
        }

        Optional<Integer> getStartMovementOffset() {
            if (hasChild(node, "startMovementOffset")) {
                return Optional.of(Integer.parseInt(XMLHelper.readChild(node, "startMovementOffset")));
            }
            return Optional.empty();
        }

        Optional<Integer> getPeriodSendingPacket() {
            if (hasChild(node, "periodSendingPacket")) {
                return Optional.of(Integer.parseInt(XMLHelper.readChild(node, "periodSendingPacket")));
            }
            return Optional.empty();
        }

        Optional<Integer> getStartSendingOffset() {
            if (hasChild(node, "startSendingOffset")) {
                return Optional.of(Integer.parseInt(XMLHelper.readChild(node, "startSendingOffset")));
            }
            return Optional.empty();
        }

        public Mote buildMote() {
            var startMovementOffset = getStartMovementOffset();
            var periodSendingPacket = getPeriodSendingPacket();
            var startSendingOffset = getStartSendingOffset();
            Mote mote;

            if (startMovementOffset.isPresent() && periodSendingPacket.isPresent() && startSendingOffset.isPresent()) {
                mote = MoteFactory.createMote(
                    getDevEUI(),
                    getMapCoordinates().getLeft(),
                    getMapCoordinates().getRight(),
                    getTransmissionPower(),
                    getSpreadingFactor(),
                    getMoteSensors(),
                    getEnergyLevel(),
                    getPath(),
                    getMovementSpeed(),
                    startMovementOffset.get(),
                    periodSendingPacket.get(),
                    startSendingOffset.get(),
                    environment
                );
            } else {
                mote = MoteFactory.createMote(
                    getDevEUI(),
                    getMapCoordinates().getLeft(),
                    getMapCoordinates().getRight(),
                    getTransmissionPower(),
                    getSpreadingFactor(),
                    getMoteSensors(),
                    getEnergyLevel(),
                    getPath(),
                    getMovementSpeed(),
                    environment
                );
            }
            return mote;
        }
    }

    private static class UserMoteReader extends MoteReader {
        protected UserMoteReader(Element moteNode, Environment environment) {
            super(moteNode, environment);
        }


        boolean isActive() {
            return Boolean.parseBoolean(XMLHelper.readChild(node, "userMoteState"));
        }

        GeoPosition getDestination() {
            Element destinationElement = (Element) node.getElementsByTagName("destination").item(0);
            long wayPointId =  Long.parseLong(destinationElement.getAttribute("id"));
            return idRemapping.getWayPointWithOriginalId(wayPointId);
        }

        @Override
        public Mote buildMote() {
            UserMote userMote = MoteFactory.createUserMote(
                getDevEUI(),
                getMapCoordinates().getLeft(),
                getMapCoordinates().getRight(),
                getTransmissionPower(),
                getSpreadingFactor(),
                getMoteSensors(),
                getEnergyLevel(),
                getPath(),
                getMovementSpeed(),
                getStartMovementOffset().get(), // Intentional
                getPeriodSendingPacket().get(), // Intentional
                getStartSendingOffset().get(),  // Intentional
                getDestination(),
                environment
            );
            userMote.setActive(isActive());
            return userMote;
        }
    }
}
