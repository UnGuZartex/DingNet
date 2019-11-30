package application;

import iot.mqtt.MQTTClientFactory;
import iot.mqtt.MqttClientBasicApi;
import iot.mqtt.TransmissionWrapper;
import iot.networkentity.Mote;
import iot.networkentity.MoteSensor;
import iot.networkentity.NetworkServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * An abstract class for an application in the system.
 */
public abstract class Application {
    /**
     * An MQTT client used to communicate with the {@link NetworkServer}.
     */
    protected MqttClientBasicApi mqttClient;


    protected Application(List<String> topics) {
        this.mqttClient = MQTTClientFactory.getSingletonInstance();
        topics.forEach(t -> this.mqttClient.subscribe(this, t, TransmissionWrapper.class, this::consumePackets));
    }


    /**
     * Method which is called when the application recieves a message from the MQTT broker.
     * @param topicFilter The topic on which the message was published.
     * @param message The message that was published.
     */
    public abstract void consumePackets(String topicFilter, TransmissionWrapper message);


    /**
     * Conversion function which converts a message of bytes to the corresponding mote sensor measurements
     * @param mote The mote from which the message originated.
     * @param messageBody The raw message data containing the sensor measurements.
     * @return A map from the sensor types to the data/measurements they produced.
     */
    protected Map<MoteSensor, Byte[]> retrieveSensorData(Mote mote, List<Byte> messageBody) {
        Map<MoteSensor, Byte[]> sensorData = new HashMap<>();

        for (var sensor : mote.getSensors()) {
            int amtBytes = sensor.getAmountOfData();
            sensorData.put(sensor, messageBody.subList(0, amtBytes).toArray(Byte[]::new));
            messageBody = messageBody.subList(amtBytes, messageBody.size());
        }

        return sensorData;
    }


    /**
     * Destructor which can be used to properly destruct applications.
     */
    public void destruct() {}
}
