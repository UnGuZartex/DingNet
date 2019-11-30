package iot.strategy.response.gateway;


import iot.Environment;
import iot.lora.LoraWanPacket;
import iot.mqtt.LoraWanPacketWrapper;
import iot.mqtt.Topics;
import iot.networkentity.Gateway;
import util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Strategy to reply with the last packet received on the Mqtt topic
 */
public class SendNewestPacket implements ResponseStrategy {

    //map <appEUI, devEUI> -> buffered packet
    private final Map<Pair<Long, Long>, LoraWanPacket> packetBuffer;
    private Gateway gateway;
    private Environment environment;

    public SendNewestPacket() {
        packetBuffer = new HashMap<>();
    }

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
                (t, msg) -> packetBuffer.put(m, msg.getPacket())
            ));
    }

    @Override
    public Optional<LoraWanPacket> retrieveResponse(Long applicationEUI, Long deviceEUI) {
        return Optional.ofNullable(packetBuffer.remove(new Pair<>(applicationEUI, deviceEUI)));
    }
}
