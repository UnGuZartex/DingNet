package datagenerator.GeneralSensor.PolynomialSensor;

import datagenerator.GeneralSensor.Sensor;
import datagenerator.iaqsensor.TimeUnit;
import util.Pair;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PolynomialSensor extends Sensor {
    private List<Double> polynomialCoefficients  = new ArrayList<Double>();
    private Double NoiseFactor = 0.0;
    private TimeUnit timeUnit;
    private Random random = new Random();


    public PolynomialSensor() {
        polynomialCoefficients.add(random.nextDouble());
        timeUnit = TimeUnit.SECONDS;
    }

    @Override
    public byte[] generateData(int x, int y, LocalTime time) {

        double dataAtTime = evaluatePolynomial(time);
        System.out.println("data: " + dataAtTime);
        return new byte[]{(byte)dataAtTime};
    }


    private double evaluatePolynomial(LocalTime time) {
        double timeToEvaluate = timeUnit.convertFromNano(time.toNanoOfDay());
        double totalValue = 0;
        for (int i = 0; i < polynomialCoefficients.size(); i++)
        {
            totalValue += polynomialCoefficients.get(i) * Math.pow(timeToEvaluate, i);
        }
        return totalValue;
    }
}
