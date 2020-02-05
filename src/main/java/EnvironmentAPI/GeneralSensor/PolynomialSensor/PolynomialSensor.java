package EnvironmentAPI.GeneralSensor.PolynomialSensor;

import EnvironmentAPI.GeneralSensor.Sensor;
import datagenerator.iaqsensor.TimeUnit;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.util.*;

public class PolynomialSensor extends Sensor {
    private List<List<Double>> newtonCoefficients  = new ArrayList<>();
    private List<Pair<Double,Double>> pointsKnown;
    private TimeUnit timeUnit;


    public PolynomialSensor(List<Pair<Double,Double>>points, GeoPosition position, double maxValue, TimeUnit unit) {
        super(position, maxValue);
        for(Pair<Double,Double> point:points){
            addPoint(point);
        }

        timeUnit = unit;

    }

    /**
     * Generate data given the polynomial of the sensor. This is determined using Newton
     * interpolation. This function returns a byte (value in range [0,255])
     * @param timeinNano: The time to evaluate the polynomial in.
     * @return a byte representing the amount of pollution in a range of [0,255].
     */
    @Override
    public double generateData(long timeinNano) {

        double dataAtTime =  evaluatePolynomial(timeinNano);
        return dataAtTime;
    }

    @Override
    public String getType() {
        return "PolynomialSensor";
    }

    @Override
    public TimeUnit getTimeUnit() {
        return timeUnit;
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
    private double evaluatePolynomial(long timeinNano) {
        double timeToEvaluate = timeUnit.convertFromNano(timeinNano);
        double totalValue = 0;
        for (int i = 0; i < newtonCoefficients.size(); i++)
        {
            totalValue += newtonCoefficients.get(i).get(0) * getPointsFromOrder(i, timeToEvaluate);
        }
        if (totalValue >= maxValue){
            totalValue = maxValue;
        }
        else if (totalValue <= 0){
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
