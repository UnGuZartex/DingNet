package util;

import iot.lora.LoraTransmission;
import iot.networkentity.NetworkEntity;

import java.util.*;
import java.util.stream.Collectors;

public class Statistics {

    private static Statistics instance = new Statistics();

    private int runNumber = 0;

    // A list representing the power setting of every transmission.
    private final Map<Long, List<PowerSettingDataPoint>> powerSettingHistory;

    // A list representing the spreading factor of every transmission.
    private final Map<Long, List<SpreadingFactorDataPoint>> spreadingFactorHistory;

    // A map with the transmissions received by the entity and if they collided with an other packet.
    // TODO make sure the order is also preserved when streaming the results and filtering on runNumbers
    private final Map<Long, LinkedHashSet<LoraTransmissionDataPoint>> receivedTransmissions;

    // A list with the transmissions transmitted by the entity
    private final Map<Long, List<LoraTransmissionDataPoint>> sentTransmissions;

    private Statistics() {
        powerSettingHistory = new HashMap<>();
        spreadingFactorHistory = new HashMap<>();
        receivedTransmissions = new HashMap<>();
        sentTransmissions = new HashMap<>();
    }

    public static Statistics getInstance() {
        return instance;
    }

    public void addPowerSettingEntry(long networkEntity, int timeInSeconds, int powerSetting) {
        initIfAbsent(powerSettingHistory, networkEntity);
        var lists = powerSettingHistory.get(networkEntity);
        lists.add(new PowerSettingDataPoint(this.runNumber, timeInSeconds, powerSetting));
    }

    public void addSpreadingFactorEntry(NetworkEntity networkEntity, int entry) {
        addSpreadingFactorEntry(networkEntity.getEUI(), entry);
    }

    public void addSpreadingFactorEntry(long networkEntity, int entry) {
        initIfAbsent(spreadingFactorHistory, networkEntity);
        var lists = spreadingFactorHistory.get(networkEntity);
        lists.add(new SpreadingFactorDataPoint(this.runNumber, entry));
    }

    public void addReceivedTransmissionsEntry(NetworkEntity networkEntity, LoraTransmission entry) {
        addReceivedTransmissionsEntry(networkEntity.getEUI(), entry);
    }

    public void addReceivedTransmissionsEntry(long networkEntity, LoraTransmission entry) {
        if (!receivedTransmissions.containsKey(networkEntity)) {
            receivedTransmissions.put(networkEntity, new LinkedHashSet<>());
        }
        var lists = receivedTransmissions.get(networkEntity);
        lists.add(new LoraTransmissionDataPoint(runNumber, entry));
    }

    public void addSentTransmissionsEntry(NetworkEntity networkEntity, LoraTransmission entry) {
        addSentTransmissionsEntry(networkEntity.getEUI(), entry);
    }

    public void addSentTransmissionsEntry(long networkEntity, LoraTransmission entry) {
        initIfAbsent(sentTransmissions, networkEntity);
        var lists = sentTransmissions.get(networkEntity);
        lists.add(new LoraTransmissionDataPoint(runNumber, entry));
    }

    private <E> void initIfAbsent(Map<Long, List<E>> map, long id) {
        if (!map.containsKey(id)) {
            map.put(id, new LinkedList<E>());
        }
    }

    public void reset() {
        powerSettingHistory.clear();
        spreadingFactorHistory.clear();
        receivedTransmissions.clear();
        sentTransmissions.clear();

        runNumber = 0;
    }

    public void addRun() {
        runNumber++;
    }

    public List<PowerSettingDataPoint> getPowerSettingHistory(long networkEntity) {
        return powerSettingHistory.get(networkEntity);
    }

    public List<Pair<Integer,Integer>> getPowerSettingHistory(long networkEntity, int run) {
        return getPowerSettingHistory(networkEntity).stream()
            .filter(o -> o.runNumber == run)
            .map(o -> new Pair<>(o.timeInSeconds, o.powerSetting))
            .collect(Collectors.toList());
    }

    public List<SpreadingFactorDataPoint> getSpreadingFactorHistory(long networkEntity) {
        return spreadingFactorHistory.get(networkEntity);
    }

    public List<Integer> getSpreadingFactorHistory(long networkEntity, int run) {
        return getSpreadingFactorHistory(networkEntity).stream()
            .filter(o -> o.runNumber == run)
            .map(o -> o.spreadingFactor)
            .collect(Collectors.toList());
    }

    public LinkedHashSet<LoraTransmissionDataPoint> getReceivedTransmissions(long networkEntity) {
        return receivedTransmissions.get(networkEntity);
    }

    public List<LoraTransmission> getReceivedTransmissions(long eui, int run) {
        return this.getAllReceivedTransmissions(eui, run).stream()
            .filter(t -> !t.isCollided())
            .collect(Collectors.toList());
    }

    public LinkedHashSet<LoraTransmission> getAllReceivedTransmissions(long eui, int run) {
        return getReceivedTransmissions(eui).stream()
            .filter(o -> o.runNumber == run)
            .map(o -> o.transmission)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }


    public List<LoraTransmissionDataPoint> getSentTransmissions(long networkEntity) {
        return sentTransmissions.get(networkEntity);
    }

    public List<LoraTransmission> getSentTransmissions(long networkEntity, int run) {
        return getSentTransmissions(networkEntity).stream()
            .filter(o -> o.runNumber == run)
            .map(o -> o.transmission)
            .collect(Collectors.toList());
    }

    public List<Double> getUsedEnergy(long networkEntity, int run) {
        List<Double> usedEnergy = new LinkedList<>();
        int i= 0;
        for (LoraTransmission transmission: getSentTransmissions(networkEntity, run)) {
            usedEnergy.add(Math.pow(10,((double)getPowerSettingHistory(networkEntity, run).get(i).getRight())/10)*transmission.getTimeOnAir()/1000);
            i++;
        }
        return usedEnergy;
    }


    public static class PowerSettingDataPoint {
        public int runNumber;
        public int timeInSeconds;
        public int powerSetting;

        PowerSettingDataPoint(int runNumber, int timeInSeconds, int powerSetting) {
            this.runNumber = runNumber;
            this.timeInSeconds = timeInSeconds;
            this.powerSetting = powerSetting;
        }
    }

    public static class SpreadingFactorDataPoint {
        public int runNumber;
        public int spreadingFactor;

        public SpreadingFactorDataPoint(int runNumber, int spreadingFactor) {
            this.runNumber = runNumber;
            this.spreadingFactor = spreadingFactor;
        }
    }


    public static class LoraTransmissionDataPoint {
        public int runNumber;
        public LoraTransmission transmission;

        LoraTransmissionDataPoint(int runNumber, LoraTransmission transmission) {
            this.runNumber = runNumber;
            this.transmission = transmission;
        }
    }
}


