package EnvironmentAPI.util;

import util.Pair;

import java.util.Comparator;

/**
 * Class to compare a pair of points
 *
 * @author Yentl.kinoo@student.kuleuven.be
 */
public class PairComparator implements Comparator<Pair<Double, Double>> {

    @Override
    public int compare(Pair<Double, Double> doubleDoublePair, Pair<Double, Double> t1) {
        if (doubleDoublePair.getLeft() < t1.getLeft()) {
            return -1;
        } else if (doubleDoublePair.getLeft() > t1.getLeft()) {
            return 1;
        }
        else{
            return 0;
        }
    }
}
