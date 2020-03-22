package EnvironmentAPI.GeneralSources.PolynomialSources;

import EnvironmentAPI.GeneralSources.Source;
import EnvironmentAPI.util.EnvSettings;
import EnvironmentAPI.util.PairComparator;
import datagenerator.iaqsensor.TimeUnit;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.util.*;

/**
 * This class describes a PolynomialSource, this is a source that follows a polynomial function
 * determined by given points, Newton-Interpolation is used.
 *
 * @author Yentl.kinoo@student.kuleuven.be
 */
public class PolynomialSource extends Source {
    private List<List<Double>> newtonCoefficients  = new ArrayList<>();
    private List<Pair<Double,Double>> pointsKnown;


    public PolynomialSource(List<Pair<Double,Double>>points, GeoPosition position, TimeUnit unit) {
        super(position, unit);
        for(Pair<Double,Double> point:points){
            addPoint(point);
        }
    }

    @Override
    public String getType() {
        return "PolynomialSource";
    }

    @Override
    public List<Pair<Double,Double>> getDefiningFeatures() {
        return this.pointsKnown;
    }

    /**
     * Resets the whole source given a set of points
     * @param points set of points to reinstantiate the source.
     */
    public void clear(Set<Pair<Double,Double>> points) {
        pointsKnown.clear();
        newtonCoefficients.clear();

        for(Pair<Double,Double> point:points){
            addPoint(point);
        }
    }

    /**
     * Generate data given the polynomial of the sensor. This is determined using Newton
     * interpolation. This function returns a byte (value in range [0,255])
     * If too many points are in this source, linear interpolation is used.
     * @param timeinNano: The time to evaluate the polynomial in.
     * @return a byte representing the amount of pollution in a range of [0,255].
     */
    @Override
    public double generateData(double timeinNano) {
        double dataAtTime;
        if (pointsKnown.size() <= EnvSettings.MAX_POINTS_NEWTON) {
             dataAtTime = evaluatePolynomial(timeinNano);
        }
        else {
            dataAtTime = evaluateLinearly(timeinNano);
        }
        return dataAtTime;
    }


    /**
     * Evaluate using linear interpolation, search the closest point to the left of this time,
     * and the closest point to the right of this point.
     * @param timeinNano time to evaluate its value from
     * @return  pollution at the given time
     */
    private double evaluateLinearly(double timeinNano) {
        double timeToEvaluate = timeUnit.convertFromNano(timeinNano);
        if (pointsKnown.stream().anyMatch(p -> p.getLeft() == timeToEvaluate)) {
            return pointsKnown.stream().filter(p -> p.getLeft() == timeToEvaluate).findFirst().get().getRight();
        }
        PairComparator pairComp = new PairComparator();
        Pair<Double,Double> leftPoint = pointsKnown.stream().filter(p -> p.getLeft() <= timeToEvaluate).max(pairComp).orElse(new Pair<Double,Double>(0.0,0.0));
        Pair<Double,Double> rightPoint = pointsKnown.stream().filter(p -> p.getLeft() >= timeToEvaluate).min(pairComp).orElse(pointsKnown.stream().max(pairComp).get());
        double rico = (rightPoint.getRight() - leftPoint.getRight()) / (rightPoint.getLeft() - leftPoint.getLeft());
        return rico*(timeToEvaluate-leftPoint.getLeft()) + leftPoint.getRight();

    }


    /**
     * Adds a point to this source and calculates the newton coefficients to use.
     * @param Point The point to add to this source
     */
    private void addPoint(Pair<Double, Double> Point){
        if(pointsKnown == null){
            pointsKnown = new ArrayList<>();
        }
        pointsKnown.add(Point);
        if(newtonCoefficients.isEmpty()){
            newtonCoefficients.add(new ArrayList<Double>(Collections.singleton(Point.getRight())));
        }
        else{
            newtonCoefficients.get(0).add(Point.getRight());
            newtonCoefficients.add(new ArrayList<>());
            for(int i = 1; i < newtonCoefficients.size(); i++){
                int listLengthPrevious = newtonCoefficients.get(i-1).size() - 1;
                double newValue = newtonCoefficients.get(i-1).get(listLengthPrevious) - newtonCoefficients.get(i-1).get(listLengthPrevious-1);
                newValue /= pointsKnown.get(listLengthPrevious-1+i).getLeft() - pointsKnown.get(listLengthPrevious-1).getLeft();
                newtonCoefficients.get(i).add(newValue);
            }
        }
    }

    /**
     * Evaluate the polynomial constructed with Newton interpolation.
     * @param timeinNano: the time to evaluate in.
     * @return a value
     */
    private double evaluatePolynomial(double timeinNano) {
        double timeToEvaluate = timeUnit.convertFromNano(timeinNano);
        double totalValue = 0;
        for (int i = 0; i < newtonCoefficients.size(); i++)
        {
            totalValue += newtonCoefficients.get(i).get(0) * getPointsFromOrder(i, timeToEvaluate);
        }
        if (totalValue <= 0){
            totalValue = 0;
        }
        return totalValue;
    }

    /**
     * Evaluate the polynomial with xth order of newton coefficient
     * @param order the order in the list
     * @param timeToEvaluate time to find the pollution from
     * @return the pollution at that time
     */
    private Double getPointsFromOrder(int order, double timeToEvaluate) {
        Double totalValue = pointsKnown.get(0).getLeft();
        for(int i = 0; i < order; i++){
            totalValue *= timeToEvaluate-pointsKnown.get(i).getLeft();
        }
        return totalValue;
    }
}
