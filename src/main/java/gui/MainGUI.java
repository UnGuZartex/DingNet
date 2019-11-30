package gui;


import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import gui.util.*;
import iot.*;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import org.jfree.chart.ChartPanel;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;
import selfadaptation.adaptationgoals.IntervalAdaptationGoal;
import selfadaptation.adaptationgoals.ThresholdAdaptationGoal;
import selfadaptation.feedbackloop.GenericFeedbackLoop;
import util.MapHelper;
import util.MutableInteger;
import util.Pair;
import util.Statistics;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class MainGUI extends JFrame implements SimulationUpdateListener, Refreshable {
    private JPanel map;
    private JPanel console;
    private JPanel mainPanel;
    private JToolBar configurationToolBar;
    private JToolBar toolBarAdaptation;
    private JTabbedPane tabbedPaneGraphs;
    private JButton openConfigurationButton;
    private JButton configureButton;
    private JButton helpButton;
    private JButton aboutButton;
    private JButton simulationSaveButton;
    private JButton totalRunButton;
    private JLabel centerLabel;
    private JPanel entitesPanel;
    private JScrollPane entitiesPane;
    private JPanel receivedPowerGraph;
    private JPanel powerSettingGraph;
    private JPanel spreadingFactorGraph;
    private JButton saveConfigurationButton;
    private JPanel usedEnergyGraph;
    private JButton moteApplicationButton;
    private JButton regionButton;
    private JTabbedPane tabbedPane1;
    private JButton moteCharacteristicsButton;
    private JLabel moteCharacteristicsLabel;
    private JLabel moteApplicationLabel;
    private JPanel particulateMatterPanel;
    private JPanel carbonDioxideField;
    private JPanel sootPanel;
    private JPanel ozonePanel;
    private JProgressBar totalRunProgressBar;
    private JPanel distanceToGatewayGraph;
    private JPanel InputProfilePanel;
    private JLabel progressLabel;
    private JButton singleRunButton;
    private JComboBox<String> adaptationComboBox;
    private JSlider speedSlider;
    private JButton clearButton;
    private JButton editRelComButton;
    private JButton editEnConButton;
    private JButton editColBoundButton;
    private JLabel relComLabel;
    private JLabel enConLabel;
    private JLabel colBoundLabel;
    private JButton resultsButton;
    private JButton timedRunButton;
    private JPanel resultsPanel;
    private JPanel runPanel;
    private JPanel simulationPanel;
    private JToolBar toolBarSingleRun;
    private JToolBar toolBarMultiRun;
    private JSplitPane mapAndLegendListSplitPane;
    private JPanel runAndStatisticsPanel;
    private JSplitPane mainWindowSplitPane;
    private JPanel legendPanel;
    private JPanel mapPanel;
    private JSplitPane statisticsSplitPane;
    private JPanel inputProfilePanel;
    private JPanel statisticsPanel;

    private static JXMapViewer mapViewer = new JXMapViewer();
    // Create a TileFactoryInfo for OpenStreetMap
    private static TileFactoryInfo info = new OSMTileFactoryInfo();
    private static DefaultTileFactory tileFactory = new DefaultTileFactory(info);

    private SimulationRunner simulationRunner;
    private MutableInteger simulationSpeed;

    private MouseAdapter moteMouse = new MoteLegendMouseListener();
    private MouseAdapter gateWayMouse = new GatewayLegendMouseListener();

    private double usedEnergy;
    private int packetsSent;
    private int packetsLost;


    public static void main(String[] args) {
        SimulationRunner simulationRunner = SimulationRunner.getInstance();

        SwingUtilities.invokeLater(() -> {
            mapViewer.setTileFactory(tileFactory);
            tileFactory.setThreadPoolSize(GUISettings.THREADPOOLSIZE);

            if (GUISettings.USE_MAP_CACHING) {
                File cache = new File(GUISettings.PATH_CACHE_TILEFACTORY);
                tileFactory.setLocalCache(new FileBasedLocalCache(cache, false));
            }

            JFrame frame = new JFrame("Dynamic DingNet simulator");
            MainGUI gui = new MainGUI(simulationRunner);
            frame.setContentPane(gui.mainPanel);

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();

            gui.updateInputProfiles();
            gui.loadAlgorithms();
            frame.setVisible(true);

            if (GUISettings.START_FULL_SCREEN) {
                frame.setExtendedState(Frame.MAXIMIZED_BOTH);
            }
        });
    }


    public MainGUI(SimulationRunner simulationRunner) {
        this.simulationRunner = simulationRunner;
        this.simulationSpeed = new MutableInteger(GUISettings.BASE_VISUALIZATION_SPEED);

        updateInputProfiles();
        updateAdaptationGoals();

        resultsButton.setEnabled(false);
        editColBoundButton.setEnabled(false);
        editEnConButton.setEnabled(false);
        editRelComButton.setEnabled(false);
        configureButton.setEnabled(false);
        saveConfigurationButton.setEnabled(false);


        // ==============================================
        // Action listeners for the components in the GUI
        // ==============================================

        configureButton.addActionListener(new ConfigureActionListener(this));
        moteCharacteristicsButton.addActionListener(new MoteSelectActionListener(this));
        moteApplicationButton.addActionListener(new MoteApplicationSelectActionListener(this));
        editRelComButton.addActionListener(new ComRelActionListener(this));
        editColBoundButton.addActionListener(new ColBoundActionListener(this));
        editEnConButton.addActionListener(new EnConActionListener(this));
        openConfigurationButton.addActionListener(new OpenConfigurationListener());
        saveConfigurationButton.addActionListener(new SaveConfigurationListener());
        simulationSaveButton.addActionListener(new SaveSimulationResultListener());

        regionButton.addActionListener(e -> this.setPollutionGraphs(simulationRunner.getEnvironment()));


        singleRunButton.addActionListener(e -> {
            if (simulationRunner.getSimulation().getInputProfile().isEmpty()) {
                showNoInputProfileSelectedError();
                return;
            }

            this.setEnabledRunButtons(false);
            simulationRunner.setupSingleRun();

            simulationRunner.simulate(simulationSpeed, this);
        });

        timedRunButton.addActionListener(e -> {
            if (simulationRunner.getSimulation().getInputProfile().isEmpty()) {
                showNoInputProfileSelectedError();
                return;
            }

            this.setEnabledRunButtons(false);
            simulationRunner.setupTimedRun();

            simulationRunner.simulate(simulationSpeed, this);
        });

        totalRunButton.addActionListener(e -> {
            if (simulationRunner.getSimulation().getInputProfile().isEmpty()) {
                showNoInputProfileSelectedError();
                return;
            }

            this.setEnabledRunButtons(false);

            simulationRunner.totalRun(this::setProgressTotalRun);
        });


        adaptationComboBox.addActionListener((ActionEvent e) -> {
            String chosenOption = (String) adaptationComboBox.getSelectedItem();
            simulationRunner.setApproach(chosenOption);
        });

        clearButton.addActionListener((ActionEvent e) -> {
            moteCharacteristicsLabel.setText("");
            moteApplicationLabel.setText("");
            resultsButton.setEnabled(false);

            List.of(receivedPowerGraph, powerSettingGraph, spreadingFactorGraph, usedEnergyGraph, distanceToGatewayGraph,
                ozonePanel, sootPanel, carbonDioxideField, particulateMatterPanel)
                .forEach((JPanel panel) -> {
                    panel.removeAll();
                    panel.repaint();
                    panel.revalidate();
                });
        });

        helpButton.addActionListener((ActionEvent e) -> {
            HelpGUI helpgui = new HelpGUI();
            helpgui.pack();
            helpgui.setVisible(true);
        });

        aboutButton.addActionListener((ActionEvent e) -> {
            AboutSimDialog aboutSimDialog = new AboutSimDialog();
            aboutSimDialog.pack();
            aboutSimDialog.setVisible(true);
        });

        resultsButton.addActionListener((ActionEvent e) -> {
            MoteCharacteristicsDialog moteCharacteristicsDialog =
                new MoteCharacteristicsDialog(usedEnergy, packetsSent, packetsLost);
            moteCharacteristicsDialog.pack();
            moteCharacteristicsDialog.setVisible(true);
        });

        speedSlider.addChangeListener(
            e -> this.simulationSpeed.setValue(GUISettings.BASE_VISUALIZATION_SPEED * speedSlider.getValue())
        );
    }


    // region setters/getters

    public Environment getEnvironment() {
        return simulationRunner.getEnvironment();
    }

    /**
     * Update the progress bar of a total run.
     * @param progress The progress so far (left: current index, right: total amount of runs).
     */
    private void setProgressTotalRun(Pair<Integer, Integer> progress) {
        totalRunProgressBar.setMinimum(0);
        totalRunProgressBar.setMaximum(progress.getRight());
        totalRunProgressBar.setValue(progress.getLeft());
        progressLabel.setText(progress.getLeft() + "/" + progress.getRight());

        // If the runs have finished, re enable the run buttons
        if (progress.getRight().equals(progress.getLeft())) {
            this.setEnabledRunButtons(true);
        }
    }

    void setRelCom(IntervalAdaptationGoal intervalAdaptationGoal) {
        Simulation simulation = simulationRunner.getSimulation();
        QualityOfService QoS = simulationRunner.getQoS();

        simulation.getInputProfile().ifPresent(inputProfile -> {
            inputProfile.putAdaptationGoal("reliableCommunication", intervalAdaptationGoal);
            QoS.putAdaptationGoal("reliableCommunication", inputProfile.getQualityOfServiceProfile().getAdaptationGoal("reliableCommunication"));
            updateAdaptationGoals();
        });
    }

    void setEnCon(ThresholdAdaptationGoal thresholdAdaptationGoal) {
        Simulation simulation = simulationRunner.getSimulation();
        QualityOfService QoS = simulationRunner.getQoS();

        simulation.getInputProfile().ifPresent(inputProfile -> {
            inputProfile.putAdaptationGoal("energyConsumption", thresholdAdaptationGoal);
            QoS.putAdaptationGoal("energyConsumption", inputProfile.getQualityOfServiceProfile().getAdaptationGoal("energyConsumption"));
            updateAdaptationGoals();
        });

    }

    void setColBound(ThresholdAdaptationGoal thresholdAdaptationGoal) {
        Simulation simulation = simulationRunner.getSimulation();
        QualityOfService QoS = simulationRunner.getQoS();

        simulation.getInputProfile().ifPresent(inputProfile -> {
            inputProfile.putAdaptationGoal("collisionBound", thresholdAdaptationGoal);
            QoS.putAdaptationGoal("collisionBound", inputProfile.getQualityOfServiceProfile().getAdaptationGoal("collisionBound"));
            updateAdaptationGoals();
        });

    }


    private void setEnabledRunButtons(boolean state) {
        this.singleRunButton.setEnabled(state);
        this.timedRunButton.setEnabled(state);
        this.totalRunButton.setEnabled(state);
    }

    // endregion


    // region Update labels/profiles/...

    /**
     * Load all the algorithms from the simulation runner in the corresponding GUI component.
     */
    private void loadAlgorithms() {
        var algorithms = simulationRunner.getAlgorithms();

        DefaultComboBoxModel<String> adaptationComboBoxModel = new DefaultComboBoxModel<>();
        for (GenericFeedbackLoop algorithm : algorithms) {
            adaptationComboBoxModel.addElement(algorithm.getName());
        }

        adaptationComboBox.setModel(adaptationComboBoxModel);
    }

    /**
     * Update the legend next to the map with entries of gateways and motes.
     * @param environment The environment in which the gateways/motes are located.
     */
    private void updateEntries(Environment environment) {
        entitesPanel.removeAll();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2, 0, 2, 0);
        JTextArea textArea;

        for (Gateway gateway : environment.getGateways()) {
            textArea = new JTextArea();
            textArea.append(String.format("Gateway %d:\n", environment.getGateways().indexOf(gateway) + 1));
            textArea.append(String.format("EUID: %d\n", gateway.getEUI()));

            double latitude = environment.getMapHelper().toLatitude(gateway.getYPosInt());
            double longitude = environment.getMapHelper().toLongitude(gateway.getXPosInt());
            textArea.append(String.format("%s%s, %s%s",
                MapHelper.getDirectionSign(latitude, "lat"), MapHelper.toDegreeMinuteSecond(latitude),
                MapHelper.getDirectionSign(longitude, "long"), MapHelper.toDegreeMinuteSecond(longitude)));

            for (int i = 0; i < textArea.getMouseListeners().length; i++) {
                textArea.removeMouseListener(textArea.getMouseListeners()[i]);
            }

            textArea.addMouseListener(gateWayMouse);
            entitesPanel.add(textArea, constraints);
        }

        for (Mote mote : environment.getMotes()) {
            textArea = new JTextArea();
            textArea.append(String.format("Mote %d:\n", environment.getMotes().indexOf(mote) + 1));
            textArea.append(String.format("EUID: %d\n", mote.getEUI()));

            double latitude = environment.getMapHelper().toLatitude(mote.getYPosInt());
            double longitude = environment.getMapHelper().toLongitude(mote.getXPosInt());
            textArea.append(String.format("%s%s, %s%s",
                MapHelper.getDirectionSign(latitude, "lat"), MapHelper.toDegreeMinuteSecond(latitude),
                MapHelper.getDirectionSign(longitude, "long"), MapHelper.toDegreeMinuteSecond(longitude)));

            for (int i = 0; i < textArea.getMouseListeners().length; i++) {
                textArea.removeMouseListener(textArea.getMouseListeners()[i]);
            }
            textArea.addMouseListener(moteMouse);
            entitesPanel.add(textArea, constraints);
        }
        entitesPanel.repaint();
        entitesPanel.revalidate();
    }


    /**
     * Update the GUI component which shows the input profiles.
     */
    private void updateInputProfiles() {
        InputProfilePanel.removeAll();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2, 0, 2, 0);
        constraints.weighty = 0;
        JPanel panel;

        Optional<InputProfile> selectedInputProfile = simulationRunner.getSimulation().getInputProfile();

        for (InputProfile inputProfile : simulationRunner.getInputProfiles()) {
            panel = new JPanel(new BorderLayout());
            panel.setPreferredSize(new Dimension(InputProfilePanel.getWidth() - 10, 50));
            panel.setBackground(Color.white);
            panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

            JPanel subPanel1 = new JPanel();
            subPanel1.setOpaque(false);
            JPanel subPanel2 = new JPanel();
            subPanel2.setOpaque(false);

            if (selectedInputProfile.isPresent() && inputProfile == selectedInputProfile.get()) {
                subPanel1.add(new JLabel(new ImageIcon(ImageLoader.IMAGE_CIRCLE_SELECTED)));
            } else {
                subPanel1.add(new JLabel(new ImageIcon(ImageLoader.IMAGE_CIRCLE_UNSELECTED)));
            }
            subPanel1.add(new JLabel(inputProfile.getName()));
            panel.add(subPanel1, BorderLayout.WEST);
            subPanel2.add(new JLabel(new ImageIcon(ImageLoader.IMAGE_EDIT_ICON)));
            panel.add(subPanel2, BorderLayout.EAST);

            subPanel2.addMouseListener(new InputProfileEditMouse(inputProfile));
            subPanel1.addMouseListener(new InputProfileSelectMouse(inputProfile));
            InputProfilePanel.add(panel, constraints);
        }

        panel = new JPanel();
        constraints.weighty = 0.9;
        InputProfilePanel.add(panel, constraints);
        InputProfilePanel.repaint();
        InputProfilePanel.revalidate();
    }

    /**
     * Update the GUI component which shows the adaptation goals.
     */
    private void updateAdaptationGoals() {
        QualityOfService QoS = simulationRunner.getQoS();

        relComLabel.setText(String.format("Interval: %s", QoS.getAdaptationGoal("reliableCommunication").toString()));
        enConLabel.setText(String.format("Threshold: %s", QoS.getAdaptationGoal("energyConsumption").toString()));
        colBoundLabel.setText(String.format("Threshold: %.2f", Double.parseDouble(QoS.getAdaptationGoal("collisionBound").toString()) * 100));
    }

    // endregion


    // region Map drawing

    /**
     * Load the open street map, together with all the painters for motes/gateways/...
     * @param refresh If false, initialize the map viewer to the center of the map with a default zoom level.
     */
    private void loadMap(boolean refresh) {
        Environment environment = simulationRunner.getEnvironment();

        if (!refresh) {
            mapViewer.setAddressLocation(environment.getMapCenter());
            mapViewer.setZoom(5);
        }

        mapViewer.setOverlayPainter(new CompoundPainterBuilder()
            .withPollutionGrid(environment, simulationRunner.getPollutionGrid())
            .withRoutingPath(environment, simulationRunner.getRoutingApplication())
            .withMotePaths(environment)
            .withMotes(environment)
            .withGateways(environment)
            .build()
        );

        map.add(mapViewer);

        double latitude = environment.getMapCenter().getLatitude();
        double longitude = environment.getMapCenter().getLongitude();
        centerLabel.setText(" " + MapHelper.getDirectionSign(latitude, "lat") +
                MapHelper.toDegreeMinuteSecond(latitude).toString() + ", " +
                MapHelper.getDirectionSign(longitude, "long") +
                MapHelper.toDegreeMinuteSecond(longitude).toString());
    }


    /**
     * Refresh the map, as well as the legend with motes and gateways.
     */
    public void refresh() {
        refreshMap();
        updateEntries(simulationRunner.getEnvironment());

    }

    /**
     * Refresh the map.
     */
    private void refreshMap() {
        loadMap(true);
    }

    // endregion


    // region Graph updates

    /**
     * Sets the graphs of the corresponding characteristics of a given mote of a given run.
     * @param moteIndex The index of the mote.
     * @param run       The number of the run.
     */
    void setMoteHistoryGraphs(int moteIndex, int run) {
        moteCharacteristicsLabel.setText("Mote " + (moteIndex + 1) + " | Run " + (run + 1));
        resultsButton.setEnabled(true);

        BiConsumer<JPanel, ChartPanel> updateGraph = (p, c) -> {
            p.removeAll();
            p.add(c);
            p.repaint();
            p.revalidate();
        };

        Mote mote = simulationRunner.getEnvironment().getMotes().get(moteIndex);

        updateGraph.accept(receivedPowerGraph, ChartGenerator.generateReceivedPowerGraphForMotes(mote, run, this.getEnvironment()));
        updateGraph.accept(usedEnergyGraph, ChartGenerator.generateUsedEnergyGraph(mote, run));
        updateGraph.accept(powerSettingGraph, ChartGenerator.generatePowerSettingGraph(mote, run));
        updateGraph.accept(spreadingFactorGraph, ChartGenerator.generateSpreadingFactorGraph(mote, run));
        updateGraph.accept(distanceToGatewayGraph, ChartGenerator.generateDistanceToGatewayGraph(mote, run, this.getEnvironment()));

        this.updateGeneralResultsMote(mote, run);
    }

    void setPollutionGraphs(int index) {
        moteApplicationLabel.setText("Mote " + (index + 1));

        BiConsumer<JPanel, Function<Mote, ChartPanel>> updateGraph = (p, f) -> {
            p.removeAll();
            p.add(f.apply(this.getEnvironment().getMotes().get(index)));
            p.repaint();
            p.revalidate();
        };

        updateGraph.accept(particulateMatterPanel, ChartGenerator::generateParticulateMatterGraph);
        updateGraph.accept(carbonDioxideField, ChartGenerator::generateCarbonDioxideGraph);
        updateGraph.accept(sootPanel, ChartGenerator::generateSootGraph);
        updateGraph.accept(ozonePanel, ChartGenerator::generateOzoneGraph);
    }


    private void setPollutionGraphs(Environment environment) {
        moteApplicationLabel.setText("Region 1");

        BiConsumer<JPanel, Pair<JPanel, JComponent>> updateGraph = (p, r) -> {
            p.removeAll();
            p.add(r.getLeft());
            p.add(r.getRight(), BorderLayout.EAST);
            p.repaint();
            p.revalidate();
        };

        updateGraph.accept(particulateMatterPanel, ChartGenerator.generateParticulateMatterGraph(environment, tileFactory, this));
        updateGraph.accept(carbonDioxideField, ChartGenerator.generateCarbonDioxideGraph(environment, tileFactory, this));
        updateGraph.accept(sootPanel, ChartGenerator.generateSootGraph(environment, tileFactory, this));
        updateGraph.accept(ozonePanel, ChartGenerator.generateOzoneGraph(environment, tileFactory, this));
    }

    // endregion


    // region Actionlisteners/Adapters

    class MoteSelectActionListener implements ActionListener {
        private MainGUI mainGui;

        MoteSelectActionListener(MainGUI mainGui) {
            this.mainGui = mainGui;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Select Mote");
            SelectMoteGUI selectMoteGUI = new SelectMoteGUI(simulationRunner.getEnvironment(), mainGui, frame);
            frame.setContentPane(selectMoteGUI.getMainPanel());
            frame.setMinimumSize(selectMoteGUI.getMainPanel().getMinimumSize());
            frame.setPreferredSize(selectMoteGUI.getMainPanel().getPreferredSize());
            frame.setVisible(true);
        }
    }

    class ComRelActionListener implements ActionListener {
        private MainGUI mainGui;

        ComRelActionListener(MainGUI mainGui) {
            this.mainGui = mainGui;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            QualityOfService QoS = simulationRunner.getQoS();
            JFrame frame = new JFrame("Change reliable communication goal");
            EditRelComGUI editRelComGUI = new EditRelComGUI(((IntervalAdaptationGoal) QoS.getAdaptationGoal("reliableCommunication")), mainGui, frame);
            frame.setContentPane(editRelComGUI.getMainPanel());
            frame.pack();
            frame.setVisible(true);
        }
    }

    class EnConActionListener implements ActionListener {
        private MainGUI mainGui;

        EnConActionListener(MainGUI mainGui) {
            this.mainGui = mainGui;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            QualityOfService QoS = simulationRunner.getQoS();
            JFrame frame = new JFrame("Change energy consumption goal");
            EditEnConGUI editEnConGUI = new EditEnConGUI((ThresholdAdaptationGoal) QoS.getAdaptationGoal("energyConsumption"), mainGui, frame);
            frame.setContentPane(editEnConGUI.getMainPanel());
            frame.pack();
            frame.setVisible(true);
        }
    }

    class ColBoundActionListener implements ActionListener {
        private MainGUI mainGui;

        ColBoundActionListener(MainGUI mainGui) {
            this.mainGui = mainGui;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            QualityOfService QoS = simulationRunner.getQoS();
            JFrame frame = new JFrame("Change collision boundary goal");
            EditColBoundGUI editColBoundGUI = new EditColBoundGUI((ThresholdAdaptationGoal) QoS.getAdaptationGoal("collisionBound"), mainGui, frame);
            frame.setContentPane(editColBoundGUI.getMainPanel());
            frame.pack();
            frame.setVisible(true);
        }
    }

    class MoteApplicationSelectActionListener implements ActionListener {
        private MainGUI mainGui;

        MoteApplicationSelectActionListener(MainGUI mainGui) {
            this.mainGui = mainGui;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Select Mote");
            SelectMoteApplicationGUI selectMoteApplicationGUI =
                new SelectMoteApplicationGUI(simulationRunner.getEnvironment(), mainGui, frame);
            frame.setContentPane(selectMoteApplicationGUI.getMainPanel());
            frame.setMinimumSize(selectMoteApplicationGUI.getMainPanel().getMinimumSize());
            frame.setPreferredSize(selectMoteApplicationGUI.getMainPanel().getPreferredSize());
            frame.setVisible(true);
        }
    }

    private class InputProfileEditMouse extends MouseAdapter {
        private InputProfile inputProfile;

        InputProfileEditMouse(InputProfile inputProfile) {
            this.inputProfile = inputProfile;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (simulationRunner.getEnvironment() != null) {
                JFrame frame = new JFrame("Edit input profile");
                EditInputProfileGUI editInputProfileGUI =
                    new EditInputProfileGUI(inputProfile, simulationRunner.getEnvironment());
                frame.setContentPane(editInputProfileGUI.getMainPanel());
                frame.setMinimumSize(editInputProfileGUI.getMainPanel().getMinimumSize());
                frame.setPreferredSize(editInputProfileGUI.getMainPanel().getPreferredSize());
                frame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Load a configuration before editing an input profile", "InfoBox: Edit InputProfile", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }


    private class InputProfileSelectMouse extends MouseAdapter {
        private InputProfile inputProfile;

        InputProfileSelectMouse(InputProfile inputProfile) {
            this.inputProfile = inputProfile;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (simulationRunner.getEnvironment() != null) {
                Simulation simulation = simulationRunner.getSimulation();
                Optional<InputProfile> currentProfile = simulation.getInputProfile();

                if (currentProfile.isPresent() && inputProfile == currentProfile.get()) {
                    simulation.setInputProfile(null);
                    editRelComButton.setEnabled(false);
                    editColBoundButton.setEnabled(false);
                    editEnConButton.setEnabled(false);
                } else {
                    // Adjust the used inputprofile and QoS in the simulation
                    simulation.setInputProfile(inputProfile);
                    simulationRunner.updateQoS(inputProfile.getQualityOfServiceProfile());

                    Set<String> qualityNames = inputProfile.getQualityOfServiceProfile().getNames();
                    editRelComButton.setEnabled(qualityNames.contains("reliableCommunication"));
                    editEnConButton.setEnabled(qualityNames.contains("energyConsumption"));
                    editColBoundButton.setEnabled(qualityNames.contains("collisionBound"));
                }
                updateInputProfiles();
                updateAdaptationGoals();
            } else {
                JOptionPane.showMessageDialog(null, "Load a configuration before selecting an input profile",
                    "InfoBox: Select InputProfile", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }


    private static class ConfigureActionListener implements ActionListener {
        private MainGUI gui;

        ConfigureActionListener(MainGUI gui) {
            this.gui = gui;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Configure configuration");
            ConfigureGUI configureGUI = new ConfigureGUI(gui, frame);
            frame.setContentPane(configureGUI.getMainPanel());
            frame.setMinimumSize(configureGUI.getMainPanel().getMinimumSize());
            frame.setPreferredSize(configureGUI.getMainPanel().getPreferredSize());
            frame.setVisible(true);

        }
    }

    private class OpenConfigurationListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Load a configuration");
            fc.setFileFilter(new FileNameExtensionFilter("xml configuration", "xml"));

            File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            String basePath = file.getParentFile().getParent();
            fc.setCurrentDirectory(new File(Paths.get(basePath, "settings", "configurations").toUri()));

            int returnVal = fc.showOpenDialog(mainPanel);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                JFrame frame = new JFrame("Loading configuration");
                LoadingGUI loadingGUI = new LoadingGUI();
                frame.setContentPane(loadingGUI.getMainPanel());
                frame.setMinimumSize(new Dimension(300, 300));
                frame.setVisible(true);

                simulationRunner.loadConfigurationFromFile(fc.getSelectedFile());

                frame.dispose();

                updateEntries(simulationRunner.getEnvironment());
                loadMap(false);

                MouseInputListener mia = new PanMouseInputListener(mapViewer);
                mapViewer.addMouseListener(mia);
                mapViewer.addMouseMotionListener(mia);
                mapViewer.addMouseListener(new CenterMapListener(mapViewer));
                mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

                mapViewer.addPropertyChangeListener("zoom", (e) -> refresh());
                mapViewer.addPropertyChangeListener("center", (e) -> refresh());

                configureButton.setEnabled(true);
                saveConfigurationButton.setEnabled(true);
            }
        }
    }

    private class SaveConfigurationListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Save a configuration");
            fc.setFileFilter(new FileNameExtensionFilter("xml configuration", "xml"));

            File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            String basePath = file.getParentFile().getParent();
            fc.setCurrentDirectory(new File(Paths.get(basePath, "settings", "configurations").toUri()));

            int returnVal = fc.showSaveDialog(mainPanel);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                JFrame frame = new JFrame("Saving configuration");
                LoadingGUI loadingGUI = new LoadingGUI();
                frame.setContentPane(loadingGUI.getMainPanel());
                frame.setMinimumSize(new Dimension(300, 300));
                frame.setVisible(true);

                file = GUIUtil.getOutputFile(fc.getSelectedFile(), "xml");
                simulationRunner.saveConfigurationToFile(file);

                frame.dispose();
            }
        }
    }

    private class SaveSimulationResultListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Save output");
            fc.setFileFilter(new FileNameExtensionFilter("xml output", "xml"));

            File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            file = new File(file.getParent());
            fc.setCurrentDirectory(file);

            int returnVal = fc.showSaveDialog(mainPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = GUIUtil.getOutputFile(fc.getSelectedFile(), "xml");
                simulationRunner.saveSimulationToFile(file);
            }
        }
    }

    private class MoteLegendMouseListener extends MouseAdapter implements ActionListener {
        @SuppressWarnings("FieldCanBeLocal")
        private final int CLICK_INTERVAL = (int) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
        private Timer timer;
        MouseEvent event;

        public MoteLegendMouseListener() {
            timer = new Timer(CLICK_INTERVAL, this);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            event = e;

            if (timer.isRunning() && !e.isConsumed() && e.getClickCount() > 1) {
                doubleClick();
                timer.stop();
            } else {
                // Initial click -> either the timer runs out (actionPerformed method), or this method is called again before the timer runs out
                timer.restart();
            }
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            timer.stop();
            singleClick();
        }

        private int getClickedIndex() {
            JTextArea jTextArea = (JTextArea) event.getSource();
            String text = jTextArea.getText();
            return Integer.parseInt(text.substring(5, text.indexOf(":")));
        }

        private void singleClick() {
            int index = this.getClickedIndex();
            setMoteHistoryGraphs(index - 1, 0);
        }

        private void doubleClick() {
            int index = this.getClickedIndex();

            JFrame frame = new JFrame("Mote settings");
            Mote mote = simulationRunner.getEnvironment().getMotes().get(index - 1);
            MoteGUI moteGUI = new MoteGUI(getEnvironment(), mote.getOriginalPosInt(), frame, MainGUI.this, MainGUI.this, mote);
            frame.setContentPane(moteGUI.getMainPanel());
            frame.setMinimumSize(moteGUI.getMainPanel().getMinimumSize());
            frame.setPreferredSize(moteGUI.getMainPanel().getPreferredSize());
            frame.setVisible(true);
        }
    }

    private class GatewayLegendMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            JTextArea jTextArea = (JTextArea) e.getSource();
            String text = jTextArea.getText();
            int index = Integer.parseInt(text.substring(8, text.indexOf(":")));
            if (e.getClickCount() == 2) {
                Environment environment = MainGUI.this.getEnvironment();
                JFrame frame = new JFrame("Gateway settings");
                GatewayGUI gatewayGUI = new GatewayGUI(environment.getGateways().get(index - 1), environment);
                frame.setContentPane(gatewayGUI.getMainPanel());
                frame.setMinimumSize(gatewayGUI.getMainPanel().getMinimumSize());
                frame.setPreferredSize(gatewayGUI.getMainPanel().getPreferredSize());
                frame.setVisible(true);
            }
        }
    }

    // endregion


    // region Miscellanious

    private void showNoInputProfileSelectedError() {
        JOptionPane.showMessageDialog(null, "Make sure to have an input profile selected before running the simulator.",
            "Warning: no input profile selected", JOptionPane.ERROR_MESSAGE);
    }

    private void updateGeneralResultsMote(Mote mote, int run) {
        this.usedEnergy = 0;
        this.packetsLost = 0;
        this.packetsSent = 0;

        Statistics statistics = Statistics.getInstance();
        Environment environment = simulationRunner.getEnvironment();

        environment.getGateways().forEach(gw ->
            statistics.getAllReceivedTransmissions(gw.getEUI(), run).stream()
                .filter(t -> t.getSender() == mote.getEUI())
                .forEach(t -> {
                    this.packetsSent++;
                    if (t.isCollided()) {
                        this.packetsLost++;
                    }
                })
        );

        statistics.getUsedEnergy(mote.getEUI(), run).forEach(energy -> this.usedEnergy += energy);
    }


    @Override
    public void update() {
        try {
            SwingUtilities.invokeAndWait(this::refreshMap);
        } catch (InterruptedException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onEnd() {
        try {
            SwingUtilities.invokeAndWait(this::refreshMap);
        } catch (InterruptedException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        this.setEnabledRunButtons(true);
    }

    // endregion


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setMinimumSize(new Dimension(1500, 1000));
        mainPanel.setPreferredSize(new Dimension(1500, 1000));
        configurationToolBar = new JToolBar();
        configurationToolBar.setFloatable(false);
        configurationToolBar.setRollover(true);
        configurationToolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        mainPanel.add(configurationToolBar, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Configuration:");
        configurationToolBar.add(label1);
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        configurationToolBar.add(toolBar$Separator1);
        openConfigurationButton = new JButton();
        openConfigurationButton.setText("Open");
        configurationToolBar.add(openConfigurationButton);
        final JToolBar.Separator toolBar$Separator2 = new JToolBar.Separator();
        configurationToolBar.add(toolBar$Separator2);
        saveConfigurationButton = new JButton();
        saveConfigurationButton.setText("Save");
        configurationToolBar.add(saveConfigurationButton);
        final JToolBar.Separator toolBar$Separator3 = new JToolBar.Separator();
        configurationToolBar.add(toolBar$Separator3);
        configureButton = new JButton();
        configureButton.setText("Configure");
        configurationToolBar.add(configureButton);
        final Spacer spacer1 = new Spacer();
        configurationToolBar.add(spacer1);
        final JToolBar.Separator toolBar$Separator4 = new JToolBar.Separator();
        configurationToolBar.add(toolBar$Separator4);
        helpButton = new JButton();
        helpButton.setText("Help");
        configurationToolBar.add(helpButton);
        final Spacer spacer2 = new Spacer();
        configurationToolBar.add(spacer2);
        final JToolBar.Separator toolBar$Separator5 = new JToolBar.Separator();
        configurationToolBar.add(toolBar$Separator5);
        aboutButton = new JButton();
        aboutButton.setText("About");
        configurationToolBar.add(aboutButton);
        final Spacer spacer3 = new Spacer();
        configurationToolBar.add(spacer3);
        mainWindowSplitPane = new JSplitPane();
        mainWindowSplitPane.setDividerLocation(600);
        mainWindowSplitPane.setOrientation(0);
        mainPanel.add(mainWindowSplitPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        mapAndLegendListSplitPane = new JSplitPane();
        mapAndLegendListSplitPane.setMinimumSize(new Dimension(1500, 300));
        mapAndLegendListSplitPane.setPreferredSize(new Dimension(1500, 600));
        mainWindowSplitPane.setLeftComponent(mapAndLegendListSplitPane);
        legendPanel = new JPanel();
        legendPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        legendPanel.setMinimumSize(new Dimension(-1, -1));
        mapAndLegendListSplitPane.setLeftComponent(legendPanel);
        entitiesPane = new JScrollPane();
        entitiesPane.setHorizontalScrollBarPolicy(31);
        entitiesPane.setVerticalScrollBarPolicy(22);
        legendPanel.add(entitiesPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(250, -1), new Dimension(250, -1), 0, false));
        entitesPanel = new JPanel();
        entitesPanel.setLayout(new GridBagLayout());
        entitiesPane.setViewportView(entitesPanel);
        mapPanel = new JPanel();
        mapPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        mapAndLegendListSplitPane.setRightComponent(mapPanel);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        mapPanel.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Map");
        toolBar1.add(label2);
        final JToolBar.Separator toolBar$Separator6 = new JToolBar.Separator();
        toolBar1.add(toolBar$Separator6);
        final Spacer spacer4 = new Spacer();
        toolBar1.add(spacer4);
        final JLabel label3 = new JLabel();
        label3.setText("Center:");
        toolBar1.add(label3);
        centerLabel = new JLabel();
        centerLabel.setText("");
        toolBar1.add(centerLabel);
        final Spacer spacer5 = new Spacer();
        toolBar1.add(spacer5);
        map = new JPanel();
        map.setLayout(new BorderLayout(0, 0));
        map.setBackground(new Color(-4473925));
        map.setForeground(new Color(-12828863));
        mapPanel.add(map, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(400, 200), null, 0, false));
        final Spacer spacer6 = new Spacer();
        mapPanel.add(spacer6, new GridConstraints(0, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 250), null, null, 0, false));
        runAndStatisticsPanel = new JPanel();
        runAndStatisticsPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        runAndStatisticsPanel.setMinimumSize(new Dimension(1500, 35));
        runAndStatisticsPanel.setPreferredSize(new Dimension(1500, 400));
        mainWindowSplitPane.setRightComponent(runAndStatisticsPanel);
        statisticsSplitPane = new JSplitPane();
        statisticsSplitPane.setDividerLocation(450);
        runAndStatisticsPanel.add(statisticsSplitPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        inputProfilePanel = new JPanel();
        inputProfilePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        inputProfilePanel.setMinimumSize(new Dimension(465, -1));
        statisticsSplitPane.setLeftComponent(inputProfilePanel);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        inputProfilePanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        console = new JPanel();
        console.setLayout(new GridLayoutManager(1, 1, new Insets(0, 5, 0, 0), -1, -1));
        console.setBackground(new Color(-4473925));
        console.setForeground(new Color(-12828863));
        panel1.add(console, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200, 200), new Dimension(-1, 400), 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setVerticalScrollBarPolicy(20);
        console.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(250, -1), null, null, 0, false));
        InputProfilePanel = new JPanel();
        InputProfilePanel.setLayout(new GridBagLayout());
        scrollPane1.setViewportView(InputProfilePanel);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 1, new Insets(5, 5, 20, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(250, -1), null, new Dimension(-1, 150), 0, false));
        final JToolBar toolBar2 = new JToolBar();
        toolBar2.setFloatable(false);
        panel2.add(toolBar2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Adaptation Goals:");
        toolBar2.add(label4);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(400, -1), null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 5, new Insets(2, 3, 0, 3), -1, -1));
        panel4.add(panel5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, new Dimension(-1, 36), 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Reliable communication:");
        panel5.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        panel5.add(spacer7, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        relComLabel = new JLabel();
        relComLabel.setText("Interval: [-48,-42]");
        panel5.add(relComLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editRelComButton = new JButton();
        editRelComButton.setText("Edit");
        panel5.add(editRelComButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("dB");
        panel5.add(label6, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 5, new Insets(2, 3, 0, 3), -1, -1));
        panel4.add(panel6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(262, 36), new Dimension(-1, 36), 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Energy consumption:");
        panel6.add(label7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        enConLabel = new JLabel();
        enConLabel.setText("Threshold: 100");
        panel6.add(enConLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editEnConButton = new JButton();
        editEnConButton.setText("Edit");
        panel6.add(editEnConButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        panel6.add(spacer8, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("mJ/min");
        panel6.add(label8, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 5, new Insets(2, 3, 0, 3), -1, -1));
        panel4.add(panel7, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(262, 36), new Dimension(-1, 36), 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Collision Bound: ");
        panel7.add(label9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        colBoundLabel = new JLabel();
        colBoundLabel.setText("Threshold: 10");
        panel7.add(colBoundLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editColBoundButton = new JButton();
        editColBoundButton.setText("Edit");
        panel7.add(editColBoundButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        panel7.add(spacer9, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("%");
        panel7.add(label10, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JToolBar toolBar3 = new JToolBar();
        toolBar3.setFloatable(false);
        panel1.add(toolBar3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Input Profile");
        toolBar3.add(label11);
        statisticsPanel = new JPanel();
        statisticsPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        statisticsSplitPane.setRightComponent(statisticsPanel);
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation(650);
        statisticsPanel.add(splitPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setLeftComponent(panel8);
        tabbedPaneGraphs = new JTabbedPane();
        tabbedPaneGraphs.setBackground(new Color(-4473925));
        tabbedPaneGraphs.setForeground(new Color(-12828863));
        panel8.add(tabbedPaneGraphs, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(250, 400), null, 0, false));
        receivedPowerGraph = new JPanel();
        receivedPowerGraph.setLayout(new BorderLayout(0, 0));
        receivedPowerGraph.setBackground(new Color(-4473925));
        receivedPowerGraph.setForeground(new Color(-12828863));
        tabbedPaneGraphs.addTab("Received Power", receivedPowerGraph);
        powerSettingGraph = new JPanel();
        powerSettingGraph.setLayout(new BorderLayout(0, 0));
        tabbedPaneGraphs.addTab("Power Setting", powerSettingGraph);
        distanceToGatewayGraph = new JPanel();
        distanceToGatewayGraph.setLayout(new BorderLayout(0, 0));
        tabbedPaneGraphs.addTab("Distance To Gateway", distanceToGatewayGraph);
        spreadingFactorGraph = new JPanel();
        spreadingFactorGraph.setLayout(new BorderLayout(0, 0));
        tabbedPaneGraphs.addTab("Spreading Factor", spreadingFactorGraph);
        usedEnergyGraph = new JPanel();
        usedEnergyGraph.setLayout(new BorderLayout(0, 0));
        tabbedPaneGraphs.addTab("Used Energy", usedEnergyGraph);
        final JToolBar toolBar4 = new JToolBar();
        toolBar4.setFloatable(false);
        panel8.add(toolBar4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JToolBar.Separator toolBar$Separator7 = new JToolBar.Separator();
        toolBar4.add(toolBar$Separator7);
        moteCharacteristicsButton = new JButton();
        moteCharacteristicsButton.setText("Mote");
        toolBar4.add(moteCharacteristicsButton);
        final JToolBar.Separator toolBar$Separator8 = new JToolBar.Separator();
        toolBar4.add(toolBar$Separator8);
        final JLabel label12 = new JLabel();
        label12.setText("Selected: ");
        toolBar4.add(label12);
        moteCharacteristicsLabel = new JLabel();
        moteCharacteristicsLabel.setText("");
        toolBar4.add(moteCharacteristicsLabel);
        final JLabel label13 = new JLabel();
        label13.setText(" ");
        toolBar4.add(label13);
        resultsButton = new JButton();
        resultsButton.setText("Results");
        toolBar4.add(resultsButton);
        final Spacer spacer10 = new Spacer();
        toolBar4.add(spacer10);
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setRightComponent(panel9);
        final JToolBar toolBar5 = new JToolBar();
        toolBar5.setFloatable(false);
        panel9.add(toolBar5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JToolBar.Separator toolBar$Separator9 = new JToolBar.Separator();
        toolBar5.add(toolBar$Separator9);
        moteApplicationButton = new JButton();
        moteApplicationButton.setText("Mote");
        toolBar5.add(moteApplicationButton);
        final JToolBar.Separator toolBar$Separator10 = new JToolBar.Separator();
        toolBar5.add(toolBar$Separator10);
        regionButton = new JButton();
        regionButton.setText("Region");
        toolBar5.add(regionButton);
        final JToolBar.Separator toolBar$Separator11 = new JToolBar.Separator();
        toolBar5.add(toolBar$Separator11);
        final JLabel label14 = new JLabel();
        label14.setText("Selected: ");
        toolBar5.add(label14);
        moteApplicationLabel = new JLabel();
        moteApplicationLabel.setText("");
        toolBar5.add(moteApplicationLabel);
        final Spacer spacer11 = new Spacer();
        toolBar5.add(spacer11);
        tabbedPane1 = new JTabbedPane();
        panel9.add(tabbedPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        particulateMatterPanel = new JPanel();
        particulateMatterPanel.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Particulate matter", particulateMatterPanel);
        carbonDioxideField = new JPanel();
        carbonDioxideField.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Carbon dioxide", carbonDioxideField);
        sootPanel = new JPanel();
        sootPanel.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Soot", sootPanel);
        ozonePanel = new JPanel();
        ozonePanel.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Ozone", ozonePanel);
        simulationPanel = new JPanel();
        simulationPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 0, 0));
        runAndStatisticsPanel.add(simulationPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), 0, 0));
        simulationPanel.add(resultsPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JToolBar toolBar6 = new JToolBar();
        toolBar6.setFloatable(false);
        resultsPanel.add(toolBar6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 35), null, 0, false));
        final JToolBar.Separator toolBar$Separator12 = new JToolBar.Separator();
        toolBar6.add(toolBar$Separator12);
        final JLabel label15 = new JLabel();
        label15.setText("Experimental results:  ");
        toolBar6.add(label15);
        simulationSaveButton = new JButton();
        simulationSaveButton.setText("Save");
        toolBar6.add(simulationSaveButton);
        final JLabel label16 = new JLabel();
        label16.setText("  ");
        toolBar6.add(label16);
        clearButton = new JButton();
        clearButton.setText("Clear");
        toolBar6.add(clearButton);
        final JToolBar.Separator toolBar$Separator13 = new JToolBar.Separator();
        toolBar6.add(toolBar$Separator13);
        final Spacer spacer12 = new Spacer();
        toolBar6.add(spacer12);
        runPanel = new JPanel();
        runPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        simulationPanel.add(runPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 35), null, 0, false));
        toolBarAdaptation = new JToolBar();
        toolBarAdaptation.setBorderPainted(true);
        toolBarAdaptation.setFloatable(false);
        toolBarAdaptation.setRollover(true);
        toolBarAdaptation.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        runPanel.add(toolBarAdaptation, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JLabel label17 = new JLabel();
        label17.setText("Simulation  ");
        toolBarAdaptation.add(label17);
        adaptationComboBox = new JComboBox();
        toolBarAdaptation.add(adaptationComboBox);
        final JToolBar.Separator toolBar$Separator14 = new JToolBar.Separator();
        toolBarAdaptation.add(toolBar$Separator14);
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 0, 0));
        toolBarAdaptation.add(panel10);
        toolBarMultiRun = new JToolBar();
        toolBarMultiRun.setFloatable(false);
        panel10.add(toolBarMultiRun, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 35), null, 0, false));
        final JToolBar.Separator toolBar$Separator15 = new JToolBar.Separator();
        toolBarMultiRun.add(toolBar$Separator15);
        totalRunButton = new JButton();
        totalRunButton.setText("Total Run");
        toolBarMultiRun.add(totalRunButton);
        final JLabel label18 = new JLabel();
        label18.setText("  ");
        toolBarMultiRun.add(label18);
        final JLabel label19 = new JLabel();
        label19.setText("Progress: ");
        toolBarMultiRun.add(label19);
        totalRunProgressBar = new JProgressBar();
        toolBarMultiRun.add(totalRunProgressBar);
        final JToolBar.Separator toolBar$Separator16 = new JToolBar.Separator();
        toolBarMultiRun.add(toolBar$Separator16);
        progressLabel = new JLabel();
        progressLabel.setText("0/0");
        toolBarMultiRun.add(progressLabel);
        final JToolBar.Separator toolBar$Separator17 = new JToolBar.Separator();
        toolBarMultiRun.add(toolBar$Separator17);
        toolBarSingleRun = new JToolBar();
        toolBarSingleRun.setFloatable(false);
        panel10.add(toolBarSingleRun, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        singleRunButton = new JButton();
        singleRunButton.setText("Single Run");
        toolBarSingleRun.add(singleRunButton);
        final JToolBar.Separator toolBar$Separator18 = new JToolBar.Separator();
        toolBarSingleRun.add(toolBar$Separator18);
        timedRunButton = new JButton();
        timedRunButton.setText("Timed Run");
        toolBarSingleRun.add(timedRunButton);
        final JToolBar.Separator toolBar$Separator19 = new JToolBar.Separator();
        toolBarSingleRun.add(toolBar$Separator19);
        final JLabel label20 = new JLabel();
        label20.setText("Speed:");
        toolBarSingleRun.add(label20);
        speedSlider = new JSlider();
        speedSlider.setMajorTickSpacing(3);
        speedSlider.setMaximum(5);
        speedSlider.setMinimum(1);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setPaintLabels(false);
        speedSlider.setPaintTicks(true);
        speedSlider.setValue(1);
        speedSlider.setValueIsAdjusting(false);
        toolBarSingleRun.add(speedSlider);
        final JToolBar.Separator toolBar$Separator20 = new JToolBar.Separator();
        toolBarSingleRun.add(toolBar$Separator20);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}

