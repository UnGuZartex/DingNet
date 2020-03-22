package gui;

import EnvironmentAPI.GeneralSources.FunctionSources.FunctionSource;
import EnvironmentAPI.GeneralSources.PolynomialSources.PolynomialSource;
import EnvironmentAPI.GeneralSources.Source;

import EnvironmentAPI.util.SourceFactory;
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

    private List<Pair<Double,Double>> data = SetUniqueList.setUniqueList(new ArrayList<Pair<Double,Double>>());

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
            }
            catch (Exception error) {
                JOptionPane.showMessageDialog(null, "Invalid input " + error.getMessage().toLowerCase(),
                    "Warning: invalid input", JOptionPane.ERROR_MESSAGE);
            }
        });


        addFunctionalSensorButton.addActionListener(e -> {
            Source newSource = SourceFactory.createFunctionSource("0",  environment.getMapCenter(), TimeUnit.MINUTES);
            remainingList.add(newSource);
            list1.setListData(remainingList.toArray());

        });

        addPolynomialSensorButton.addActionListener(e -> {
            Pair<Double,Double> DefaultPoint = new Pair<Double,Double>(0.0,0.0);
            List<Pair<Double,Double>> points = new ArrayList<>();
            points.add(DefaultPoint);
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

    public void refresh(){
        loadMap(true);
        mainGUI.refresh();

    }

    public void updateList(){
        if (remainingList != null) {
            list1.setListData(remainingList.toArray());
        }
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
                    List<Pair<Double,Double>> newData = (List<Pair<Double, Double>>) Chosen.getDefiningFeatures();
                    for (Pair<Double,Double> point : newData) {
                        data.add(new Pair<Double,Double>(point.getLeft(),point.getRight()));
                    }
                    list2.setListData(data.toArray());

                }
                refresh();
                setSourceFunction();

            }
            catch (Exception ex) {
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
            }
            else if (toChange.getType().equals("PolynomialSource")) {
                PolynomialSource source = (PolynomialSource) toChange;
                HashSet resulting = new HashSet();
                resulting.addAll(data);
                source.clear(resulting);
            }
        }
    }

    private GeoPosition ToGeoPos(String position) {
        position = position.replace("[","");
        position = position.replace("]","");
        String[] longlat = position.split(",");
        return new GeoPosition(Double.parseDouble(longlat[0]), Double.parseDouble(longlat[1]));
    }

    private TimeUnit ToTimeUnit(String timeUnit) {
        switch (timeUnit){
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
            fc.setCurrentDirectory(new File(Paths.get(basePath, "src", "main","java","EnvironmentAPI","Configurations").toUri()));

            int returnVal = fc.showSaveDialog(panel1);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = GUIUtil.getOutputFile(fc.getSelectedFile(), "xml");
                simRunner.savePollutionConfiguration(file);
            }
            frame.dispose();
        }
    }

    protected void loadMap(boolean isRefresh) {
        if(PositionText.getText().isEmpty()){
            mapViewer.setOverlayPainter(new CompoundPainterBuilder()
                .withBorders(environment)
                .withSources(environment, simRunner.getEnvironmentAPI())
                .build()
            );
        }
        else {
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
                if(environment.isWithinBounds(geo)) {
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
