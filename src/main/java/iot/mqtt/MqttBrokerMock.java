package iot.mqtt;

import util.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mock that represent a mqtt broker
 */
public class MqttBrokerMock {

    private static final String WILDCARD_SINGLE_LEVEL = "+";
    private static final String WILDCARD_MULTI_LEVEL = "#";
    private static final String LEVEL_SEPARATOR = "/";

    private final Map<MqttMock, List<String>> clientSubscribed;

    private static MqttBrokerMock ourInstance = new MqttBrokerMock();

    /**
     *
     * @return the singleton instance
     */
    public static MqttBrokerMock getInstance() {
        return ourInstance;
    }

    private MqttBrokerMock() {
        clientSubscribed = new HashMap<>();
    }

    /**
     * Method to connect a {@link MqttMock} to this broker
     * @param instance the client
     */
    public void connect(MqttMock instance) {
        if (clientSubscribed.containsKey(instance)) {
            throw new IllegalStateException();
        }
        clientSubscribed.put(instance, new LinkedList<>());
    }

    /**
     * Method to disconnect a {@link MqttMock} to this broker
     * @param instance the client
     */
    public void disconnect(MqttMock instance) {
        if (!clientSubscribed.containsKey(instance)) {
            throw new IllegalStateException();
        }
        clientSubscribed.remove(instance);
    }

    /**
     * method to publish a message
     * @param topic the topic of the message
     * @param message the message to publish
     */
    public void publish(String topic, MqttMessageType message) {
        clientSubscribed.entrySet().stream()
            .map(e -> new Pair<>(e.getKey(), e.getValue()
                .stream()
                .filter(f -> checkTopicMatch(topic, f))
                .collect(Collectors.toList())))
            .forEach(e-> e.getRight().forEach(t -> e.getLeft().dispatch(t, topic, message)));
    }

    /**
     * method to subscribe a {@link MqttMock} to a topic
     * @param instance the client
     * @param topicFilter the topic with possible wildcard
     */
    public void subscribe(MqttMock instance, String topicFilter) {
        if (!clientSubscribed.containsKey(instance)) {
            throw new IllegalStateException();
        }
        clientSubscribed.get(instance).add(topicFilter);
    }

    /**
     * method to unsubscribe a {@link MqttMock} to a previous subscribed topic
     * @param instance the client
     * @param topicFilter the topic previous subscribed
     */
    public void unsubscribe(MqttMock instance, String topicFilter) {
        if (!clientSubscribed.containsKey(instance)) {
            throw new IllegalStateException();
        }
        clientSubscribed.get(instance).remove(topicFilter);
    }

    private boolean checkTopicMatch(final String topic, final String filter) {
        var topicSplitted = topic.split(LEVEL_SEPARATOR);
        var filterSplitted = filter.split(LEVEL_SEPARATOR);
        int index = 0;
        while (index < topicSplitted.length && index < filterSplitted.length &&
            (topicSplitted[index].equals(filterSplitted[index]) || filterSplitted[index].equals(WILDCARD_SINGLE_LEVEL))) {
            index++;
        }

        return (index == filterSplitted.length && index == topicSplitted.length) ||
            (index == filterSplitted.length - 1 && filterSplitted[index].equals(WILDCARD_MULTI_LEVEL));
    }
}
