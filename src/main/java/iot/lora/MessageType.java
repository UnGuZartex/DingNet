package iot.lora;

/**
 * Enum to encode the type of a message sent by a device
 */
public enum MessageType {

    SENSOR_VALUE(0),
    REQUEST_PATH(1),
    REQUEST_UPDATE_PATH(2),
    KEEPALIVE(3);

    private final byte code;

    MessageType(byte code) {
        this.code = code;
    }

    MessageType(int code) {
        this((byte)code);
    }

    public byte getCode() {
        return code;
    }
}
