package datagenerator.GeneralSensor.PolynomialSensor;

import datagenerator.GeneralSensor.Sensor;
import datagenerator.iaqsensor.TimeUnit;
import util.Pair;

import java.time.LocalTime;
import java.util.*;

public class PolynomialSensor extends Sensor {
    private List<Double> newtonCoefficients  = new ArrayList<Double>();
    private List<Pair<Double,Double>> pointsKnown  = new ArrayList<Pair<Double,Double>>();
    private Double NoiseFactor = 0.0;
    private TimeUnit timeUnit;


    public PolynomialSensor(int radius, List<Pair>points) {
        super(radius);
        pointsKnown.add(new Pair<Double,Double>(1.0,6.0));
        pointsKnown.add(new Pair<Double,Double>(2.0,9.0));
        pointsKnown.add(new Pair<Double,Double>(3.0,2.0));
        pointsKnown.add(new Pair<Double,Double>(4.0,5.0));


        calculateNewtonCoefficients();
        System.out.println(newtonCoefficients);

        timeUnit = TimeUnit.SECONDS;
        System.out.println(evaluatePolynomial(LocalTime.of(0,0,4)));

    }

    @Override
    public byte[] generateData(int x, int y, LocalTime time) {

        double dataAtTime = evaluatePolynomial(time);
        System.out.println("data: " + dataAtTime);
        return new byte[]{(byte)dataAtTime};
    }


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
    private double evaluatePolynomial(LocalTime time) {
        double timeToEvaluate = timeUnit.convertFromNano(time.toNanoOfDay());
        double totalValue = 0;
        for (int i = 0; i < newtonCoefficients.size(); i++)
        {
            totalValue += newtonCoefficients.get(i) * getPointsFromOrder(i, timeToEvaluate);
            System.out.println(i + ": " + totalValue);
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
