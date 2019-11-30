package iot.lora;


import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class LoraWanPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * The payload of a packet.
     */
    private final byte[] payload;
    /**
     * if the packet has low data rate optimization.
     */
    private final boolean lowDataRateOptimization;
    /**
     * The coding rate of the packet
     */
    private final double codingRate;
    /**
     * The amount of preamble symbols of the packet
     */
    private final int amountOfPreambleSymbols;
    /**
     * The length in symbols of the packet.
     */
    private final int length;
    /**
     * The MAC commands included in the packet.
     */
    private final List<MacCommand> macCommands;
    /**
     * The EUI of designated receiver.
     */
    private final long designatedReceiverEUI;
    /**
     * The EUI of the sender.
     */
    private final long senderEUI;

    /**
     * header of he packet
     */
    private final FrameHeader header;

    //region constructor
    /**
     * A constructor generating a packet with a given payload, header, lowDataRateOptimization, amountOfPreambleSymbols,
     * codingRate and macCommands.
     * @param senderEUI
     * @param designatedReceiverEUI
     * @param payload
     * @param header
     * @param lowDataRateOptimization
     * @param amountOfPreambleSymbols
     * @param codingRate
     * @param macCommands
     */
    public LoraWanPacket(long senderEUI, long designatedReceiverEUI, byte[] payload, FrameHeader header, boolean lowDataRateOptimization,
                         int amountOfPreambleSymbols, double codingRate, List<MacCommand> macCommands) {
        this.senderEUI = senderEUI;
        this.designatedReceiverEUI = designatedReceiverEUI;
        this.amountOfPreambleSymbols = amountOfPreambleSymbols;
        this.codingRate = codingRate;
        this.header = header;
        this.lowDataRateOptimization = lowDataRateOptimization;
        this.payload = payload;
        this.macCommands = macCommands;
        this.length = payload.length + 13;
    }

    /**
     * A constructor generating a packet with a given payload and macCommands;
     * @param senderEUI
     * @param designatedReceiverEUI
     * @param payload
     * @param header
     * @param macCommands
     */
    public LoraWanPacket(long senderEUI, long designatedReceiverEUI, byte[] payload, FrameHeader header, List<MacCommand> macCommands) {
        this(senderEUI, designatedReceiverEUI, payload, header,false,8,0.8,macCommands);
    }

    /**
     * A constructor generating a packet with a given payload and macCommands;
     * @param senderEUI
     * @param designatedReceiverEUI
     * @param payload
     * @param macCommands
     */
    public LoraWanPacket(long senderEUI, long designatedReceiverEUI, byte[] payload, List<MacCommand> macCommands) {
        this(senderEUI, designatedReceiverEUI, payload, new BasicFrameHeader(), false, 8, 0.8, macCommands);
    }

    /**
     * Create an empty packet
     * @param senderEUI the sender
     * @param designatedReceiverEUI the receiver
     * @return the packet
     */
    public static LoraWanPacket createEmptyPacket(long senderEUI, long designatedReceiverEUI) {
        return new LoraWanPacket(senderEUI, designatedReceiverEUI, new byte[0], new LinkedList<>());
    }

    //endregion

    /**
     *
     * @return the header of the packet
     */
    public FrameHeader getFrameHeader() {
        return header;
    }

    /**
     * Returns the MAC commands
     * @return the MAC commands.
     */
    public List<MacCommand> getMacCommands() {
        return macCommands;
    }

    /**
     * Returns the payload
     * @return the payload
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Returns the EUI of designated receiver.
     * @return The EUI of designated receiver.
     */
    public long getReceiverEUI() {
        return designatedReceiverEUI;
    }

    /**
     * Returns the EUI of sender.
     * @return The EUI of sender.
     */
    public long getSenderEUI() {
        return senderEUI;
    }

    /**
     * Returns if the packet has an explicit header.
     * @return if the packet has an explicit header.
     */
    public boolean hasHeader() {
        return true;//TODO
    }

    /**
     * Returns if the packet has low data rate optimization.
     * @return if the packet has low data rate optimization.
     */
    public boolean hasLowDataRateOptimization() {
        return lowDataRateOptimization;
    }

    /**
     * Returns the coding rate.
     * @return the coding rate.
     */
    public double getCodingRate() {
        return codingRate;
    }

    /**
     * Returns the amount of preamble symbols.
     * @return the amount of preamble symbols.
     */
    public int getAmountOfPreambleSymbols() {
        return amountOfPreambleSymbols;
    }

    /**
     * Returns the length in symbols.
     * @return the length in symbols.
     */
    public int getLength() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoraWanPacket that = (LoraWanPacket) o;
        return designatedReceiverEUI == that.designatedReceiverEUI &&
            getSenderEUI() == that.getSenderEUI() &&
            Arrays.equals(getPayload(), that.getPayload()) &&
            getMacCommands().equals(that.getMacCommands()) &&
            getFrameHeader().equals(that.getFrameHeader());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getMacCommands(), designatedReceiverEUI, getSenderEUI(), getFrameHeader());
        result = 31 * result + Arrays.hashCode(getPayload());
        return result;
    }
}
