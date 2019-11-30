package selfadaptation.instrumentation;

import iot.SimulationRunner;
import iot.lora.LoraTransmission;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import util.ListHelper;
import util.Pair;
import util.Statistics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FeedbackLoopGatewayBuffer {
    private Map<Mote, List<List<Pair<Gateway, LoraTransmission>>>> gatewayBuffer;

    public FeedbackLoopGatewayBuffer() {
        gatewayBuffer = new HashMap<>();
    }

    public void add(Mote mote, Gateway gateway) {
        // FIXME this needs looking into, not sure how this is used in the actual simulation
        var environment = SimulationRunner.getInstance().getEnvironment();
        var transmissions = Statistics.getInstance().getReceivedTransmissions(gateway.getEUI(), environment.getNumberOfRuns() - 1);

        if (gatewayBuffer.containsKey(mote)) {
            boolean contains = false;
            for (Pair<Gateway, LoraTransmission> pair : ListHelper.getLast(gatewayBuffer.get(mote))) {
                if (pair.getLeft() == gateway) {
                    contains = true;
                    break;
                }
            }
            if (contains) {
                gatewayBuffer.get(mote).add(new LinkedList<>());
            }

            ListHelper.getLast(gatewayBuffer.get(mote)).add(new Pair<>(gateway, ListHelper.getLast(transmissions)));
        } else {
            List<Pair<Gateway, LoraTransmission>> buffer = new LinkedList<>();

            buffer.add(new Pair<>(gateway, ListHelper.getLast(transmissions)));

            List<List<Pair<Gateway, LoraTransmission>>> buffers = new LinkedList<>();
            buffers.add(buffer);
            gatewayBuffer.put(mote,buffers);
        }
    }

    public boolean hasReceivedAllSignals(Mote mote) {
        return gatewayBuffer.get(mote).size() > 1;
    }

    public List<LoraTransmission> getReceivedSignals(Mote mote) {
        List<LoraTransmission> result = new LinkedList<>();

        if (hasReceivedAllSignals(mote)) {
            for (Pair<Gateway, LoraTransmission> pair : gatewayBuffer.get(mote).get(0)) {
                result.add(pair.getRight());
            }
            gatewayBuffer.get(mote).remove(0);
        }

        return result;
    }
}
