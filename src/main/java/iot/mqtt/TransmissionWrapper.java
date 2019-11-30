package iot.mqtt;

import iot.lora.LoraTransmission;

/**
 * Wrapper of a {@link LoraTransmission} as mqtt message.
 * It is used for the communications from {@link iot.networkentity.Gateway} to {@link iot.networkentity.NetworkServer} to {@link application.Application}
 */
public class TransmissionWrapper implements MqttMessageType {

    private final LoraTransmission transmission;

    public TransmissionWrapper(LoraTransmission transmission) {
        this.transmission = transmission;
    }

    public LoraTransmission getTransmission() {
        return transmission;
    }
}
