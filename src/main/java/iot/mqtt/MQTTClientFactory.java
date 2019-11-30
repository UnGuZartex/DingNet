package iot.mqtt;

/**
 * Factory to retrieve an instance of {@link MqttClientBasicApi}
 */
public class MQTTClientFactory {

    private enum MqttClientType {PAHO, MOCK}
    private static MqttClientType DEFAULT_INSTANCE_TYPE = MqttClientType.MOCK;
    private static MqttClientBasicApi clientBasicApi;

    /**
     *
     * @return the singleton instance of {@link MqttClientBasicApi} of the predefined type {@link MqttClientType}
     */
    public static MqttClientBasicApi getSingletonInstance() {
        if (clientBasicApi == null) {
            switch (DEFAULT_INSTANCE_TYPE) {
                case PAHO: {
                    clientBasicApi = createPahoClient();
                } break;
                case MOCK: {
                    clientBasicApi = createMockClient();
                } break;
            }
        }
        return clientBasicApi;
    }

    /**
     *
     * @return a new instance of a mock {@link MqttClientBasicApi}
     */
    public static MqttMock createMockClient() {
        return new MqttMock();
    }

    /**
     *
     * @return a new instance of a Paho {@link MqttClientBasicApi}
     */
    public static PahoMqttClient createPahoClient() {
        return new PahoMqttClient();
    }
}
