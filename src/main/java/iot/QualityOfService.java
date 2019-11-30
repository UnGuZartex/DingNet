package iot;

import selfadaptation.adaptationgoals.AdaptationGoal;

import java.util.Map;
import java.util.Set;

/**
 * A class representing a requested Quality Of Service.
 */
public class QualityOfService {

    private Map<String, AdaptationGoal> adaptationGoals;


    /**
     * Construct a Quality Of Service with given adaptationGoals
     * @param adaptationGoals the adaptation goals of the Quality Of Service.
     */
    public QualityOfService(Map<String, AdaptationGoal> adaptationGoals) {
        this.adaptationGoals = adaptationGoals;
    }


    /**
     * Returns the AdaptationGoal with the given name.
     * @param name The name of the AdaptationGoal.
     * @return The AdaptationGoal with the given name.
     */
    public AdaptationGoal getAdaptationGoal(String name) {
        return adaptationGoals.get(name);
    }

    public Set<String> getNames() {
        return adaptationGoals.keySet();
    }

    /**
     * Puts the AdaptationGoal with the given name in the HashMap.
     * @param name The name of the AdaptationGoal.
     * @param adaptationGoal The AdaptationGoal to put in the HashMap.
     */
    public void putAdaptationGoal(String name, AdaptationGoal adaptationGoal) {
        this.adaptationGoals.put(name,adaptationGoal);
    }


    public void updateAdaptationGoals(QualityOfService QoS) {
        QoS.getNames().forEach(n -> this.putAdaptationGoal(n, QoS.getAdaptationGoal(n)));
    }
}
