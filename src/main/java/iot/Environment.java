package iot;

import be.kuleuven.cs.som.annotate.Basic;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import iot.networkentity.NetworkEntity;
import org.jxmapviewer.viewer.GeoPosition;
import util.Connection;
import util.GraphStructure;
import util.MapHelper;
import util.Statistics;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A class representing a map of the environment.
 */
public class Environment implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * The max x-coordinate allowed on the map
     */
    private static int maxXpos = -1;
    /**
     * The max y-coordinate allowed on the map
     */
    private static int maxYpos = -1;

    /**
     * The origin of the map
     */
    private final GeoPosition origin;

    /**
     * A list containing all motes currently active on the map.
     */
    private List<Mote> motes = new LinkedList<>();

    /**
     * A list containing all gateways currently active on the map.
     */
    private List<Gateway> gateways = new LinkedList<>();

    /**
     * The actual map containing the characteristics of the environment.
     */
    private Characteristic[][] characteristics;

    /**
     * The number of zones in the configuration.
     */
    private int numberOfZones;

    /**
     * The number of runs with this configuration.
     */
    private int numberOfRuns;

    /**
     * A way to represent the flow of time in the environment.
     */
    private GlobalClock clock;


    private GraphStructure graph;
    private MapHelper mapHelper;


    /**
     * A constructor generating a new environment with a given map with characteristics.
     * @param characteristics   The map with the characteristics of the current environment.
     * @param mapOrigin coordinates of the point [0,0] on the map.
     * @param numberOfZones the number of zones defined in the region.
     * @param wayPoints a map of waypoints (ID -> coordinates).
     * @param connections a map of connections (ID -> connection).
     * @Post    Sets the max x-coordinate to the x size of the map if the map is valid.
     * @Post    Sets the max y-coordinate to the y size of the map if the map is valid.
     * @Post    Sets the characteristics to the given map if the map is valid.
     * @Post    Sets the max x-coordinate to 0 if the map is  not valid.
     * @Post    Sets the max y-coordinate to 0 if the map is not valid.
     * @Post    Sets the characteristics to an empty list if the map is not valid.
     */
    public Environment(Characteristic[][] characteristics, GeoPosition mapOrigin, int numberOfZones,
                       Map<Long, GeoPosition> wayPoints, Map<Long, Connection> connections) {
        if (areValidCharacteristics(characteristics)) {
            maxXpos = characteristics.length - 1;
            maxYpos = characteristics[0].length - 1;
            this.characteristics = characteristics;
        } else {
            throw new IllegalArgumentException("Invalid characteristics given in constructor of Environment.");
        }

        this.numberOfZones = numberOfZones;
        this.origin = mapOrigin;
        this.clock = new GlobalClock();

        this.graph = new GraphStructure(wayPoints, connections);
        this.mapHelper = new MapHelper(this.origin);

        numberOfRuns = 1;
    }

    public static int getMapWidth() {
        if (maxXpos == -1) {
            throw new IllegalStateException("map not already initialized");
        }
        return maxXpos;
    }

    public static int getMapHeight() {
        if (maxYpos == -1) {
            throw new IllegalStateException("map not already initialized");
        }
        return maxYpos;
    }

    /**
     * Returns the clock used by this environment.
     * @return The clock used by this environment.
     */
    public GlobalClock getClock() {
        return clock;
    }

    /**
     * Gets the number of zones.
     * @return The number of zones.
     */
    public int getNumberOfZones() {
        return numberOfZones;
    }

    /**
     * Sets the number of zones.
     * @param numberOfZones the number of zones.
     */
    public void setNumberOfZones(int numberOfZones) {
        this.numberOfZones = numberOfZones;
    }


    public GraphStructure getGraph() {
        return this.graph;
    }

    public MapHelper getMapHelper() {
        return this.mapHelper;
    }



    /**
     * Determines if a x-coordinate is valid on the map
     * @param x The x-coordinate to check
     * @return true if the coordinate is not bigger than the max coordinate
     */
    public boolean isValidXpos(double x) {
        return x >= 0 && x <= getMaxXpos();
    }

    /**
     *
     * @return the max x-coordinate
     */
    @Basic
    public int getMaxXpos() {
        return maxXpos;
    }

    /**
     * Determines if a y-coordinate is valid on the map
     * @param y The y-coordinate to check
     * @return true if the coordinate is not bigger than the max coordinate
     */
    public boolean isValidYpos(double y) {
        return y >= 0 && y <= getMaxYpos();
    }

    /**
     *
     * @return the max y-coordinate
     */
    @Basic
    public int getMaxYpos() {
        return maxYpos;
    }

    /**
     * Returns all the gateways on the map.
     * @return A list with all the gateways on the map.
     */
    @Basic
    public List<Gateway> getGateways() {
        return gateways;
    }

    /**
     * Adds a gateway to the list of gateways if it is located in this environment.
     * @param gateway  the node to add
     * @Post    If the gateway is in this environment, it is added to the list.
     */
    @Basic
    public void addGateway(Gateway gateway) {
        // TODO check if coordinates are within valid bounds (although... is this really necessary?)
        gateways.add(gateway);
    }

    /**
     *
     * @return A list with all the motes on the map.
     */
    @Basic
    public List<Mote> getMotes() {
        return motes;
    }

    /**
     * Adds a mote to the list of motes if it is located in this environment.
     * @param mote  the mote to add
     * @Post    If the mote is in this environment, it is added to the list.
     */
    @Basic
    public void addMote(Mote mote) {
        // TODO check if coordinates are within valid bounds (although... is this really necessary?)
        motes.add(mote);
    }


    /**
     * Retrieve the {@link iot.networkentity.NetworkEntity} with the required Id.
     * @param id The Id of the required entity.
     * @return The entity with the given Id.
     * @throws java.util.NoSuchElementException if no entity is present with the given Id.
     */
    public NetworkEntity getNetworkEntityById(long id) {
        return Stream.concat(this.getMotes().stream(), this.getGateways().stream())
            .filter(ne -> ne.getEUI() == id)
            .findFirst()
            .orElseThrow();
    }

    /**
     * Determines if a given map of characteristics is valid.
     * @param characteristics The map to check.
     * @return  True if the Map is square.
     */
    private boolean areValidCharacteristics(Characteristic[][] characteristics) {
        if (characteristics.length == 0) {
            return false;
        } else if (characteristics[0].length == 0) {
            return false;
        }

        // Make sure that each row has the same length
        int ySize = characteristics[0].length;

        for (Characteristic[] row : characteristics) {
            if (row.length != ySize) {
                return false;
            }
        }

        return true;
    }

    /**
     * returns the characteristics of a given position
     * @param xPos  The x-coordinate of the position.
     * @param yPos  The y-coordinate of the position.
     * @return  the characteristic of the position if the position is valid.
     */
    public Characteristic getCharacteristic(int xPos, int yPos) {
        if (isValidXpos(xPos) && isValidYpos(yPos)) {
            return characteristics[xPos][yPos];
        } else {
            return null;
        }
    }

    /**
     * Sets the characteristic to the given characteristic on the given location.
     * @param characteristic the given characteristic.
     */
    public void setCharacteristics(Characteristic characteristic, int xPos, int yPos) {
        this.characteristics[xPos][yPos] = characteristic;
    }


    /**
     * Returns the coordinates of the point [0,0] on the map.
     * @return The coordinates of the point [0,0] on the map.
     */
    public GeoPosition getMapOrigin() {
        return this.origin;
    }

    /**
     * Returns the geoPosition of the center of the map.
     * @return The geoPosition of the center of the map.
     */
    public GeoPosition getMapCenter() {
        return new GeoPosition(mapHelper.toLatitude(getMaxYpos() / 2),
            mapHelper.toLongitude(getMaxXpos() / 2));
    }



    /**
     * A function that moves a mote to a geoposition 1 step and returns true if the mote has moved.
     * @param mote The mote to move.
     * @param destination The position to move towards.
     */
    public void moveMote(Mote mote, GeoPosition destination) {
        double xPosDest = this.mapHelper.toMapXCoordinate(destination);
        double yPosDest = this.mapHelper.toMapYCoordinate(destination);
        double xPosMote = mote.getXPosDouble();
        double yPosMote = mote.getYPosDouble();

        if (xPosMote != xPosDest || yPosMote != yPosDest) {
            double deltaX = (xPosDest - xPosMote);
            double deltaY = (yPosDest - yPosMote);
            double distance = Math.min(1, Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)));
            var angle = Math.atan(Math.abs(deltaY) / Math.abs(deltaX));

            xPosMote += distance * Math.cos(angle) * (xPosDest > xPosMote ? 1 : -1);
            yPosMote += distance * Math.sin(angle) * (yPosDest > yPosMote ? 1 : -1);

            mote.setPos(xPosMote, yPosMote);
        }
    }


    /**
     * reset all entities in the configuration.
     */
    public void resetHistory() {
        getClock().reset();
        Statistics.getInstance().reset();
        numberOfRuns = 1;
    }

    /**
     * Adds a run to all entities in the configuration.
     */
    public void addRun() {
        getClock().reset();
        Statistics.getInstance().addRun();
        numberOfRuns++;
    }

    /**
     * Returns the number of runs of this configuration.
     * @return The number of runs of this configuration.
     */
    @Basic
    public int getNumberOfRuns() {
        return numberOfRuns;
    }


    /**
     * Shortens the routes of motes which contain the given waypoint.
     * @param wayPointId The ID of the waypoint.
     */
    public void removeWayPointFromMotes(long wayPointId) {
        motes.forEach(o -> o.shortenPathFromWayPoint(wayPointId));
    }

    public void removeConnectionFromMotes(long connectionId) {
        motes.forEach(o -> o.shortenPathFromConnection(connectionId));
    }
}

