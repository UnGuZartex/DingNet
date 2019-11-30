package iot.mqtt;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Class of utils to manage all the mqtt topics
 */
public class Topics {

    private final static String UPSTREAM_SUFFIX = "/rx";
    private final static String DOWNSTREAM_SUFFIX = "/tx";
    private final static String MOTE_ID = "node";
    private final static String GATEWAY_ID = "gateway";
    private final static String APPLICATION_ID = "application";

    public static String getGatewayToNetServer(long applicationId, long gatewayId, long nodeId) {
        return getGatewayToNetServer(""+applicationId, ""+gatewayId, ""+nodeId);
    }

    public static String getGatewayToNetServer(String applicationId, String gatewayId, String nodeId) {
        return createTopicWithGateway(applicationId, gatewayId, nodeId, UPSTREAM_SUFFIX);
    }

    public static String getNetServerToGateway(long applicationId, long gatewayId, long nodeId) {
        return getNetServerToGateway(""+applicationId, ""+gatewayId, ""+nodeId);
    }

    public static String getNetServerToGateway(String applicationId, String gatewayId, String nodeId) {
        return createTopicWithGateway(applicationId, gatewayId, nodeId, DOWNSTREAM_SUFFIX);
    }

    public static String getNetServerToApp(long applicationId, long nodeId) {
        return getNetServerToApp(""+applicationId, ""+nodeId);
    }

    public static String getNetServerToApp(String applicationId, String nodeId) {
        return createTopic(applicationId, nodeId, UPSTREAM_SUFFIX);
    }

    public static String getAppToNetServer(long applicationId, long nodeId) {
        return getAppToNetServer(""+applicationId, ""+nodeId);
    }

    public static String getAppToNetServer(String applicationId, String nodeId) {
        return createTopic(applicationId, nodeId, DOWNSTREAM_SUFFIX);
    }

    private static String createTopic(String applicationId, String nodeId, String suffix) {
        return new StringBuilder()
            .append(APPLICATION_ID)
            .append("/")
            .append(applicationId)
            .append("/")
            .append(MOTE_ID)
            .append("/")
            .append(nodeId)
            .append(suffix)
            .toString();
    }

    private static String createTopicWithGateway(String applicationId, String gatewayId, String nodeId, String suffix) {
        return new StringBuilder()
            .append(APPLICATION_ID)
            .append("/")
            .append(applicationId)
            .append("/")
            .append(GATEWAY_ID)
            .append("/")
            .append(gatewayId)
            .append("/")
            .append(MOTE_ID)
            .append("/")
            .append(nodeId)
            .append(suffix)
            .toString();
    }

    /**
     *
     * @param topic
     * @return the mote id inside the topic
     */
    public static long getMote(String topic) {
        return getId(MOTE_ID, topic);
    }

    /**
     *
     * @param topic
     * @return the gateway id inside the topic
     */
    public static long getGateway(String topic) {
        return getId(GATEWAY_ID, topic);
    }

    /**
     *
     * @param topic
     * @return the application id inside the topic
     */
    public static long getApp(String topic) {
        return getId(APPLICATION_ID, topic);
    }

    private static long getId(String idName, String topic) {
        var list = Arrays.asList(topic.split("/"));
        var index = list.indexOf(idName);
        if (index < 0 || index >= list.size()-1) {
            throw new NoSuchElementException("required id not found: " + idName + " in the topic: " + topic);
        }
        return Long.valueOf(list.get(index + 1));
    }
}
