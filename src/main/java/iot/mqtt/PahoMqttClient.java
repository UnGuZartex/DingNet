package iot.mqtt;

import com.google.gson.*;
import iot.lora.BasicFrameHeader;
import iot.lora.EU868ParameterByDataRate;
import iot.lora.FrameHeader;
import iot.lora.RegionalParameter;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * Mqtt client for a real mqtt server. This implementation use the library Paho
 */
public class PahoMqttClient implements MqttClientBasicApi{

    private MqttClient mqttClient;
    private Gson gson;
    private Map<String, List<MqttMessageConsumer>> subscribed = new HashMap<>();

    public PahoMqttClient() {
        this("tcp://test.mosquitto.org:1883", "testFenomeno1995");
    }

    public PahoMqttClient(@NotNull String address, @NotNull String clientId) {
        gson = addAdapters(new GsonBuilder()).create();
        try {
            mqttClient = new MqttClient(address, clientId, new MemoryPersistence());
            connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private GsonBuilder addAdapters(GsonBuilder builder) {

        builder.registerTypeAdapter(FrameHeader.class, (JsonSerializer<FrameHeader>) (header, type, context) -> {
            var obj = new JsonObject();
            obj.addProperty("sourceAddress", Base64.getEncoder().encodeToString(header.getSourceAddress()));
            obj.addProperty("fCtrl", header.getFCtrl());
            obj.addProperty("fCnt", header.getFCntAsShort());
            obj.addProperty("fOpts", Base64.getEncoder().encodeToString(header.getFOpts()));
            return obj;
        });

        builder.registerTypeAdapter(FrameHeader.class, (JsonDeserializer<FrameHeader>) (jsonElement, type, jsonDeserializationContext) -> {
            var header = new BasicFrameHeader();
            header
                .setSourceAddress(Base64.getDecoder().decode(((JsonObject) jsonElement).get("sourceAddress").getAsString()))
                .setFCnt(((JsonObject) jsonElement).get("fCnt").getAsShort())
                .setfCtrl(((JsonObject) jsonElement).get("fCtrl").getAsByte())
                .setFOpts(Base64.getDecoder().decode(((JsonObject) jsonElement).get("fOpts").getAsString()));
            return header;
        });

        builder.registerTypeAdapter(RegionalParameter.class,
            (JsonDeserializer<RegionalParameter>) (element, type, context) -> EU868ParameterByDataRate.valueOf(element.getAsString()));

        return builder;
    }

    @Override
    public void connect() {
        var opt = new MqttConnectOptions();
        opt.setCleanSession(true);
        if (!mqttClient.isConnected()) {
            try {
                mqttClient.connect(opt);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void disconnect() {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
            subscribed.clear();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void publish(String topic, MqttMessageType message) {
        var msg = new MqttMessage(gson.toJson(message).getBytes(US_ASCII));
        try {
            if (!mqttClient.isConnected()) {
                connect();
            }
            mqttClient.publish(topic, msg);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T extends MqttMessageType> void subscribe(Object subscriber, String topicFilter, Class<T> classMessage, BiConsumer<String, T> messageConsumer) {
        if (!subscribed.containsKey(topicFilter)) {
            subscribed.put(topicFilter, new LinkedList<>());
            try {
                mqttClient.subscribe(topicFilter, (topic, msg) -> subscribed.get(topicFilter).forEach(c -> c.accept(topic, msg.toString())));
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        subscribed.get(topicFilter).add(new MqttMessageConsumer<T>(subscriber, messageConsumer, classMessage));
    }

    @Override
    public void unsubscribe(Object subscriber, String topicFilter) {
        subscribed.get(topicFilter).removeIf(c -> c.getSubscriber().equals(subscriber));
        if (subscribed.get(topicFilter).isEmpty()) {
            try {
                mqttClient.unsubscribe(topicFilter);
                subscribed.remove(topicFilter);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private class MqttMessageConsumer<T extends MqttMessageType> {

        private final Object subscriber;
        private final BiConsumer<String, T> consumer;
        private final Class<T> clazz;

        public MqttMessageConsumer(Object subscriber, BiConsumer<String, T> consumer, Class<T> clazz) {
            this.consumer = consumer;
            this.clazz = clazz;
            this.subscriber = subscriber;
        }

        public void accept(String t, String message) {
            consumer.accept(t, gson.fromJson(message, clazz));
        }

        public Object getSubscriber() {
            return subscriber;
        }
    }
}
