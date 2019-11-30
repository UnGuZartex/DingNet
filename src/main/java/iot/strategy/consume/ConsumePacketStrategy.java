package iot.strategy.consume;

import iot.lora.LoraWanPacket;
import iot.networkentity.Mote;

/**
 * Consumer of a {@link LoraWanPacket} received from a {@link Mote}
 */
@FunctionalInterface
public interface ConsumePacketStrategy {

    /**
     *
     * @param mote the instance of the mote that have received the packet
     * @param packet the received packet
     */
    void consume(Mote mote, LoraWanPacket packet);
}
