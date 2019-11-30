package datagenerator.PolynomialSensor;

import datagenerator.SensorDataGenerator;
import datagenerator.iaqsensor.TimeUnit;
import util.Pair;

import java.time.LocalTime;
import java.util.List;

public class PolynomialSensor implements SensorDataGenerator {
    private List<Double> polynomialCoefficients;
    private Double NoiseFactor;
    private TimeUnit timeUnit;


    public PolynomialSensor() {
        polynomialCoefficients.add(0.0);
    }

    @Override
    public byte[] generateData(int x, int y, LocalTime time) {

        double dataAtTime = evaluatePolynomial(time);
        return new byte[]{(byte) dataAtTime};
    }

    @Override
    public byte[] generateData(Pair<Integer, Integer> pos, LocalTime time) {
        return generateData(pos.getLeft(), pos.getRight(), time);
    }

    @Override
    public double nonStaticDataGeneration(double x, double y) {
        return 0;
    }

    @Override
    public void reset() {

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
