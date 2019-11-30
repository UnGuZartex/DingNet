package iot.mqtt;

import java.util.function.BiConsumer;

/**
 * Interface with basic API for a mqtt client
 */
public interface MqttClientBasicApi {

    /**
     * To connect to the server
     */
    void connect();

    /**
     * To disconnect to server
     */
    void disconnect();

    /**
     * Publish a message with a specified topic
     * @param topic the message topic
     * @param message the message
     */
    void publish(String topic, MqttMessageType message);

    /**
     * Subscribe to all the topic that start with topicFilter
     * @param subscriber instance oh the subscriber
     * @param topicFilter the topic filter with support to the wildcard '+' and '#'
     * @param classMessage the class of the message that will be receive
     * @param messageConsumer consumer for the message already converted to the required class
     * @param <T> Type of the received message on this topic
     */
    <T extends MqttMessageType> void subscribe(Object subscriber, String topicFilter, Class<T> classMessage, BiConsumer<String, T> messageConsumer);

    /**
     * Unsubscribe a topic previous subscribed
     * @param subscriber the subscriber
     * @param topicFilter the topic previous subscribed
     */
    void unsubscribe(Object subscriber, String topicFilter);
}
