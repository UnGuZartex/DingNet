package selfadaptation.feedbackloop;

import be.kuleuven.cs.som.annotate.Basic;
import be.kuleuven.cs.som.annotate.Model;
import iot.QualityOfService;
import iot.lora.LoraTransmission;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import selfadaptation.adaptationgoals.IntervalAdaptationGoal;
import selfadaptation.instrumentation.FeedbackLoopGatewayBuffer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A class representing the signal based adaptation approach.
 */
public class SignalBasedAdaptation extends GenericFeedbackLoop {
    /**
     * A HashMap representing the buffers for the approach.
     */
    @Model
    private Map<Mote, List<Double>> reliableMinPowerBuffers;

    /**
     * A map to keep track of which gateway has already sent the packet.
     */
    @Model
    private FeedbackLoopGatewayBuffer gatewayBuffer;

    /**
     * A QualityOfService representing the required quality of service.
     */
    @Model
    private QualityOfService qualityOfService;



    /**
     * Constructs a new instance of the signal based adaptation approach with a given quality of service.
     * @param qualityOfService The quality of service for the received signal strength.
     */
    public SignalBasedAdaptation(QualityOfService qualityOfService) {
        super("Signal-based");
        this.qualityOfService = qualityOfService;
        gatewayBuffer = new FeedbackLoopGatewayBuffer();
        reliableMinPowerBuffers = new HashMap<>();
    }


    /**
     * Returns the algorithm buffers.
     * @return The algorithm buffers.
     */
    @Model
    private Map<Mote, List<Double>> getReliableMinPowerBuffers() {
        return this.reliableMinPowerBuffers;
    }

    /**
     * Puts an reliableMinPowerBuffer in the reliableMinPowerBuffers under mote.
     * @param mote The mote where to put the entry.
     * @param reliableMinPowerBuffer The buffer to put in the buffers.
     */
    @Model
    private void putReliableMinPowerBuffer(Mote mote, List<Double> reliableMinPowerBuffer) {
        this.reliableMinPowerBuffers.put(mote, reliableMinPowerBuffer);
    }

    /**
     * returns a map with gateway buffers.
     * @return A map with gateway buffers.
     */
    private FeedbackLoopGatewayBuffer getGatewayBuffer() {
        return gatewayBuffer;
    }

    /**
     * Returns the lower bound of the approach.
     * @return The lower bound of the approach.
     */
    @Basic
    public double getLowerBound() {
        return ((IntervalAdaptationGoal) qualityOfService.getAdaptationGoal("reliableCommunication")).getLowerBoundary();
    }

    /**
     * Returns the upper bound of the approach.
     * @return The upper bound of the approach.
     */
    @Basic
    public double getUpperBound() {
        return ((IntervalAdaptationGoal) qualityOfService.getAdaptationGoal("reliableCommunication")).getUpperBoundary();
    }


    @Override
    public void adapt(Mote mote, Gateway gateway) {
        /**
         First we check if we have received the message already from all gateways.
         */
        getGatewayBuffer().add(mote, gateway);
        if (getGatewayBuffer().hasReceivedAllSignals(mote)) {
            /**
             * check what is the highest received signal strength.
             */

            List<LoraTransmission> receivedSignals = getGatewayBuffer().getReceivedSignals(mote);

            double receivedPower = receivedSignals.get(0).getTransmissionPower();

            for (LoraTransmission transmission : receivedSignals) {
                if (receivedPower < transmission.getTransmissionPower()) {
                    receivedPower = transmission.getTransmissionPower();
                }
            }

            /**
             * If the buffer has an entry for the current mote, the new highest received signal strength is added to it,
             * else a new buffer is created and added to which we can add the signal strength.
             */
            List<Double> reliableMinPowerBuffer = new LinkedList<>();
            if (getReliableMinPowerBuffers().containsKey(mote)) {
                reliableMinPowerBuffer = getReliableMinPowerBuffers().get(mote);
            }
            reliableMinPowerBuffer.add(receivedPower);
            putReliableMinPowerBuffer(mote, reliableMinPowerBuffer);
            /**
             * If the buffer for the mote has 5 entries, the algorithm can start making adjustments.
             */
            if (getReliableMinPowerBuffers().get(mote).size() == 5) {
                /**
                 * The average is taken of the 5 entries.
                 */
                double average = getReliableMinPowerBuffers().get(mote).stream()
                    .mapToDouble(o -> o)
                    .average()
                    .orElse(0L);

                /**
                 * If the average of the signal strengths is higher than the upper bound, the transmitting power is decreased by 1;
                 */
                if (average > getUpperBound() && getMoteProbe().getPowerSetting(mote) > -3) {
                    getMoteEffector().setPower(mote, getMoteProbe().getPowerSetting(mote) - 1);
                }

                /**
                 * If the average of the signal strengths is lower than the lower bound, the transmitting power is increased by 1;
                 */
                if (average < getLowerBound() && getMoteProbe().getPowerSetting(mote) < 14) {
                    getMoteEffector().setPower(mote, getMoteProbe().getPowerSetting(mote) + 1);
                }

                putReliableMinPowerBuffer(mote, new LinkedList<>());
            }
        }
    }

}

