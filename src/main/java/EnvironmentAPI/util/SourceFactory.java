package EnvironmentAPI.util;

import EnvironmentAPI.GeneralSources.FunctionSources.FunctionSource;
import EnvironmentAPI.GeneralSources.PolynomialSources.PolynomialSource;
import datagenerator.iaqsensor.TimeUnit;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Class representing a factory for all the types of sources, sorts all points for polynomial sources and creates the correct source
 *
 * @author Yentl.kinoo@student.kuleuven.be
 */
public class SourceFactory {
    public static PolynomialSource createPolynomialSource(List<Pair<Double,Double>> Points,
                                                          GeoPosition position, TimeUnit unit){
        Points = sortPoints(Points);
        return new PolynomialSource(Points, position, unit);
    }

    public static FunctionSource createFunctionSource(String function,
                                                      GeoPosition position, TimeUnit unit){
        return new FunctionSource(position, function, unit);
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
