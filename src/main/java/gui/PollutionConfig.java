package gui;

import EnvironmentAPI.GeneralSensor.Sensor;

import EnvironmentAPI.util.SensorFactory;
import datagenerator.iaqsensor.TimeUnit;
import gui.util.ChartGenerator;
import gui.util.CompoundPainterBuilder;
import gui.util.GUISettings;
import gui.util.GUIUtil;
import iot.Environment;
import iot.SimulationRunner;

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
import java.util.List;
import java.util.function.BiConsumer;

public class PollutionConfig {
    private JPanel panel1;

    private JList list1;
    private JButton addFunctionalSensorButton;
    private JButton deleteSelectedButton;
    private JFormattedTextField PositionText;
    private JFormattedTextField MaximumValueText;
    private JFormattedTextField TimeUnitText;
    private JButton configureOtherPropertiesButton;
    private JButton saveValuesTemporaryButton;
    private JButton saveValuesToFileButton;
    private JPanel MapPanel;
    private JButton addPolynomialSensorButton;
    private JFormattedTextField typeText;
    private JFormattedTextField NoiseField;
    private JPanel GraphPanel;
    private SimulationRunner simRunner;
    private List<Sensor> toDelete;
    private List<Sensor> toAdd;

    private List<Sensor> remainingList;
    private JFrame frame;

    protected MainGUI mainGUI;
    protected Environment environment;

    protected JXMapViewer mapViewer = new JXMapViewer();
    protected TileFactoryInfo info = new OSMTileFactoryInfo();
    protected DefaultTileFactory tileFactory = new DefaultTileFactory(info);

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



        toDelete = new ArrayList<Sensor>();
        toAdd = new ArrayList<Sensor>();
        this.simRunner = simRunner;
        this.frame = frame;
        remainingList = simRunner.getEnvironmentAPI().getSensors();
        List<Sensor> sensorList = simRunner.getEnvironmentAPI().getSensors();
        list1.setListData(sensorList.toArray());
        list1.addListSelectionListener(new SharedListSelectionHandler());
        deleteSelectedButton.addActionListener(e -> {
            if (!list1.isSelectionEmpty()) {
                toDelete.add(remainingList.get(list1.getSelectedIndex()));
                remainingList.remove(list1.getSelectedIndex());
                ListModel model = list1.getModel();
                Sensor[] newList = new Sensor[model.getSize()-1];
                for (int i = 0; i < model.getSize(); i++){
                    if(i < list1.getSelectedIndex()) {
                        Sensor sensor = (Sensor) model.getElementAt(i);
                        newList[i] = sensor;
                    }
                    if(i > list1.getSelectedIndex()) {
                        Sensor sensor = (Sensor) model.getElementAt(i);
                        newList[i-1] = sensor;
                    }
                }
                list1.setListData(newList);
            }
        });


        addFunctionalSensorButton.addActionListener(e -> {
            Sensor newSensor = SensorFactory.createFunctionSensor("0", 255.0, new GeoPosition(0,0), TimeUnit.MINUTES, 1);
            toAdd.add(newSensor);
            remainingList.add(newSensor);
            list1.setListData(remainingList.toArray());

        });

        addPolynomialSensorButton.addActionListener(e -> {
            Pair<Double,Double> DefaultPoint = new Pair<Double,Double>(0.0,0.0);
            List<Pair<Double,Double>> points = new ArrayList<>();
            points.add(DefaultPoint);
            Sensor newSensor = SensorFactory.createPolynomialSensor(points, 255.0, new GeoPosition(0,0), TimeUnit.MINUTES, 1);
            toAdd.add(newSensor);
            remainingList.add(newSensor);
            list1.setListData(remainingList.toArray());

        });
        saveValuesTemporaryButton.addActionListener(new SaveTemporaryActionListener());
        saveValuesToFileButton.addActionListener(new TotalSaveActionListener());


    }

    void setSensorFunction(Sensor Chosen) {
        BiConsumer<JPanel, ChartPanel> updateGraph = (p, c) -> {
            p.removeAll();
            p.add(c);
            p.repaint();
            p.revalidate();
        };

        updateGraph.accept(GraphPanel, ChartGenerator.generateSensorGraph(Chosen));
    }
    public JPanel getMainPanel() {
        return panel1;
    }

    public void refresh(){
        loadMap(true);
        mainGUI.refresh();
    }

    private class SharedListSelectionHandler implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            Sensor Chosen = remainingList.get(list1.getSelectedIndex());
            PositionText.setValue(Chosen.getPosition());
            TimeUnitText.setValue(Chosen.getTimeUnit());
            MaximumValueText.setValue(Chosen.getMaxValue());
            typeText.setValue(Chosen.getType());
            NoiseField.setValue(Chosen.getNoiseRatio());
            refresh();
            setSensorFunction(Chosen);

        }
    }

    class SaveTemporaryActionListener implements ActionListener {


        @Override
        public void actionPerformed(ActionEvent e) {
            if(!toDelete.isEmpty()){
                for(int i = 0; i < toDelete.size(); i++){
                    simRunner.getEnvironmentAPI().removeSensor(toDelete.get(i));
                }
            }
            toDelete.clear();
            int currentlyChanged = list1.getSelectedIndex();
            Sensor toChange = simRunner.getEnvironmentAPI().getSensors().get(currentlyChanged);
            toChange.setPosition(ToGeoPos(PositionText.getText()));
            toChange.setMaxValue(Integer.valueOf(MaximumValueText.getText()));
            toChange.setTimeUnit(ToTimeUnit(TimeUnitText.getText()));
            toChange.setNoiseRatio(Integer.parseInt(NoiseField.getText()));
            refresh();
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
    }

    class TotalSaveActionListener implements ActionListener {


        @Override
        public void actionPerformed(ActionEvent e) {
            if(!toDelete.isEmpty()){
                for(int i = 0; i < toDelete.size(); i++){
                    simRunner.getEnvironmentAPI().removeSensor(toDelete.get(i));
                }
            }
            toDelete.clear();
            int currentlyChanged = list1.getSelectedIndex();
            Sensor toChange = simRunner.getEnvironmentAPI().getSensors().get(currentlyChanged);
            toChange.setPosition(ToGeoPos(PositionText.getText()));
            toChange.setMaxValue(Integer.valueOf(MaximumValueText.getText()));
            toChange.setTimeUnit(ToTimeUnit(TimeUnitText.getText()));
            toChange.setNoiseRatio(Integer.parseInt(NoiseField.getText()));
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
    }

    protected void loadMap(boolean isRefresh) {
        if(PositionText.getText().isEmpty()){
            mapViewer.setOverlayPainter(new CompoundPainterBuilder()
                .withBorders(environment)
                .withSensors(environment,simRunner.getEnvironmentAPI())
                .build()
            );
        }
        else {
            mapViewer.setOverlayPainter(new CompoundPainterBuilder()
                .withBorders(environment)
                .withSensors(environment, simRunner.getEnvironmentAPI())
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
                PositionText.setValue(geo);
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
