package application.Environment.GeneralSensor.PolynomialSensor;

import application.Environment.GeneralSensor.Sensor;
import application.Environment.PollutionEnvironment;
import datagenerator.iaqsensor.TimeUnit;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.time.LocalTime;
import java.util.*;

public class PolynomialSensor extends Sensor {
    private List<Double> newtonCoefficients  = new ArrayList<>();
    private List<Pair<Double,Double>> pointsKnown  = new ArrayList<>();
    private Double NoiseFactor = 0.0;
    private TimeUnit timeUnit;
    private Random random = new Random();


    public PolynomialSensor(int radius, List<Pair>points, PollutionEnvironment environment, GeoPosition position) {
        super(radius, environment, position);
        pointsKnown.add(new Pair<Double,Double>(1.0, random.nextDouble()*150));
        pointsKnown.add(new Pair<Double,Double>(2.0,random.nextDouble()*150));
        pointsKnown.add(new Pair<Double,Double>(3.0,random.nextDouble()*150));
        pointsKnown.add(new Pair<Double,Double>(4.0,random.nextDouble()*150));
        pointsKnown.add(new Pair<Double,Double>(5.0,random.nextDouble()*150));
        pointsKnown.add(new Pair<Double,Double>(6.0,random.nextDouble()*150));
        pointsKnown.add(new Pair<Double,Double>(7.0,random.nextDouble()*150));
        pointsKnown.add(new Pair<Double,Double>(8.0,random.nextDouble()*150));
        pointsKnown.add(new Pair<Double,Double>(9.0,random.nextDouble()*150));
        pointsKnown.add(new Pair<Double,Double>(10.0,random.nextDouble()*150));
        pointsKnown.add(new Pair<Double,Double>(11.0,random.nextDouble()*150));
        pointsKnown.add(new Pair<Double,Double>(12.0,random.nextDouble()*150));


        calculateNewtonCoefficients();
        System.out.println(newtonCoefficients);

        timeUnit = TimeUnit.HOURS;
        System.out.println(generateData(LocalTime.of(0, 0, 4)));

    }

    /**
     * Generate data given the polynomial of the sensor. This is determined using Newton
     * interpolation. This function returns a byte (value in range [0,255])
     * @param time: The time to evaluate the polynomial in.
     * @return a byte representing the amount of pollution in a range of [0,255].
     */
    public double generateData(LocalTime time) {

        double dataAtTime =  evaluatePolynomial(time);
        System.out.println("data: " + dataAtTime);
        return dataAtTime;
    }

    /**
     * Calculate the Coefficients needed to determine the Newton polynomial
     * This is done using divided differences. The table with these values is generated
     * in the Coefficients map.
     */
    private void calculateNewtonCoefficients() {
        Map<Integer,Map<Integer,Double>> Coefficients = new HashMap<>();
        int order = pointsKnown.size() - 1;
        Map<Integer,Double> orderZero = new HashMap<>();
        int calculatingOrder = 0;
        List<Double> Xlist = new ArrayList<>();

        for (int i = 0; i < pointsKnown.size(); i++){
            orderZero.put(i, pointsKnown.get(i).getRight());
            Xlist.add(pointsKnown.get(i).getLeft());
        }
        Coefficients.put(calculatingOrder, orderZero);
        while(calculatingOrder < order){
            calculatingOrder++;
            Coefficients.put(calculatingOrder, getCoeffWithOrder(Coefficients,Xlist, calculatingOrder));
        }
        System.out.println(Coefficients);
        for(int i = 0; i < Coefficients.size(); i++){
            newtonCoefficients.add(Coefficients.get(i).get(0));
        }
    }

    /**
     * Calculate the coefficients of an order given the previous order coefficients.
     * @param alreadyKnown: Map consisting of already known coefficients
     * @param X: List containing all x-values
     * @param order: The current order to calculate
     * @return a map containing all coefficients for the order.
     */
    private Map<Integer, Double> getCoeffWithOrder(Map<Integer, Map<Integer,Double>> alreadyKnown,
                                                  List<Double> X, int order){
        Map<Integer, Double> previousOrder = alreadyKnown.get(alreadyKnown.size()-1);
        Map<Integer, Double> results = new HashMap<>();

        for(int i = 0; i < previousOrder.size()-1; i++){
            Double currentValue = previousOrder.get(i);
            Double nextValue = previousOrder.get(i+1);
            results.put(i,((nextValue-currentValue)/(X.get(i+order) - X.get(i))));
        }

        return results;
    }

    /**
     * Evaluate the polynomial constructed with Newton interpolation.
     * @param time: the time to evaluate in.
     * @return a value
     */
    private double evaluatePolynomial(LocalTime time) {
        double timeToEvaluate = timeUnit.convertFromNano(time.toNanoOfDay());
        double totalValue = 0;
        for (int i = 0; i < newtonCoefficients.size(); i++)
        {
            totalValue += newtonCoefficients.get(i) * getPointsFromOrder(i, timeToEvaluate);
            System.out.println(i + ": " + totalValue);
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
