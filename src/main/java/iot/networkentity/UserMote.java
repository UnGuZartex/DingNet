package iot.networkentity;

import datagenerator.SensorDataGenerator;
import iot.Environment;
import iot.GlobalClock;
import iot.lora.BasicFrameHeader;
import iot.lora.LoraWanPacket;
import iot.lora.MacCommand;
import iot.lora.MessageType;
import iot.strategy.consume.AddPositionToPath;
import org.jxmapviewer.viewer.GeoPosition;
import util.Converter;
import util.MapHelper;
import util.Path;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UserMote extends Mote {

    // Distance in km
    public static final double DISTANCE_THRESHOLD_ROUNDING_ERROR = 0.001;

    // the user mote can ask for a path only if this property is true
    private boolean isActive = false;
    private GeoPosition destination;
    private final LocalTime whenAskPath = LocalTime.of(0, 0, 15);
    private boolean alreadyRequested;

    UserMote(long DevEUI, int xPos, int yPos, int transmissionPower, int SF,
             List<MoteSensor> moteSensors, int energyLevel, Path path, double movementSpeed,
             int startMovementOffset, int periodSendingPacket, int startSendingOffset, GeoPosition destination, Environment environment) {
        super(DevEUI, xPos, yPos, transmissionPower, SF, moteSensors, energyLevel, path, movementSpeed, startMovementOffset, periodSendingPacket, startSendingOffset, environment);
        this.destination = destination;

        this.initialize();
    }

    @Override
    protected LoraWanPacket composePacket(Byte[] data, Map<MacCommand, Byte[]> macCommands) {
        GlobalClock clock = this.getEnvironment().getClock();

        if (isActive() && !alreadyRequested && whenAskPath.isBefore(clock.getTime())) {
            alreadyRequested = true;
            byte[] payload= new byte[17];
            payload[0] = MessageType.REQUEST_PATH.getCode();
            System.arraycopy(getGPSSensor().generateData(getPosInt(), clock.getTime()), 0, payload, 1, 8);
            System.arraycopy(Converter.toByteArray(destination), 0, payload, 9, 8);
            return new LoraWanPacket(getEUI(), getApplicationEUI(), payload,
                new BasicFrameHeader().setFCnt(incrementFrameCounter()), new LinkedList<>(macCommands.keySet()));
        }
        return LoraWanPacket.createEmptyPacket(getEUI(), getApplicationEUI());
    }

    @Override
    public void setPos(double xPos, double yPos) {
        super.setPos(xPos, yPos);

        Environment environment = this.getEnvironment();

        if (isActive()) {
            if (getPath().isEmpty()) {
                throw new IllegalStateException("I don't have any path to follow...I can't move:(");
            }
            var path = getPath();
            var wayPoints = path.getWayPoints();
            //if I don't the path to the destination and I am at the penultimate position of the path

            if (path.getDestination().isPresent() &&    //at least the path has one point
                MapHelper.distance(path.getDestination().get(), destination) > DISTANCE_THRESHOLD_ROUNDING_ERROR &&
                wayPoints.size() > 1 &&
                environment.getMapHelper().toMapCoordinate(wayPoints.get(wayPoints.size()-2)).equals(getPosInt())) {
                //require new part of path
                askNewPartOfPath();
            }
        }
    }

    private void askNewPartOfPath() {
        if (getPath().getDestination().isEmpty()) {
            throw new IllegalStateException("You can't require new part of path without a previous one");
        }
        byte[] payload= new byte[9];
        payload[0] = MessageType.REQUEST_UPDATE_PATH.getCode();
        System.arraycopy(Converter.toByteArray(getPath().getDestination().get()), 0, payload, 1, 8);
        sendToGateWay(new LoraWanPacket(getEUI(), getApplicationEUI(), payload,
            new BasicFrameHeader().setFCnt(incrementFrameCounter()), new LinkedList<>()));

        var clock = this.getEnvironment().getClock();
        var oldDestination = getPath().getDestination();
        clock.addTriggerOneShot(clock.getTime().plusSeconds(30), () -> {
            if (oldDestination.equals(getPath().getDestination())) {
                askNewPartOfPath();
            }
        });
    }

    private SensorDataGenerator getGPSSensor() {
        return getSensors().stream().filter(s -> s.equals(MoteSensor.GPS)).findFirst().orElseThrow().getSensorDataGenerator();
    }

    public boolean isActive() {
        return isActive;
    }

    /**
     * Setting active a userMote means also set not active all this other userMote
     * @param active true to set active, false otherwise
     */
    public void setActive(boolean active) {
        if (active) {
            this.getEnvironment().getMotes().stream()
                .filter(m -> m instanceof UserMote)
                .map(m -> (UserMote)m)
                .forEach(m -> {
                    m.setActive(false);
                    m.enable(false);
                });
        }
        isActive = active;
    }

    public GeoPosition getDestination() {
        return this.destination;
    }

    public void setDestination(GeoPosition destination) {
        this.destination = destination;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && isActive();
    }

    @Override
    public boolean isArrivedToDestination() {
        return this.getPosInt().equals(this.getEnvironment().getMapHelper().toMapCoordinate(destination));
    }


    @Override
    protected void initialize() {
        super.initialize();

        setPath(new Path(List.of(this.getEnvironment().getMapHelper().toGeoPosition(this.getPosInt())),
            this.getEnvironment().getGraph()));
        this.alreadyRequested = false;
        consumePacketStrategies.add(new AddPositionToPath());
    }
}
