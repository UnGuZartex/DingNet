package EnvironmentAPI.GeneralSources.PolynomialSources;

import EnvironmentAPI.GeneralSources.Source;
import EnvironmentAPI.util.EnvSettings;
import EnvironmentAPI.util.PairComparator;
import datagenerator.iaqsensor.TimeUnit;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.util.*;

public class PolynomialSource extends Source {
    private List<List<Double>> newtonCoefficients  = new ArrayList<>();
    private List<Pair<Double,Double>> pointsKnown;


    public PolynomialSource(List<Pair<Double,Double>>points, GeoPosition position, TimeUnit unit) {
        super(position, unit);
        for(Pair<Double,Double> point:points){
            addPoint(point);
        }
    }

    /**
     * Generate data given the polynomial of the sensor. This is determined using Newton
     * interpolation. This function returns a byte (value in range [0,255])
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

    @Override
    public String getType() {
        return "PolynomialSource";
    }


    @Override
    public Object getDefiningFeatures() {
        return this.pointsKnown;
    }

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

    private Double getPointsFromOrder(int order, double timeToEvaluate) {
        Double totalValue = pointsKnown.get(0).getLeft();
        for(int i = 0; i < order; i++){
            totalValue *= timeToEvaluate-pointsKnown.get(i).getLeft();
        }
        return totalValue;
    }
}
