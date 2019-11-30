package scenario;

import iot.SimulationRunner;
import iot.SimulationUpdateListener;
import iot.networkentity.Mote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;
import util.MutableInteger;

import java.io.File;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCompleteSimulation {

    @BeforeEach
    private void init() throws NoSuchFieldException, IllegalAccessException {
        Field instance = SimulationRunner.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    void testCompleteSimulationWithoutApplications() {
        // Provide test configuration
        SimulationRunner runner = SimulationRunner.getInstance();
        runner.loadConfigurationFromFile(new File(TestCompleteSimulation.class.getResource("test_configuration.xml").getPath()));
        runner.updateInputProfilesFile(new File(TestCompleteSimulation.class.getResource("test_inputProfile.xml").getPath()));
        runner.getSimulation().setInputProfile(runner.getInputProfiles().get(0));

        // Do simulation
        runner.setupSingleRun();
        runner.simulate(new MutableInteger(Integer.MAX_VALUE), new SimulationUpdateListener() {
            @Override
            public void update() {}
            @Override
            public void onEnd() {
                // Check if mote is at destination
                Mote mote = runner.getEnvironment().getMotes().get(0);
                assertEquals(mote.getPosInt(), runner.getEnvironment().getMapHelper().toMapCoordinate(new GeoPosition(50.859172,4.688349)));
            }
        });

    }
}
