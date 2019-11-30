package selfadaptation.adaptationgoals;

import be.kuleuven.cs.som.annotate.Basic;
import be.kuleuven.cs.som.annotate.Model;

/**
 * An adaptation goal with a lower and upper value.
 */
public class IntervalAdaptationGoal extends AdaptationGoal {

    /**
     * A double representing the lower value of the goal
     */
    @Model
    private final double lowerBoundary;

    /**
     * A double representing the upper value of the goal
     */
    @Model
    private final double upperBoundary;

    /**
     * Constructs a IntervalAdaptationGoal with a given lower and upper value.
     * @param lowerBoundary The lower boundary of the goal.
     * @param upperBoundary The upper boundary of the goal.
     */
    public IntervalAdaptationGoal(double lowerBoundary, double upperBoundary) {
        this.lowerBoundary = lowerBoundary;
        this.upperBoundary = upperBoundary;
    }

    /**
     * Returns the lower value of the goal.
     * @return The lower value of the goal.
     */
    @Basic
    public double getLowerBoundary() {
        return lowerBoundary;
    }

    /**
     * Returns the upper value of the goal.
     * @return The upper value of the goal.
     */
    @Basic
    public double getUpperBoundary() {
        return upperBoundary;
    }


    public String toString() {
        return String.format("[%.2f,%.2f]", this.lowerBoundary, this.upperBoundary);
    }
}
