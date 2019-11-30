package datagenerator.iaqsensor;

import java.beans.ConstructorProperties;

public class Cell {

    private final int cellNumber;
    private final double fromTime;
    private final AirQualityLevel level;

    @ConstructorProperties({"cellNumber", "fromTime", "level"})
    public Cell(int cellNumber, double fromTime, String level) {
        this.cellNumber = cellNumber;
        this.fromTime = fromTime;
        this.level = AirQualityLevel.valueOf(level);
    }

    public int getCellNumber() {
        return cellNumber;
    }

    public double getFromTime() {
        return fromTime;
    }

    public AirQualityLevel getLevel() {
        return level;
    }


}
