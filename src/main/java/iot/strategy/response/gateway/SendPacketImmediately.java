package iot.strategy.response.gateway;


import iot.Environment;
import iot.lora.LoraWanPacket;
import iot.mqtt.LoraWanPacketWrapper;
import iot.mqtt.Topics;
import iot.networkentity.Gateway;
import util.Pair;

import java.util.Optional;

/**
 * Strategy to send immediately a packet after received it on mqtt topic
 */
public class SendPacketImmediately implements ResponseStrategy {

    private Gateway gateway;
    private Environment environment;

    @Override
    public ResponseStrategy init(Gateway gateway, Environment environment) {
        this.gateway = gateway;
        this.environment = environment;
        //subscribe to all mote topic
        subscribeToMotesTopic();
        return this;
    }

    private void subscribeToMotesTopic() {
        environment.getMotes().stream()
            .map(m -> new Pair<>(m.getApplicationEUI(), m.getEUI()))
            .forEach(m -> gateway.getMqttClient().subscribe(
                this,
                Topics.getNetServerToGateway(m.getLeft(), gateway.getEUI(), m.getRight()),
                LoraWanPacketWrapper.class,
                (t, msg) -> gateway.sendToDevice(msg.getPacket())
            ));
    }

    @Override
    public Optional<LoraWanPacket> retrieveResponse(Long applicationEUI, Long deviceEUI) {
        return Optional.empty();
    }
}
