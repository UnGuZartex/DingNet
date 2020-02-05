package gui;

import EnvironmentAPI.GeneralSensor.Sensor;

import iot.SimulationRunner;


import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class PollutionConfig implements PropertyChangeListener {
    private JPanel panel1;

    private JList list1;
    private JButton addButton;
    private JButton deleteSelectedButton;
    private JFormattedTextField PositionText;
    private JFormattedTextField MaximumValueText;
    private JFormattedTextField TimeUnitText;
    private JButton configureOtherPropertiesButton;
    private JButton saveValuesTemporaryButton;
    private JButton saveValuesToFileButton;
    private JPanel MapPanel;
    private SimulationRunner simRunner;
    private List<Sensor> toDelete;
    private JFrame frame;

    public PollutionConfig(MainGUI parent, JFrame frame, SimulationRunner simRunner) {
        toDelete = new ArrayList<Sensor>();
        this.simRunner = simRunner;
        this.frame = frame;
        List<Sensor> sensorList = simRunner.getEnvironmentAPI().getSensors();
        list1.setListData(sensorList.toArray());
        list1.addListSelectionListener(new SharedListSelectionHandler());
        deleteSelectedButton.addActionListener(e -> {
            if (!list1.isSelectionEmpty()) {
                toDelete.add(sensorList.get(list1.getSelectedIndex()));
                ListModel model = list1.getModel();
                Sensor[] newList = new Sensor[model.getSize()-1];
                for (int i = 0; i < model.getSize(); i++){
                    if(i < list1.getSelectedIndex()) {
                        Sensor sensor = (Sensor) model.getElementAt(i);
                        newList[i] = (sensor);
                    }
                    if(i > list1.getSelectedIndex()) {
                        Sensor sensor = (Sensor) model.getElementAt(i);
                        newList[i-1] = (sensor);
                    }
                }
                list1.setListData(newList);
            }
        });
        saveValuesTemporaryButton.addActionListener(new SaveTemporaryActionListener());
        PositionText.addPropertyChangeListener("value", this );


    }
    public JPanel getMainPanel() {
        return panel1;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        if (source == PositionText) {
            changeValuePosition(PositionText.getValue().toString());
        } else if (source == TimeUnitText) {
            changeValueTimeUnit(TimeUnitText.getValue().toString());
        } else if (source == MaximumValueText) {
            changeValueMaximumValue(MaximumValueText.getValue().toString());
        }
    }

    private void changeValueMaximumValue(String value) {
        //TODO
    }

    private void changeValueTimeUnit(String value) {
        //TODO
    }

    private void changeValuePosition(String value) {
        //TODO
    }


    private class SharedListSelectionHandler implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            Sensor Chosen = simRunner.getEnvironmentAPI().getSensors().get(list1.getSelectedIndex());
            PositionText.setValue(Chosen.getPosition());
            TimeUnitText.setValue(Chosen.getTimeUnit());
            MaximumValueText.setValue(Chosen.getMaxValue());



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
            frame.dispose();
        }
    }
}
