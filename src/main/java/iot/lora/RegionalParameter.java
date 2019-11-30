package iot.lora;

/**
 * Interface that represent the regional parameter used by a LoRa device to send a packet
 */
public interface RegionalParameter {

    int getDataRate();

    int getSpreadingFactor();

    int getBandwidth();

    int getBitRate();

    int getMaximumPayloadSize();
}
