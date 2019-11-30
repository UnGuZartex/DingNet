package iot.networkcommunication.api;


import iot.lora.LoraTransmission;
import iot.lora.LoraWanPacket;
import iot.lora.RegionalParameter;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Interface for a LoRa sender entity
 */
public interface Sender {

    /**
     * Method to send a packet to a set of {@link Receiver}, according to the protocol specification
     * @param packet the packet to send
     * @param receivers the set of receivers
     * @return a copy of the transmission sent, if the packet is sent
     */
    Optional<LoraTransmission> send(LoraWanPacket packet, Set<Receiver> receivers);

    /**
     *
     * @return if the device stay already transmitting a packet
     */
    boolean isTransmitting();

    /**
     *
     * @return queue of packet to send
     */
    List<LoraWanPacket> getSendingQueue();

    /**
     *
     * @return the packet that the device stay transmitting
     */
    LoraWanPacket getTransmittingMessage();

    /**
     * method to abort the current transmission
     */
    void abort();

    /**
     *
     * @param transmissionPower the transmission power for the new transmissions
     * @return this
     */
    Sender setTransmissionPower(double transmissionPower);

    /**
     *
     * @param regionalParameter the regional parameter for the new transmissions
     * @return this
     */
    Sender setRegionalParameter(RegionalParameter regionalParameter);

    /**
     *
     * @return the regional parameter used for the transmission
     */
    RegionalParameter getRegionalParameter();

    /**
     *
     * @return the transmission power used for the transmission
     */
    double getTransmissionPower();

    /**
     * reset the sender to the initial state
     */
    void reset();
}
