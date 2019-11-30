package iot.lora;

import java.util.Base64;

/**
 * Interface that represent the frame header at MAC level
 */
public interface FrameHeader {

    /**
     *
     * @return the address of the sender as byte array of size 4
     */
    byte[] getSourceAddress();

    default String getSourceAddressAsString() {
        return Base64.getEncoder().encodeToString(getSourceAddress());
    }

    byte getFCtrl();

    /**
     *
     * @return frame counter as byte array of size 2
     */
    byte[] getFCnt();

    /**
     *
     * @return frame counter as short
     */
    short getFCntAsShort();

    /**
     *
     * @return byte array with frame options. It could be empty
     */
    byte[] getFOpts();

    /**
     *
     * @return true if the header contains at least one option, false otherwise
     */
    default boolean hasOptions() {
        return getFOpts().length > 0;
    }
}
