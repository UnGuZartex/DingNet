package iot.strategy.store;

import iot.lora.LoraWanPacket;

import java.util.Optional;

/**
 * Strategy to define how manage the packet received from a {@link iot.networkentity.Mote}
 */
public interface ReceivedPacketStrategy {

    /**
     * manage the new received packet
     * @param packet the new packet
     */
    void addReceivedMessage(LoraWanPacket packet);

    /**
     *
     * @return true if there are packet to manage
     */
    boolean hasPackets();

    /**
     *
     * @return the received packet to manage if present, an Optional.empty otherwise
     */
    Optional<LoraWanPacket> getReceivedPacket();

    /**
     * reset strategy
     */
    void reset();
}
