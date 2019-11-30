package selfadaptation.feedbackloop;


import be.kuleuven.cs.som.annotate.Model;
import iot.SimulationRunner;
import iot.lora.LoraTransmission;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import selfadaptation.instrumentation.FeedbackLoopGatewayBuffer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A class representing the distance based adaptation approach.
 */
public class ReliableEfficientDistanceGateway extends GenericFeedbackLoop {
    /**
     * A HashMap representing the buffers for the approach.
     */
    @Model
    private Map<Mote, List<Double>> reliableDistanceGatewayBuffers;


    /**
     * A map to keep track of which gateway has already sent the packet.
     */
    @Model
    private FeedbackLoopGatewayBuffer gatewayBuffer;



    /**
     * Returns the algorithm buffers.
     * @return The algorithm buffers.
     */
    @Model
    private Map<Mote, List<Double>> getReliableDistanceGatewayBuffers() {
        return this.reliableDistanceGatewayBuffers;
    }

    /**
     * Puts an reliableMinPowerBuffer in the reliableMinPowerBuffers under mote.
     * @param mote The mote where to put the entry.
     * @param reliableDistanceGatewayBuffer The buffer to put in the buffers.
     */
    @Model
    private void putReliableDistanceGatewayBuffers(Mote mote, List<Double> reliableDistanceGatewayBuffer) {
        this.reliableDistanceGatewayBuffers.put(mote,reliableDistanceGatewayBuffer);
    }

    /**
     * Constructs a distance based approach.
     */
    public ReliableEfficientDistanceGateway() {
        super("Distance-based");
        gatewayBuffer = new FeedbackLoopGatewayBuffer();
        reliableDistanceGatewayBuffers = new HashMap<>();
    }


    /**
     * returns a map with gateway buffers.
     * @return A map with gateway buffers.
     */
    private FeedbackLoopGatewayBuffer getGatewayBuffer() {
        return gatewayBuffer;
    }

    public void adapt(Mote mote, Gateway dataGateway) {
        /**
         First we check if we have received the message already from all gateways.
         */
        getGatewayBuffer().add(mote,dataGateway);
        if (getGatewayBuffer().hasReceivedAllSignals(mote)) {
            /**
             * Check for the signal which has travelled the shortest distance.
             */
            List<LoraTransmission> receivedSignals = getGatewayBuffer().getReceivedSignals(mote);
            var env = SimulationRunner.getInstance().getEnvironment();
            double shortestDistance = Math.sqrt(Math.pow(env.getNetworkEntityById(receivedSignals.get(0).getReceiver()).getYPosInt()-receivedSignals.get(0).getYPos(),2)+
                    Math.pow(env.getNetworkEntityById(receivedSignals.get(0).getReceiver()).getXPosInt()-receivedSignals.get(0).getXPos(),2));

            for (LoraTransmission transmission: receivedSignals) {
                if (shortestDistance>Math.sqrt(Math.pow(env.getNetworkEntityById(transmission.getReceiver()).getYPosInt()-transmission.getYPos(),2)+
                        Math.pow(env.getNetworkEntityById(transmission.getReceiver()).getXPosInt()-transmission.getXPos(),2))) {
                    shortestDistance = Math.sqrt(Math.pow(env.getNetworkEntityById(transmission.getReceiver()).getYPosInt()-transmission.getYPos(),2)+
                            Math.pow(env.getNetworkEntityById(transmission.getReceiver()).getXPosInt()-transmission.getXPos(),2));
                }
            }



            /**
             * If the buffer has an entry for the current mote, the new the distance to the nearest gateway is added to it,
             * else a new buffer is created and added to which we can add the the distance to the nearest gateway.
             */
            List<Double> reliableDistanceGatewayBuffer;
            if (!getReliableDistanceGatewayBuffers().containsKey(mote)) {
                putReliableDistanceGatewayBuffers(mote, new LinkedList<>());
            }
            reliableDistanceGatewayBuffer = getReliableDistanceGatewayBuffers().get(mote);
            reliableDistanceGatewayBuffer.add(shortestDistance);
            putReliableDistanceGatewayBuffers(mote,reliableDistanceGatewayBuffer);
            /**
             * If the buffer for the mote has 4 entries, the algorithm can start making adjustments.
             */
            if (getReliableDistanceGatewayBuffers().get(mote).size() == 4) {
                /**
                 * The average is taken of the 4 entries.
                 */
                double average = 0;
                for (double distance : getReliableDistanceGatewayBuffers().get(mote)) {
                    average += distance;
                }
                average = average / 4;

                /**
                 * Depending on which interval the average is in, the power setting is adjusted.
                 */
                if (average < 20)
                    getMoteEffector().setPower(mote, 2);
                else if (average < 45)
                    getMoteEffector().setPower(mote, 3);
                else if (average < 70)
                    getMoteEffector().setPower(mote, 4);
                else if (average < 95)
                    getMoteEffector().setPower(mote, 5);
                else if (average < 120)
                    getMoteEffector().setPower(mote, 6);
                else if (average < 145)
                    getMoteEffector().setPower(mote, 7);
                else if (average < 170)
                    getMoteEffector().setPower(mote, 8);
                else if (average < 195)
                    getMoteEffector().setPower(mote, 9);
                else if (average < 220)
                    getMoteEffector().setPower(mote, 10);
                else if (average < 245)
                    getMoteEffector().setPower(mote, 11);
                else if (average < 270)
                    getMoteEffector().setPower(mote, 12);
                else if (average < 295)
                    getMoteEffector().setPower(mote, 13);
                else
                    getMoteEffector().setPower(mote, 14);
                putReliableDistanceGatewayBuffers(mote, new LinkedList<>());
            }
        }
    }

}
