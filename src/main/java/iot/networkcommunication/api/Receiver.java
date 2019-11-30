package iot.networkcommunication.api;

import iot.lora.LoraTransmission;
import util.Pair;

import java.util.function.Consumer;

/**
 * Interface for a LoRa receiver entity
 */
public interface Receiver {

    /**
     *
     * @return Id of the receiver
     */
    long getID();

    /**
     *
     * @param packet the transmission received
     */
    void receive(LoraTransmission packet);

    /**
     *
     * @return receiver position as double
     */
    Pair<Double, Double> getReceiverPosition();

    /**
     *
     * @return receiver position as int
     */
    Pair<Integer, Integer> getReceiverPositionAsInt();

    /**
     * Setter for the consumer for all the received transmission
     * @param consumerPacket the consumer
     * @return this
     */
    Receiver setConsumerPacket(Consumer<LoraTransmission> consumerPacket);

    /**
     * reset the receiver to the initial state
     */
    void reset();
}
