package application.pollution;

import application.Application;
import iot.Environment;
import iot.lora.LoraWanPacket;
import iot.lora.MessageType;
import iot.mqtt.Topics;
import iot.mqtt.TransmissionWrapper;
import iot.networkentity.MoteSensor;
import util.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PollutionMonitor extends Application {

    // The pollution grid which keeps the measurements.
    private PollutionGrid pollutionGrid;
    // The environment in which the pollution monitor operates
    private Environment environment;


    public PollutionMonitor(Environment environment, PollutionGrid pollutionGrid) {
        super(List.of(Topics.getNetServerToApp("+", "+")));

        this.pollutionGrid = pollutionGrid;
        this.environment = environment;
    }


    /**
     * Determine the pollution level from a byte array, solely using the data from an IAQ sensor.
     * @param sensorData The considered data.
     * @return A pollution level according to the IAQ sensor reading (or 0.0 if no IAQ sensor data is present).
     */
    private double determinePollutionLevelFromIAQData(Map<MoteSensor, Byte[]> sensorData) {
        // NOTE: only consider IAQ sensors for now
        return sensorData.entrySet().stream()
            .filter(me -> me.getKey().equals(MoteSensor.IAQ))
            .map(Map.Entry::getValue)
            .flatMap(Arrays::stream) // Can do this here since we filter the IAQ sensor (we know it generates a single byte)
            .mapToDouble(b -> (b.intValue() - 1) / 4.0)
            .average()
            .orElse(0.0);
    }

    /**
     * Handle a LoRa packet by extracting the measurements from it and adding them to the pollution grid.
     * @param message The message to be handled.
     */
    private void handleSensorData(LoraWanPacket message) {
        // Filter out the first byte
        var body = Arrays.stream(Converter.toObjectType(message.getPayload()))
            .skip(1) // Skip the first byte since this indicates the message type
            .collect(Collectors.toList());
        if (body.isEmpty()) {
            return;
        }

        var mote = this.environment.getMotes().stream()
            .filter(m -> m.getEUI() == message.getSenderEUI())
            .findFirst()
            .orElseThrow();

        // Retrieve the position of the mote
        // TODO is this position even correct when getting it at this point? Has it changed since the transmission of the data?
        var position = this.environment.getMapHelper().toGeoPosition(mote.getPosInt());

        // Retrieve the individual sensor readings
        Map<MoteSensor, Byte[]> sensorData = this.retrieveSensorData(mote, body);


        // Make sure the IAQ sensor is present in the currently processed mote
        if (!sensorData.containsKey(MoteSensor.IAQ)) {
            return;
        }

        this.pollutionGrid.addMeasurement(message.getSenderEUI(), position, new PollutionLevel(this.determinePollutionLevelFromIAQData(sensorData)));
    }

    @Override
    public void consumePackets(String topicFilter, TransmissionWrapper transmission) {
        var message = transmission.getTransmission().getContent();
        // Only handle packets with a route request
        if (message.getPayload()[0] == MessageType.SENSOR_VALUE.getCode()) {
            handleSensorData(message);
        }
    }
}
