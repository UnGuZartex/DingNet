package EnvironmentAPI.util;

import EnvironmentAPI.GeneralSensor.FunctionSensor.FunctionSensor;
import EnvironmentAPI.GeneralSensor.PolynomialSensor.PolynomialSensor;
import datagenerator.iaqsensor.TimeUnit;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SensorFactory {
    public static PolynomialSensor createPolynomialSensor(List<Pair<Double,Double>> Points,
                                                          Double maxValue,
                                                          GeoPosition position, TimeUnit unit){
        Points = sortPoints(Points);
        return new PolynomialSensor(Points, position, maxValue, unit);
    }

    public static FunctionSensor createFunctionSensor(String function,
                                                      Double maxValue,
                                                      GeoPosition position, TimeUnit unit){
        return new FunctionSensor(position, function, maxValue, unit);
    }

    private static List<Pair<Double,Double>> sortPoints(List<Pair<Double,Double>> points){
        List<Pair<Double,Double>> result = new ArrayList<>();
        HashMap<Double,Double> map = new HashMap<>();
        for(Pair<Double,Double> point:points){
            map.put(point.getLeft(),point.getRight());
        }
        List<Double> toSort = new ArrayList<>();
        for(Double key:map.keySet()){
            toSort.add(key);
        }
        Collections.sort(toSort);
        for(Double sorted:toSort){
            result.add(new Pair<Double,Double>(sorted, map.get(sorted)));
        }
        return result;
    }
}
