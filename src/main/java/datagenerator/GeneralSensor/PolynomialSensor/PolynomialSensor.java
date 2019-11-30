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
        pointsKnown.add(new Pair<Double,Double>(0.0,1.0));
        pointsKnown.add(new Pair<Double,Double>(1.0,0.0));
        calculateNewtonCoefficients();
        System.out.println(newtonCoefficients);
        timeUnit = TimeUnit.SECONDS;
    }

    @Override
    public byte[] generateData(int x, int y, LocalTime time) {

        double dataAtTime = evaluatePolynomial(time);
        System.out.println("data: " + dataAtTime);
        return new byte[]{(byte)dataAtTime};
    }


    private void calculateNewtonCoefficients() {
        Map<String,Map<String,Double>> Coefficients = new HashMap<>();
        int order = pointsKnown.size() - 1;
        Map<String,Double> orderZero = new HashMap<>();
        int calculatingOrder = 0;
        List<Double> Xlist = new ArrayList<>();

        for (int i = 0; i < pointsKnown.size(); i++){
            orderZero.put(String.valueOf(i), pointsKnown.get(i).getRight());
            Xlist.add(pointsKnown.get(i).getLeft());
        }
        Coefficients.put(String.valueOf(calculatingOrder), orderZero);
        while(calculatingOrder < order){
            calculatingOrder++;
            Coefficients.put(String.valueOf(calculatingOrder), getCoeffWithOrder(Coefficients,Xlist));
        }
        System.out.println(Coefficients);
    }

    private Map<String, Double> getCoeffWithOrder(Map<String, Map<String,Double>> alreadyKnown,
                                                  List<Double> X){
        Map<String, Double> previousOrder = alreadyKnown.get(String.valueOf(alreadyKnown.size()-1));
        Map<String, Double> results = new HashMap<>();
        for(int i = 0; i < previousOrder.size()-1; i++){
            Double currentValue = previousOrder.get(String.valueOf(i));
            Double nextValue = previousOrder.get(String.valueOf(i+1));
            results.put(String.valueOf(i),((nextValue-currentValue)/(X.get(i + 1) - X.get(i))));
        }

        return results;
    }
    private double evaluatePolynomial(LocalTime time) {
        double timeToEvaluate = timeUnit.convertFromNano(time.toNanoOfDay());
        double totalValue = 0;
        for (int i = 0; i < newtonCoefficients.size(); i++)
        {
            totalValue += newtonCoefficients.get(i) * Math.pow(timeToEvaluate, i);
        }
        return totalValue;
    }
}
