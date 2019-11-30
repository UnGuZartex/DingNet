package iot.lora;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Set of regional parameter valid in Europe. Class of parameter: EU868
 */
public enum EU868ParameterByDataRate implements RegionalParameter {

    DATA_RATE_0(0, 12, 125, 250, 59),
    DATA_RATE_1(1, 11, 125, 440, 59),
    DATA_RATE_2(2, 10, 125, 980, 59),
    DATA_RATE_3(3, 9, 125, 1760, 123),
    DATA_RATE_4(4, 8, 125, 3125, 230),
    DATA_RATE_5(5, 7, 125, 5470, 230);

    private final int dataRate;
    private final int spreadingFactor;
    private final int bandwidth;
    private final int bitRate;
    private final int maxPayloadSize;

    EU868ParameterByDataRate(int dataRate, int spreadingFactor, int bandwidth, int bitRate, int maxPayloadSize) {
        this.dataRate = dataRate;
        this.spreadingFactor = spreadingFactor;
        this.bandwidth = bandwidth;
        this.bitRate = bitRate;
        this.maxPayloadSize = maxPayloadSize;
    }

    @Override
    public int getDataRate() {
        return dataRate;
    }

    @Override
    public int getSpreadingFactor() {
        return spreadingFactor;
    }

    @Override
    public int getBandwidth() {
        return bandwidth;
    }

    @Override
    public int getBitRate() {
        return bitRate;
    }

    @Override
    public int getMaximumPayloadSize() {
        return maxPayloadSize;
    }

    public static Stream<RegionalParameter> valuesAsStream() {
        return Arrays.stream(EU868ParameterByDataRate.values());
    }

    public static List<RegionalParameter> valuesAsList() {
        return List.of(EU868ParameterByDataRate.values());
    }
}
