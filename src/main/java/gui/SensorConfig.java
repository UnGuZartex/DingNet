package gui;
import iot.Environment;
import iot.SimulationRunner;


import javax.swing.*;

public class SensorConfig {
    private final MainGUI mainGUI;
    private final Environment environment;
    private final SimulationRunner simRunner;
    private final JFrame frame;
    private JPanel main;
    private JPanel panel1;

    public SensorConfig(MainGUI parent, JFrame frame, SimulationRunner simRunner) {
        this.mainGUI = parent;
        this.environment = mainGUI.getEnvironment();
        this.simRunner = simRunner;
        this.frame = frame;

    }

    public JPanel getMainPanel() {
        return main;
    }
}
