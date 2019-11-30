package iot.strategy.store;

import iot.lora.LoraWanPacket;

import java.util.Optional;

/**
 * Strategy to maintain the newest packet according to the frame counter of the {@link iot.lora.FrameHeader}
 */
public class MaintainLastPacket implements ReceivedPacketStrategy {

    private int lastPacketReceived = -1;
    private LoraWanPacket packet;

    @Override
    public void addReceivedMessage(LoraWanPacket packet) {
        if (packet.getFrameHeader().getFCntAsShort() > lastPacketReceived) {
            lastPacketReceived = packet.getFrameHeader().getFCntAsShort();
            this.packet = packet;
        }
    }

    @Override
    public boolean hasPackets() {
        return packet != null;
    }

    @Override
    public Optional<LoraWanPacket> getReceivedPacket() {
        var tmp = Optional.ofNullable(packet);
        packet = null;
        return tmp;
    }

    @Override
    public void reset() {
        this.lastPacketReceived = -1;
        this.packet = null;
    }
}
