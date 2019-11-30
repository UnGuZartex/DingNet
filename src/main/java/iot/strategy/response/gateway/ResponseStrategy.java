package iot.strategy.response.gateway;

import iot.Environment;
import iot.lora.LoraWanPacket;
import iot.networkentity.Gateway;

import java.util.Optional;

/**
 * Strategy to define with which packet a {@link Gateway} has to reply to a {@link iot.networkentity.Mote}
 */
public interface ResponseStrategy {

    /**
     * initialize the strategy
     * @param gateway the instance of the gateway
     * @return this
     */
    ResponseStrategy init(Gateway gateway, Environment environment);

    /**
     *
     * @param applicationEUI the id of the application
     * @param deviceEUI the device to answer
     * @return the packet ro send if present, Optional.empty otherwise
     */
    Optional<LoraWanPacket> retrieveResponse(Long applicationEUI, Long deviceEUI);
}
