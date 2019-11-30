package iot.strategy.response.gateway;

import iot.Environment;
import iot.lora.LoraWanPacket;
import iot.networkentity.Gateway;

import java.util.Optional;

/**
 * Strategy to never replay to a device
 */
public class NoResponse implements ResponseStrategy {
    @Override
    public ResponseStrategy init(Gateway gateway, Environment environment) {
        return this;
    }

    @Override
    public Optional<LoraWanPacket> retrieveResponse(Long applicationEUI, Long deviceEUI) {
        return Optional.empty();
    }
}
