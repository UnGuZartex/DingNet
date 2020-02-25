package gui;

import EnvironmentAPI.GeneralSensor.FunctionSensor.FunctionSensor;
import EnvironmentAPI.GeneralSensor.PolynomialSensor.PolynomialSensor;
import EnvironmentAPI.GeneralSensor.Sensor;

import datagenerator.iaqsensor.TimeUnit;
import gui.util.GUIUtil;
import iot.SimulationRunner;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;


import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
    private SimulationRunner simRunner;
    private List<Sensor> toDelete;
    private List<Sensor> toAdd;

    private List<Sensor> remainingList;
    private JFrame frame;

    public PollutionConfig(MainGUI parent, JFrame frame, SimulationRunner simRunner) {
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
            Sensor newSensor = new FunctionSensor(new GeoPosition(0,0), "x", 255, TimeUnit.MINUTES);
            toAdd.add(newSensor);
            remainingList.add(newSensor);
            list1.setListData(remainingList.toArray());

        });

        addPolynomialSensorButton.addActionListener(e -> {
            Pair<Double,Double> DefaultPoint = new Pair<Double,Double>(0.0,0.0);
            List<Pair<Double,Double>> points = new ArrayList<>();
            points.add(DefaultPoint);
            Sensor newSensor = new PolynomialSensor(points, new GeoPosition(0,0), 255, TimeUnit.MINUTES);
            toAdd.add(newSensor);
            remainingList.add(newSensor);
            list1.setListData(remainingList.toArray());

        });
        saveValuesTemporaryButton.addActionListener(new SaveTemporaryActionListener());
        saveValuesToFileButton.addActionListener(new TotalSaveActionListener());


    }
    public JPanel getMainPanel() {
        return panel1;
    }


    private class SharedListSelectionHandler implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            Sensor Chosen = simRunner.getEnvironmentAPI().getSensors().get(list1.getSelectedIndex());
            PositionText.setValue(Chosen.getPosition());
            TimeUnitText.setValue(Chosen.getTimeUnit());
            MaximumValueText.setValue(Chosen.getMaxValue());
            typeText.setValue(Chosen.getType());




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
}
