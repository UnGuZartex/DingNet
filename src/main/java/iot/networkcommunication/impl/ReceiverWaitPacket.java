package iot.networkcommunication.impl;

import iot.GlobalClock;
import iot.lora.LoraTransmission;
import iot.networkcommunication.api.Receiver;
import iot.networkentity.NetworkEntity;
import util.Pair;
import util.TimeHelper;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ReceiverWaitPacket implements Receiver {

    // The levels of power in between which it can discriminate.
    private final double transmissionPowerThreshold;
    private Consumer<LoraTransmission> consumerPacket;

    private List<LoraTransmission> transmissions = new LinkedList<>();

    private GlobalClock clock;

    private final NetworkEntity receiver;

    public ReceiverWaitPacket(NetworkEntity receiver, double transmissionPowerThreshold, GlobalClock clock) {
        this.transmissionPowerThreshold = transmissionPowerThreshold;
        this.receiver = receiver;
        this.clock = clock;
    }

    @Override
    public long getID() {
        return receiver.getEUI();
    }

    @Override
    public void receive(LoraTransmission transmission) {
        var collidedTransmission = transmissions.stream()
            .filter(t -> collision(transmission, t))
            .collect(Collectors.toList());
        collidedTransmission.forEach(LoraTransmission::setCollided);
        if (!collidedTransmission.isEmpty()) {
            transmission.setCollided();
        }
        transmissions.add(transmission);
        clock.addTriggerOneShot(transmission.getDepartureTime().plus((long)transmission.getTimeOnAir(), ChronoUnit.MILLIS),()->{
            transmission.setArrived();
            consumerPacket.accept(transmission);
        });
    }

    /**
     * Checks if two packets collide according to the model
     * @param a The first packet.
     * @param b The second packet.
     * @return true if the packets collide, false otherwise.
     */
    private boolean collision(LoraTransmission a, LoraTransmission b) {
        return a.getSpreadingFactor() == b.getSpreadingFactor() &&     //check spreading factor
            a.getTransmissionPower() - b.getTransmissionPower() < transmissionPowerThreshold && //check transmission power
            Math.abs(Duration.between(a.getDepartureTime().plusNanos(TimeHelper.miliToNano((long)a.getTimeOnAir()) / 2), //check time on air
                b.getDepartureTime().plusNanos(TimeHelper.miliToNano((long)b.getTimeOnAir()) / 2)).toNanos())
                < TimeHelper.miliToNano((long)a.getTimeOnAir()) / 2 + TimeHelper.miliToNano((long)b.getTimeOnAir()) / 2;
    }

    @Override
    public Pair<Double, Double> getReceiverPosition() {
        return new Pair<>(receiver.getXPosDouble(), receiver.getYPosDouble());
    }

    @Override
    public Pair<Integer, Integer> getReceiverPositionAsInt() {
        return receiver.getPosInt();
    }

    @Override
    public Receiver setConsumerPacket(Consumer<LoraTransmission> consumerPacket) {
        this.consumerPacket = consumerPacket;
        return this;
    }

    @Override
    public void reset() {
        transmissions.clear();
    }
}
