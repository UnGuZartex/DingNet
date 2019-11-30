package iot.lora;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 *  LoRa receiver sensitivity in dBm at different bandwidths and spreading factors.
 *  taken from DOI: 10.3390/s16091466
 */
public enum RxSensitivity {
    BW125SF7(7, 125, -123),
    BW125SF8(8, 125, -126),
    BW125SF9(9, 125, -129),
    BW125SF10(10, 125, -132),
    BW125SF11(11, 125, -133),
    BW125SF12(12, 125, -136);

    private final int spreadingFactor;
    private final int bandwidth;
    private final int receiverSensitivity;

    RxSensitivity(int spreadingFactor, int bandwidth, int receiverSensitivity) {
        this.spreadingFactor = spreadingFactor;
        this.bandwidth = bandwidth;
        this.receiverSensitivity = receiverSensitivity;
    }

    public int getSpreadingFactor() {
        return spreadingFactor;
    }

    /**
     *
     * @return bandwidth in kHz
     */
    public int getBandwidth() {
        return bandwidth;
    }

    /**
     *
     * @return sensitivity in dBm
     */
    public int getReceiverSensitivity() {
        return receiverSensitivity;
    }

    /**
     *
     * @return sensitivity in dBm
     */
    public static int getReceiverSensitivity(RegionalParameter parameters) {
        return getReceiverSensitivity(parameters.getSpreadingFactor(), parameters.getBandwidth());
    }

    /**
     *
     * @return sensitivity in dBm
     */
    public static int getReceiverSensitivity(int spreadingFactor, int bandwidth) {
        return stream()
            .filter(s -> s.getSpreadingFactor() == spreadingFactor)
            .filter(s -> s.getBandwidth() == bandwidth)
            .findFirst()
            .orElseThrow()
            .getReceiverSensitivity();
    }

    public static Stream<RxSensitivity> stream() {
        return Arrays.stream(values());
    }
}
