package iot.strategy.response.gateway;


import iot.Environment;
import iot.lora.LoraWanPacket;
import iot.networkentity.Gateway;

import java.util.LinkedList;
import java.util.Optional;

/**
 * Strategy to send a dummy a packet every 50 packet received
 */
public class DummyResponse implements ResponseStrategy {

    private Gateway gateway;
    private int count = 0;

    @Override
    public ResponseStrategy init(Gateway gateway, Environment environment) {
        this.gateway = gateway;
        return this;
    }

    @Override
    public Optional<LoraWanPacket> retrieveResponse(Long applicationEUI, Long deviceEUI) {
        LoraWanPacket resp = null;
        if (count++ % 50 == 0) {
            resp = new LoraWanPacket(gateway.getEUI(), deviceEUI, new byte[]{(byte)(count/50)}, new LinkedList<>());
        }
        return Optional.ofNullable(resp);
    }
}
