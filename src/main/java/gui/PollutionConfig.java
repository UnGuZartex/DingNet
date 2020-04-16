package gui;

import EnvironmentAPI.GeneralSources.FunctionSources.FunctionSource;
import EnvironmentAPI.GeneralSources.PolynomialSources.PolynomialSource;
import EnvironmentAPI.GeneralSources.Source;

import EnvironmentAPI.util.SourceFactory;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import datagenerator.iaqsensor.TimeUnit;
import gui.util.ChartGenerator;
import gui.util.CompoundPainterBuilder;
import gui.util.GUISettings;
import gui.util.GUIUtil;
import iot.Environment;
import iot.SimulationRunner;

import org.apache.commons.collections4.list.SetUniqueList;
import org.jfree.chart.ChartPanel;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import util.Pair;


import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiConsumer;

public class PollutionConfig {
    private JPanel panel1;

    private JList list1;
    private JButton addFunctionalSensorButton;
    private JButton deleteSelectedButton;
    private JFormattedTextField PositionText;
    private JFormattedTextField TimeUnitText;
    private JButton saveValuesTemporaryButton;
    private JButton saveValuesToFileButton;
    private JPanel MapPanel;
    private JButton addPolynomialSensorButton;
    private JFormattedTextField typeText;
    private JPanel GraphPanel;
    private JTextField maxX;
    private JButton redrawGraphButton;
    private JFormattedTextField functionTextField;
    private JList list2;
    private JFormattedTextField pollTextField;
    private JButton addToList;
    private JFormattedTextField timeField;
    private JLabel pollutionField;
    private JButton deleteButton;

    private SimulationRunner simRunner;

    private List<Source> remainingList;
    private JFrame frame;

    protected MainGUI mainGUI;
    protected Environment environment;

    protected JXMapViewer mapViewer = new JXMapViewer();
    protected TileFactoryInfo info = new OSMTileFactoryInfo();
    protected DefaultTileFactory tileFactory = new DefaultTileFactory(info);

    private List<Pair<Double, Double>> data = SetUniqueList.setUniqueList(new ArrayList<Pair<Double, Double>>());

