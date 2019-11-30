package iot.strategy.consume;


import iot.lora.LoraWanPacket;
import iot.networkentity.Mote;

import java.util.Arrays;

/**
 * Print the payload of the received packet
 */
public class DummyConsumer implements ConsumePacketStrategy {
    @Override
    public void consume(Mote mote, LoraWanPacket packet) {
        System.out.println(Arrays.toString(packet.getPayload()));
    }
}
