package EnvironmentAPI.GeneralSensor.PolynomialSensor;

import EnvironmentAPI.GeneralSensor.Sensor;
import EnvironmentAPI.PollutionEnvironment;
import datagenerator.iaqsensor.TimeUnit;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.util.*;

public class PolynomialSensor extends Sensor {
    private List<List<Double>> newtonCoefficients  = new ArrayList<>();
    private List<Pair<Double,Double>> pointsKnown  = new ArrayList<>();
    private Double NoiseFactor = 0.0;
    private TimeUnit timeUnit;
    private Random random = new Random();


    public PolynomialSensor(int radius, List<Pair>points, GeoPosition position) {
        super(radius, position);

        addPoint(new Pair<Double,Double>(1.0,random.nextDouble()*155));
        addPoint(new Pair<Double,Double>(2.0,random.nextDouble()*155));
        addPoint(new Pair<Double,Double>(3.0,random.nextDouble()*155));
        addPoint(new Pair<Double,Double>(4.0,random.nextDouble()*155));
        addPoint(new Pair<Double,Double>(5.0,random.nextDouble()*155));
        addPoint(new Pair<Double,Double>(15.0,random.nextDouble()*155));



        timeUnit = TimeUnit.SECONDS;

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

    public void addPoint(Pair<Double,Double> Point){
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
        if (totalValue >= 255){
            totalValue = 255;
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
