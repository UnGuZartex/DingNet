package unit;

import application.pollution.PollutionLevel;
import org.junit.jupiter.api.Test;
import util.Pair;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestPollutionLevel {
    @Test
    void happyDay() {
        PollutionLevel level = new PollutionLevel(0.5);
        assertEquals(level.getPollutionFactor(), 0.5);
    }

    @Test
    void rainyDay() {
        assertThrows(IllegalArgumentException.class, () -> new PollutionLevel(4.5));

        assertThrows(IllegalArgumentException.class, () -> new PollutionLevel(-1.0));
    }

    @Test
    void meanPollutionLevels() {
        List<Pair<Double, PollutionLevel>> input = new ArrayList<>();
        input.add(new Pair<>(30.0, new PollutionLevel(0.9)));
        input.add(new Pair<>(10.0, new PollutionLevel(0.5)));
        input.add(new Pair<>(20.0, new PollutionLevel(0.15)));

        assertEquals(PollutionLevel.getMediumPollution(input).getPollutionFactor(), 0.5);
    }

}
