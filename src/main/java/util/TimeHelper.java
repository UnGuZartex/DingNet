package util;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class TimeHelper {
    public static double nanoToMili(double nanoTime) {
        return  nanoTime / 1e6;
    }

    public static double miliToNano(double miliTime) {
        return miliTime * 1e6;
    }

    public static long miliToNano(long miliTime) {
        return miliTime * (long) 1e6;
    }

    public static double secToMili(double secTime) {
        return  secTime * 1e3;
    }

    public static LocalTime roundToMilli(LocalTime time) {
        var lowerBound = time.truncatedTo(ChronoUnit.MILLIS);
        var threshold = lowerBound.plus(500, ChronoUnit.MICROS);
        return time.isBefore(threshold) ? lowerBound : lowerBound.plus(1, ChronoUnit.MILLIS);
    }
}
