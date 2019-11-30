package iot;

import be.kuleuven.cs.som.annotate.Basic;

import java.awt.*;


/**
 * A class representing the characteristics of a certain location.
 */
public enum Characteristic {

    Forest(3, 1000, 1.5, new Color(0, 102, 0)),
    City(1, 1000, 2, new Color(51, 153, 255)),
    Plain(2, 1000, 1.5, new Color(255, 153, 0));
    /**
     * An integer representing the path loss exponent in a certain position.
     */
    private final double pathLossExponent;
    /**
     * An integer representing the reference distance for the mean path loss in a certain position.
     */
    private final double referenceDistance;
    /**
     * An integer representing the shadow fading in a certain position.
     */
    private final double shadowFading;
    /**
     * The color of the characteristic.
     */
    private final Color color;

    /**
     * A constructor generating a characteristic with a given mean path loss, path loss exponent, reference distance
     * and shadow fading.
     * @param pathLossExponent  The path loss exponent to set.
     * @param referenceDistance The reference distance to set.
     * @param shadowFading The shadow fading to set.
     * @param color The color of the characteristic.
     */
     Characteristic(double pathLossExponent, double referenceDistance, double shadowFading, Color color) {
         this.color = color;
        this.pathLossExponent = pathLossExponent;
        this.referenceDistance = referenceDistance;
        this.shadowFading = shadowFading;
    }


    /**
     *  Returns the path loss exponent of this position.
     * @return The path loss exponent of this position.
     */
    @Basic
    public double getPathLossExponent() {
        return pathLossExponent;
    }

    /**
     *  Returns the reference distance of this position.
     * @return The reference distance of this position.
     */
    @Basic
    public double getReferenceDistance() {
        return referenceDistance;
    }

    /**
     *  Returns the shadow fading of this position.
     * @return The shadow fading of this position.
     */
    @Basic
    public double getShadowFading() {
        return shadowFading;
    }

    /**
     * Returns the color.
     * @return the color.
     */
    @Basic
    public Color getColor() {
        return color;
    }
}
