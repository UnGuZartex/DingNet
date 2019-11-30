package unit;

import application.pollution.PollutionGrid;
import application.pollution.PollutionLevel;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;

import static org.junit.jupiter.api.Assertions.assertEquals;


class TestPollutionGrid {
    @Test
    void addMeasurements() {
        PollutionGrid grid = new PollutionGrid();

        grid.addMeasurement(1L, new GeoPosition(5, 5), new PollutionLevel(0.5));
        assertEquals(grid.getPollutionLevel(new GeoPosition(5, 5)).getPollutionFactor(), 0.5);

        grid.addMeasurement(2L, new GeoPosition(10, 10), new PollutionLevel(0.8));
        grid.addMeasurement(2L, new GeoPosition(10, 10), new PollutionLevel(0.1));
        assertEquals(grid.getPollutionLevel(new GeoPosition(10, 10)).getPollutionFactor(), 0.1);
    }

    @Test
    void noMeasurements() {
        PollutionGrid grid = new PollutionGrid();

        assertEquals(grid.getPollutionLevel(new GeoPosition(5,5)).getPollutionFactor(), 0.0);
        assertEquals(grid.getPollutionLevel(new GeoPosition(50,50)).getPollutionFactor(), 0.0);
    }
}
