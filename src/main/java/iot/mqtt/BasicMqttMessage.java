package iot.mqtt;

import iot.lora.MacCommand;
import util.Converter;

import java.util.LinkedList;
import java.util.List;

/**
 * MqttMessage that represent a message from an {@link application.Application} to the {@link iot.networkentity.NetworkServer}
 */
public class BasicMqttMessage implements MqttMessageType {

    private final List<Byte> data;
    private List<MacCommand> macCommands;

    public BasicMqttMessage(List<Byte> data) {
        this(data, new LinkedList<>());
    }

    public BasicMqttMessage(List<Byte> data, List<MacCommand> macCommands) {
        this.data = data;
        this.macCommands = macCommands;
    }

    public List<Byte> getData() {
        return data;
    }

    public byte[] getDataAsArray() {
        return Converter.toRowType(data.toArray(new Byte[0]));
    }

    public List<MacCommand> getMacCommands() {
        return macCommands;
    }

    public BasicMqttMessage setMacCommands(List<MacCommand> macCommands) {
        this.macCommands = macCommands;
        return this;
    }
}