    public PollutionConfig(MainGUI parent, JFrame frame, SimulationRunner simRunner) {
        this.mainGUI = parent;
        this.environment = mainGUI.getEnvironment();
        this.simRunner = simRunner;
        MapPanel.setLayout(new BorderLayout(0, 0));
        MapPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null));

        GraphPanel.setLayout(new BorderLayout(0, 0));
        GraphPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null));

        if (GUISettings.USE_MAP_CACHING) {
            File cache = new File(GUISettings.PATH_CACHE_TILEFACTORY);
            tileFactory.setLocalCache(new FileBasedLocalCache(cache, false));
        }

        for (MouseListener ml : mapViewer.getMouseListeners()) {
            mapViewer.removeMouseListener(ml);
        }
        mapViewer.setZoom(5);

        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        mapViewer.addMouseListener(new MapMouseAdapter(this.MapPanel));
        mapViewer.addPropertyChangeListener("zoom", (e) -> refresh());
        mapViewer.addPropertyChangeListener("center", (e) -> refresh());
        mapViewer.setTileFactory(tileFactory);
        tileFactory.setThreadPoolSize(GUISettings.THREADPOOLSIZE);


        loadMap(false);


        this.simRunner = simRunner;
        this.frame = frame;
        remainingList = simRunner.getEnvironmentAPI().getPoll().getSources();
        list1.setListData(remainingList.toArray());
        list1.addListSelectionListener(new SharedListSelectionHandler());
        deleteSelectedButton.addActionListener(e -> {
            if (!list1.isSelectionEmpty()) {
                remainingList.remove(list1.getSelectedIndex());
                updateList();
            }
        });

        deleteButton.addActionListener(e -> {
            if (!list2.isSelectionEmpty()) {
                data.remove(list2.getSelectedIndex());
                list2.setListData(data.toArray());
            }
        });

        addToList.addActionListener(e -> {
            try {
                data.add(new Pair<Double, Double>(Double.parseDouble(timeField.getText()), Double.parseDouble(pollTextField.getText())));
                list2.setListData(data.toArray());
            } catch (Exception error) {
                JOptionPane.showMessageDialog(null, "Invalid input " + error.getMessage().toLowerCase(),
                    "Warning: invalid input", JOptionPane.ERROR_MESSAGE);
            }
        });


        addFunctionalSensorButton.addActionListener(e -> {
            Source newSource = SourceFactory.createFunctionSource("0", environment.getMapCenter(), TimeUnit.MINUTES);
            remainingList.add(newSource);
            list1.setListData(remainingList.toArray());

        });

        addPolynomialSensorButton.addActionListener(e -> {
            List<Pair<Double, Double>> points = new ArrayList<>();
            Source newSource = SourceFactory.createPolynomialSource(points, environment.getMapCenter(), TimeUnit.MINUTES);
            remainingList.add(newSource);
            list1.setListData(remainingList.toArray());

        });
        saveValuesTemporaryButton.addActionListener(new SaveTemporaryActionListener());
        saveValuesToFileButton.addActionListener(new TotalSaveActionListener());
        redrawGraphButton.addActionListener(e -> {
            setSourceFunction();

        });

    }

    void setSourceFunction() {
        if (!list1.isSelectionEmpty()) {
            Source Chosen = remainingList.get(list1.getSelectedIndex());
            BiConsumer<JPanel, ChartPanel> updateGraph = (p, c) -> {
                p.removeAll();
                p.add(c);
                p.repaint();
                p.revalidate();
            };
            int xmax = 20;


            try {
                xmax = Integer.parseInt(maxX.getText());
            } catch (NumberFormatException n) {
            }
            updateGraph.accept(GraphPanel, ChartGenerator.generateSourceGraph(Chosen, xmax));
        }
    }

    public JPanel getMainPanel() {
        return panel1;
    }

    public void refresh() {
        loadMap(true);
        mainGUI.refresh();

    }

    public void updateList() {
        if (remainingList != null) {
            list1.setListData(remainingList.toArray());
        }
    }

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
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(29, 27, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setMinimumSize(new Dimension(800, 600));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(2, 2, 2, 25, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        PositionText = new JFormattedTextField();
        panel2.add(PositionText, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Position");
        panel2.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        TimeUnitText = new JFormattedTextField();
        TimeUnitText.setText("");
        panel2.add(TimeUnitText, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Time Unit");
        panel2.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Type");
        panel2.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        typeText = new JFormattedTextField();
        typeText.setEnabled(false);
        panel2.add(typeText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        functionTextField = new JFormattedTextField();
        functionTextField.setEnabled(false);
        panel2.add(functionTextField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Function");
        panel2.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JToolBar toolBar1 = new JToolBar();
        panel1.add(toolBar1, new GridConstraints(0, 0, 1, 27, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JToolBar toolBar2 = new JToolBar();
        panel1.add(toolBar2, new GridConstraints(28, 0, 1, 27, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("SensorSettings");
        panel1.add(label5, new GridConstraints(1, 2, 1, 24, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(3, 0, 15, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(328, 128), null, 0, false));
        list1 = new JList();
        list1.setDoubleBuffered(true);
        list1.setInheritsPopupMenu(true);
        list1.setValueIsAdjusting(true);
        scrollPane1.setViewportView(list1);
        final JLabel label6 = new JLabel();
        label6.setText("Select a source to change");
        panel1.add(label6, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(328, 16), null, 0, false));
        saveValuesToFileButton = new JButton();
        saveValuesToFileButton.setText("Save values to file**");
        panel1.add(saveValuesToFileButton, new GridConstraints(25, 2, 1, 25, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveValuesTemporaryButton = new JButton();
        saveValuesTemporaryButton.setText("Save source*");
        panel1.add(saveValuesTemporaryButton, new GridConstraints(24, 2, 1, 25, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("*Saving the source will save the value for this runtime");
        panel1.add(label7, new GridConstraints(26, 2, 1, 25, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(1, 26, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        GraphPanel = new JPanel();
        GraphPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(GraphPanel, new GridConstraints(23, 0, 5, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        deleteSelectedButton = new JButton();
        deleteSelectedButton.setText("Delete selected");
        panel1.add(deleteSelectedButton, new GridConstraints(20, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(328, 30), null, 0, false));
        addFunctionalSensorButton = new JButton();
        addFunctionalSensorButton.setText("Add Functional Source");
        panel1.add(addFunctionalSensorButton, new GridConstraints(19, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(328, 30), null, 0, false));
        addPolynomialSensorButton = new JButton();
        addPolynomialSensorButton.setText("Add Polynomial Source");
        panel1.add(addPolynomialSensorButton, new GridConstraints(18, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(328, 30), null, 0, false));
        maxX = new JTextField();
        maxX.setText("20");
        panel1.add(maxX, new GridConstraints(22, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        redrawGraphButton = new JButton();
        redrawGraphButton.setText("Redraw Graph");
        panel1.add(redrawGraphButton, new GridConstraints(22, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Max x");
        panel1.add(label8, new GridConstraints(21, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("**Saving the values will save the values of the selected source/previously saved sources");
        panel1.add(label9, new GridConstraints(27, 2, 1, 25, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        MapPanel = new JPanel();
        MapPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(MapPanel, new GridConstraints(12, 2, 12, 25, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Points");
        panel1.add(label10, new GridConstraints(4, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addToList = new JButton();
        addToList.setText("Add");
        panel1.add(addToList, new GridConstraints(11, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        timeField = new JFormattedTextField();
        timeField.setText("0");
        panel1.add(timeField, new GridConstraints(10, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        pollTextField = new JFormattedTextField();
        pollTextField.setText("0");
        panel1.add(pollTextField, new GridConstraints(10, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("t (in TimeUnit)");
        panel1.add(label11, new GridConstraints(9, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pollutionField = new JLabel();
        pollutionField.setText("Pollution");
        panel1.add(pollutionField, new GridConstraints(9, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deleteButton = new JButton();
        deleteButton.setText("Delete");
        panel1.add(deleteButton, new GridConstraints(11, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel1.add(scrollPane2, new GridConstraints(5, 2, 4, 23, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        list2 = new JList();
        scrollPane2.setViewportView(list2);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }


    private class SharedListSelectionHandler implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            try {
                Source Chosen = remainingList.get(list1.getSelectedIndex());
                PositionText.setValue(Chosen.getPosition());
                TimeUnitText.setValue(Chosen.getTimeUnit());
                typeText.setValue(Chosen.getType());
                if (Chosen.getType().equals("FunctionSource")) {
                    functionTextField.setValue(Chosen.getDefiningFeatures());
                    functionTextField.setEnabled(true);
                    list2.setListData(new Object[]{});
                    list2.setEnabled(false);
                    deleteButton.setEnabled(false);
                    addToList.setEnabled(false);

                } else if (Chosen.getType().equals("PolynomialSource")) {
                    functionTextField.setValue("");
                    functionTextField.setEnabled(false);
                    data.clear();
                    list2.setEnabled(true);
                    deleteButton.setEnabled(true);
                    addToList.setEnabled(true);
                    List<Pair<Double, Double>> newData = (List<Pair<Double, Double>>) Chosen.getDefiningFeatures();
                    for (Pair<Double, Double> point : newData) {
                        data.add(new Pair<Double, Double>(point.getLeft(), point.getRight()));
                    }
                    list2.setListData(data.toArray());

                }
                refresh();
                setSourceFunction();

            } catch (Exception ex) {
                PositionText.setValue("");
                TimeUnitText.setValue("");
                typeText.setValue("");
                functionTextField.setValue("");
            }
        }
    }

    class SaveTemporaryActionListener implements ActionListener {


        @Override
        public void actionPerformed(ActionEvent e) {
            saveToSource();
            setSourceFunction();
        }


    }

    public void saveToSource() {
        if (!list1.isSelectionEmpty()) {
            int currentlyChanged = list1.getSelectedIndex();
            Source toChange = remainingList.get(currentlyChanged);
            toChange.setPosition(ToGeoPos(PositionText.getText()));
            toChange.setTimeUnit(ToTimeUnit(TimeUnitText.getText()));
            if (toChange.getType().equals("FunctionSource")) {
                FunctionSource source = (FunctionSource) toChange;
                source.setFunction(functionTextField.getText());
            } else if (toChange.getType().equals("PolynomialSource")) {
                PolynomialSource source = (PolynomialSource) toChange;
                HashSet resulting = new HashSet();
                resulting.addAll(data);
                source.clear(resulting);
            }
        }
    }

    private GeoPosition ToGeoPos(String position) {
        position = position.replace("[", "");
        position = position.replace("]", "");
        String[] longlat = position.split(",");
        return new GeoPosition(Double.parseDouble(longlat[0]), Double.parseDouble(longlat[1]));
    }

    private TimeUnit ToTimeUnit(String timeUnit) {
        switch (timeUnit) {
            case "NANOS":
                return TimeUnit.NANOS;
            case "MICROS":
                return TimeUnit.MICROS;
            case "MILLIS":
                return TimeUnit.MILLIS;
            case "SECONDS":
                return TimeUnit.SECONDS;
            case "MINUTES":
                return TimeUnit.MINUTES;
            case "HOURS":
                return TimeUnit.HOURS;
            default:
                throw new IllegalArgumentException("INVALID TIME UNIT: " + timeUnit);

        }

    }

    class TotalSaveActionListener implements ActionListener {


        @Override
        public void actionPerformed(ActionEvent e) {
            saveToSource();
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Save PollutionConfiguration");
            fc.setFileFilter(new FileNameExtensionFilter("xml output", "xml"));

            File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            String basePath = file.getParentFile().getParent();
            fc.setCurrentDirectory(new File(Paths.get(basePath, "src", "main", "java", "EnvironmentAPI", "Configurations").toUri()));

            int returnVal = fc.showSaveDialog(panel1);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = GUIUtil.getOutputFile(fc.getSelectedFile(), "xml");
                simRunner.savePollutionConfiguration(file);
            }
            frame.dispose();
        }
    }

    protected void loadMap(boolean isRefresh) {
        if (PositionText.getText().isEmpty()) {
            mapViewer.setOverlayPainter(new CompoundPainterBuilder()
                .withBorders(environment)
                .withSources(environment, simRunner.getEnvironmentAPI())
                .build()
            );
        } else {
            mapViewer.setOverlayPainter(new CompoundPainterBuilder()
                .withBorders(environment)
                .withSources(environment, simRunner.getEnvironmentAPI())
                .withSelected(environment, PositionText.getText())
                .build()
            );
        }

        if (!isRefresh) {
            mapViewer.setAddressLocation(environment.getMapCenter());
        }

        MapPanel.add(mapViewer);
    }

    class MapMouseAdapter implements MouseListener {
        private JPanel panel;

        MapMouseAdapter(JPanel panel) {
            this.panel = panel;
        }

        public void mouseClicked(MouseEvent e) {
            loadMap(true);
            if (e.getClickCount() == 1) {
                Point p = e.getPoint();
                GeoPosition geo = mapViewer.convertPointToGeoPosition(p);
                if (environment.isWithinBounds(geo)) {
                    PositionText.setValue(geo);
                }
            }
            refresh();
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

}
