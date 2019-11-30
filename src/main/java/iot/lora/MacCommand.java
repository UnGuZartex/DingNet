package iot.lora;

import be.kuleuven.cs.som.annotate.Basic;

/**
 * An enum representing MAC Commands.
 */
public enum MacCommand {
    ResetInd(1), ResetConf(1), LinkCheckReq(0), LinkCheckAns(1), LinkADRReq(4), LinkADRAns(1), DutyCycleReq(1),
    DutyCycleAns(0), RXParamSetupReq(4), RXParamSetupAns(1), DevStatusReq(0), DevStatusAns(2), NewChannelReq(5),
    NewChannelAns(1), DlChannelReq(4), DlChannelAns(1), RXTimingSetupReq(1), RXTimingSetupAns(0), TxParamSetupReq(1),
    TxParamSetupAns(0), RekeyInd(1), RekeyConf(1), ADRParamSetupReq(1), ADRParamSetupAns(0), DeviceTimeReq(0),
    DeviceTimeAns(5),ForceRejoinReq(2), RejoinParamSetupReq(1), RejoinParamSetupAns(1);

    /**
    The amount of bytes the command needs.
     */
    private final int length;

    MacCommand(int length) {
        this.length = length;
    }
    @Basic
    public int getLength() {
        return length;
    }
}
