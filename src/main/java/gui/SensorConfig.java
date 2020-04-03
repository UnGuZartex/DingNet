package gui;

import EnvironmentAPI.GeneralSources.Source;
import EnvironmentAPI.Sensor.Sensor;
import EnvironmentAPI.util.SensorFactory;
import EnvironmentAPI.util.SourceFactory;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import datagenerator.iaqsensor.TimeUnit;
import gui.util.CompoundPainterBuilder;
import gui.util.GUISettings;
import gui.util.GUIUtil;
import iot.Environment;
import iot.SimulationRunner;
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
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class SensorConfig {
    private final MainGUI mainGUI;
    private final Environment environment;
    private final SimulationRunner simRunner;
    private final JFrame frame;
    private JPanel panel1;
    private JList list1;
    private JButton addSensorButton;
    private JButton deleteSensorButton;
    private JFormattedTextField positionText;
    private JFormattedTextField MaxValueText;
    private JFormattedTextField NoiseRatioText;
    private JButton saveSensorButton;
    private JButton saveToFileButton;
    private JPanel MapPanel;

    protected JXMapViewer mapViewer = new JXMapViewer();
    protected TileFactoryInfo info = new OSMTileFactoryInfo();
    protected DefaultTileFactory tileFactory = new DefaultTileFactory(info);

    private List<Sensor> remainingList;

    public SensorConfig(MainGUI parent, JFrame frame, SimulationRunner simRunner) {
        this.mainGUI = parent;
        this.environment = mainGUI.getEnvironment();
        this.simRunner = simRunner;
        this.frame = frame;
        NumberFormatter numberFormatter = new NumberFormatter();
        NumberFormat doubleFieldFormatter = DecimalFormat.getInstance();
        doubleFieldFormatter.setGroupingUsed(false);
        numberFormatter.setFormat(doubleFieldFormatter);
        DefaultFormatterFactory factory = new DefaultFormatterFactory(
            numberFormatter, numberFormatter, numberFormatter);
        MaxValueText.setFormatterFactory(factory);

        remainingList = simRunner.getEnvironmentAPI().getSensors();
        list1.setListData(remainingList.toArray());
        MapPanel.setLayout(new BorderLayout(0, 0));
        MapPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null));

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
        mapViewer.addMouseListener(new SensorConfig.MapMouseAdapter(this.MapPanel));
        mapViewer.addPropertyChangeListener("zoom", (e) -> refresh());
        mapViewer.addPropertyChangeListener("center", (e) -> refresh());
        mapViewer.setTileFactory(tileFactory);
        tileFactory.setThreadPoolSize(GUISettings.THREADPOOLSIZE);


        deleteSensorButton.addActionListener(e -> {
            if (!list1.isSelectionEmpty()) {
                remainingList.remove(list1.getSelectedIndex());
                list1.setListData(remainingList.toArray());
            }
        });


        addSensorButton.addActionListener(e -> {
            Sensor newSensor = SensorFactory.createSensor(simRunner.getEnvironmentAPI().getPoll(), environment, environment.getMapCenter(), 255, 0);
            remainingList.add(newSensor);
            list1.setListData(remainingList.toArray());

        });
        loadMap(false);

        saveSensorButton.addActionListener(new SaveTemporaryActionListener());
        saveToFileButton.addActionListener(new TotalSaveActionListener());
        list1.addListSelectionListener(new SharedListSelectionHandler());

    }

    public void refresh() {
        loadMap(true);
        mainGUI.refresh();

    }

    protected void loadMap(boolean isRefresh) {
        if (positionText.getText().isEmpty()) {
            mapViewer.setOverlayPainter(new CompoundPainterBuilder()
                .withBorders(environment)
                .withSensors(environment, simRunner.getEnvironmentAPI())
                .build()
            );
        } else {
            mapViewer.setOverlayPainter(new CompoundPainterBuilder()
                .withBorders(environment)
                .withSensors(environment, simRunner.getEnvironmentAPI())
                .withSelected(environment, positionText.getText())
                .build()
            );
        }

        if (!isRefresh) {
            mapViewer.setAddressLocation(environment.getMapCenter());
        }

        MapPanel.add(mapViewer);
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
        panel1.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setMinimumSize(new Dimension(800, 600));
        panel1.setPreferredSize(new Dimension(800, 600));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        list1 = new JList();
        scrollPane1.setViewportView(list1);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Position");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("MaxValue");
        panel2.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("NoiseRatio");
        panel2.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        positionText = new JFormattedTextField();
        panel2.add(positionText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        MaxValueText = new JFormattedTextField();
        panel2.add(MaxValueText, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        NoiseRatioText = new JFormattedTextField();
        panel2.add(NoiseRatioText, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        MapPanel = new JPanel();
        MapPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(MapPanel, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        addSensorButton = new JButton();
        addSensorButton.setText("Add Sensor");
        panel1.add(addSensorButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deleteSensorButton = new JButton();
        deleteSensorButton.setText("Delete Sensor");
        panel1.add(deleteSensorButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveSensorButton = new JButton();
        saveSensorButton.setText("Save Sensor");
        panel1.add(saveSensorButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveToFileButton = new JButton();
        saveToFileButton.setText("Save to file");
        panel1.add(saveToFileButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JToolBar toolBar1 = new JToolBar();
        panel1.add(toolBar1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JToolBar toolBar2 = new JToolBar();
        panel1.add(toolBar2, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
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
                if (!list1.isSelectionEmpty()) {
                    Sensor Chosen = remainingList.get(list1.getSelectedIndex());
                    positionText.setValue(Chosen.getPosition());
                    MaxValueText.setValue(Chosen.getMaxValue());
                    NoiseRatioText.setValue(Chosen.getNoiseRatio());
                } else {
                    positionText.setValue("");
                    MaxValueText.setValue("");
                    NoiseRatioText.setValue("");
                }
                refresh();
            } catch (Exception ex) {
                positionText.setValue("");
                MaxValueText.setValue("");
                NoiseRatioText.setValue("");
            }
        }
    }

    public JPanel getMainPanel() {
        return panel1;
    }

    class SaveTemporaryActionListener implements ActionListener {


        @Override
        public void actionPerformed(ActionEvent e) {
            saveToSource();
        }


    }

    public void saveToSource() {
        if (!list1.isSelectionEmpty()) {
            int currentlyChanged = list1.getSelectedIndex();
            Sensor toChange = remainingList.get(currentlyChanged);
            toChange.setPosition(ToGeoPos(positionText.getText()));
            String maxValue = MaxValueText.getText().replace(",", ".");
            toChange.setMaxValue(Double.parseDouble(maxValue));
            toChange.setNoiseRatio(Integer.parseInt(NoiseRatioText.getText()));
            System.out.println(toChange.getMaxValue());
        }
    }

    private GeoPosition ToGeoPos(String position) {
        position = position.replace("[", "");
        position = position.replace("]", "");
        String[] longlat = position.split(",");
        return new GeoPosition(Double.parseDouble(longlat[0]), Double.parseDouble(longlat[1]));
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
                    positionText.setValue(geo);
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
