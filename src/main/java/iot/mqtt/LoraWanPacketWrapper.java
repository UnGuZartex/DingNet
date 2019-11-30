package iot.mqtt;

import iot.lora.LoraWanPacket;

/**
 * Wrapper of a {@link LoraWanPacket} as mqtt message.
 * It is used for the communications from {@link iot.networkentity.NetworkServer} to {@link iot.networkentity.Gateway}
 */
public class LoraWanPacketWrapper implements MqttMessageType {

    private final LoraWanPacket packet;

    public LoraWanPacketWrapper(LoraWanPacket packet) {
        this.packet = packet;
    }

    public LoraWanPacket getPacket() {
        return packet;
    }
}
