package EnvironmentAPI.util;

import EnvironmentAPI.GeneralSensor.FunctionSensor.FunctionSensor;
import EnvironmentAPI.GeneralSensor.PolynomialSensor.PolynomialSensor;
import datagenerator.iaqsensor.TimeUnit;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.util.List;

public class SensorFactory {
    public static PolynomialSensor createPolynomialSensor(List<Pair<Double,Double>> Points,
                                                          Double maxValue,
                                                          GeoPosition position, TimeUnit unit){
        return new PolynomialSensor(Points, position, maxValue, unit);
    }

    public static FunctionSensor createFunctionSensor(String function,
                                                      Double maxValue,
                                                      GeoPosition position, TimeUnit unit){
        return new FunctionSensor(position, function, maxValue, unit);
    }
}
