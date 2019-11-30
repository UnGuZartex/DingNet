package gui;


import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import gui.util.GUIUtil;
import gui.util.Refreshable;
import iot.Environment;
import iot.networkentity.Mote;
import iot.networkentity.MoteFactory;
import iot.networkentity.MoteSensor;
import iot.networkentity.UserMote;
import org.jxmapviewer.viewer.GeoPosition;
import util.GraphStructure;
import util.Pair;
import util.Path;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class MoteGUI {
    private JPanel mainPanel;
    private JTextField EUItextField;
    private JSpinner powerSpinner;
    private JSpinner SFSpinner;
    private JButton saveButton;
    private JButton generateEUIButton;
    private JButton addMoteSensorButton;
    private JList<MoteSensor> sensorList;
    private JComboBox<MoteSensor> moteSensorComboBox;
    private JSpinner movementSpeedSpinner;
    private JSpinner offsetMovementSpinner;
    private JSpinner periodSpinner;
    private JSpinner offsetSendingSpinner;
    private JCheckBox isUserMoteCheckBox;
    private JCheckBox isActiveCheckBox;
    private JLabel destinationLabel;
    private JButton chooseDestinationButton;
    private JLabel positionLabel;
    private JRadioButton newPositionRadioButton;
    private JRadioButton existingPositionRadioButton;
    private JLabel xPositionLabel;
    private JLabel yPositionLabel;
    private JLabel latPositionLabel;
    private JLabel lonPositionLabel;
    private JLabel destinationWaypointLabel;
    private Environment environment;

    private Random random;
    private GeoPosition source;
    private MainGUI mainGUI;
    private Mote mote;

    public MoteGUI(Environment environment, Pair<Integer, Integer> pos, JFrame frame, Refreshable parent, MainGUI mainGUI, Mote mote) {
        this(environment, environment.getMapHelper().toGeoPosition(pos), frame, parent, mainGUI, mote);
    }

    public MoteGUI(Environment environment, GeoPosition geoPosition, JFrame frame, Refreshable parent, MainGUI mainGUI) {
        this(environment, geoPosition, frame, parent, mainGUI, null);
    }

    public MoteGUI(Environment environment, GeoPosition geoPosition, JFrame frame, Refreshable parent, MainGUI mainGUI, Mote mote) {
        // region Initializing fields/params
        this.environment = environment;
        this.random = new Random();
        this.mainGUI = mainGUI;
        this.mote = mote;

        boolean isNewMote = mote == null;

        // endregion


        // region Initializing GUI components
        updateSourcePosition(geoPosition);

        // Disable new EUI generation for existing motes
        EUItextField.setText(isNewMote ? generateNewEUIString() : Long.toUnsignedString(mote.getEUI()));
        EUItextField.setEnabled(isNewMote);
        generateEUIButton.setEnabled(isNewMote);

        powerSpinner.setModel(new SpinnerNumberModel(isNewMote ? 14 : mote.getTransmissionPower(), -3, 14, 1));
        SFSpinner.setModel(new SpinnerNumberModel(isNewMote ? 12 : mote.getSF(), 1, 12, 1));
        movementSpeedSpinner.setModel(new SpinnerNumberModel(isNewMote ? 1 : mote.getMovementSpeed(), 0.01, 1000, 0.01));
        periodSpinner.setModel(new SpinnerNumberModel(isNewMote ? 30 : mote.getPeriodSendingPacket(), 1, Integer.MAX_VALUE, 1));
        offsetSendingSpinner.setModel(new SpinnerNumberModel(isNewMote ? 1 : mote.getStartSendingOffset(), 1, Integer.MAX_VALUE, 1));
        offsetMovementSpinner.setModel(new SpinnerNumberModel(isNewMote ? 0 : mote.getStartMovementOffset(), 0, Integer.MAX_VALUE, 1));
        moteSensorComboBox.setModel(new DefaultComboBoxModel<>(MoteSensor.values()));

        DefaultListModel<MoteSensor> sensorListModel = new DefaultListModel<>();
        if (!isNewMote) {
            mote.getSensors().forEach(sensorListModel::addElement);
        }
        sensorList.setModel(sensorListModel);

        ButtonGroup positionButtonGroup = new ButtonGroup();
        positionButtonGroup.add(newPositionRadioButton);
        positionButtonGroup.add(existingPositionRadioButton);

        // For new motes, add a new waypoint by default
        // Reverse case for existing motes
        newPositionRadioButton.setSelected(isNewMote);
        existingPositionRadioButton.setSelected(!isNewMote);

        if (!isNewMote) {
            initializeExistingUserMoteFields();
        } else {
            updateUserMoteFields(true);
        }

        // endregion


        // region listeners

        newPositionRadioButton.addActionListener(e -> spawnMapFrame(this::updateSourcePosition));
        existingPositionRadioButton.addActionListener(e -> spawnMapFrame(this::updateSourcePosition));

        chooseDestinationButton.addActionListener(e ->
                spawnMapFrame(selectedPosition ->
                    destinationWaypointLabel.setText(Long.toUnsignedString(environment.getGraph().getClosestWayPoint(selectedPosition))))
        );

        generateEUIButton.addActionListener(e -> this.EUItextField.setText(generateNewEUIString()));

        addMoteSensorButton.addActionListener(e -> {
            DefaultListModel<MoteSensor> listModel = new DefaultListModel<>();

            for (int i = 0; i < sensorList.getModel().getSize(); i++) {
                listModel.addElement(sensorList.getModel().getElementAt(i));
            }

            listModel.addElement((MoteSensor) moteSensorComboBox.getSelectedItem());
            sensorList.setModel(listModel);
        });

        isUserMoteCheckBox.addChangeListener(evt -> this.updateUserMoteFields(isNewMote));

        saveButton.addActionListener((e) -> {
            if (!isNewMote) {
                updateMote(mote, isUserMoteCheckBox.isSelected());
            } else if (isUserMoteCheckBox.isSelected()) {
                addUserMote();
            } else {
                addMote();
            }

            parent.refresh();
            frame.dispose();
        });

        // endregion
    }

    private void initializeExistingUserMoteFields() {
        // We are working with an existing mote -> do not allow changing of class type
        isUserMoteCheckBox.setVisible(false);
        isUserMoteCheckBox.setSelected(mote instanceof UserMote);
        updateUserMoteFields(false);
    }


    private void updateSourcePosition(GeoPosition pos) {
        if (newPositionRadioButton.isSelected()) {
            this.source = pos;
        } else {
            GraphStructure graph = this.environment.getGraph();
            this.source = graph.getWayPoint(graph.getClosestWayPoint(pos));
        }

        // Update the labels for the x/y positions and lat/lon accordingly
        GUIUtil.updateLabelCoordinateLat(latPositionLabel, pos.getLatitude());
        GUIUtil.updateLabelCoordinateLon(lonPositionLabel, pos.getLongitude());

        xPositionLabel.setText(String.format("x: %d", environment.getMapHelper().toMapXCoordinate(pos)));
        yPositionLabel.setText(String.format("y: %d", environment.getMapHelper().toMapYCoordinate(pos)));
    }

    private void updateUserMoteFields(boolean isNewMote) {
        if (isUserMoteCheckBox.isSelected()) {
            setVisibleUserMoteComponents(true);

            // If we are creating a new mote, the destination is not set by default
            if (isNewMote) {
                destinationWaypointLabel.setText("");
                isActiveCheckBox.setSelected(false);
            } else {
                UserMote userMote = (UserMote) mote;
                long destinationWayPointId = environment.getGraph().getClosestWayPoint(userMote.getDestination());

                destinationWaypointLabel.setText(Long.toUnsignedString(destinationWayPointId));
                isActiveCheckBox.setSelected(userMote.isActive());
            }
        } else {
            // Not a usermote, hide the fields specific to usermotes
            setVisibleUserMoteComponents(false);
        }
    }


    private String generateNewEUIString() {
        return Long.toUnsignedString(random.nextLong());
    }

    private void setVisibleUserMoteComponents(boolean state) {
        isActiveCheckBox.setVisible(state);
        destinationLabel.setVisible(state);
        chooseDestinationButton.setVisible(state);
        destinationWaypointLabel.setVisible(state);
    }

    private void spawnMapFrame(Consumer<GeoPosition> consumer) {
        JFrame framePositionSelection = new JFrame("Choose a position");
        DestinationGUI destinationGUI = new DestinationGUI(framePositionSelection, mainGUI, consumer);
        framePositionSelection.setContentPane(destinationGUI.getMainPanel());
        framePositionSelection.setMinimumSize(destinationGUI.getMainPanel().getMinimumSize());
        framePositionSelection.setPreferredSize(destinationGUI.getMainPanel().getPreferredSize());
        framePositionSelection.setVisible(true);
    }


    private List<MoteSensor> getSelectedMoteSensors() {
        List<MoteSensor> moteSensors = new LinkedList<>();
        for (Object sensor : ((DefaultListModel) sensorList.getModel()).toArray()) {
            moteSensors.add((MoteSensor) sensor);
        }
        return moteSensors;
    }


    private Pair<Integer, Integer> handleChosenStartingPosition() {
        if (newPositionRadioButton.isSelected()) {
            // Add a new waypoint to the graph
            environment.getGraph().addWayPoint(source);
        }
        return environment.getMapHelper().toMapCoordinate(source);
    }


    private void addMote() {
        Pair<Integer, Integer> position = handleChosenStartingPosition();

        // TODO Energy level? Add field as well?
        environment.addMote(MoteFactory.createMote(Long.parseUnsignedLong(EUItextField.getText()), position.getLeft(), position.getRight(),
                (int) powerSpinner.getValue(),
                (int) SFSpinner.getValue(), this.getSelectedMoteSensors(), 20, new Path(environment.getGraph()),
                (double) movementSpeedSpinner.getValue(),
                (int) offsetMovementSpinner.getValue(), (int) periodSpinner.getValue(),
                (int) offsetSendingSpinner.getValue(), environment));
    }

    private void addUserMote() {
        GraphStructure graph = environment.getGraph();
        Pair<Integer, Integer> position = handleChosenStartingPosition();

        // if no destination waypoint is set, default to the starting waypoint on which the mote is located
        long selectedDestinationIndex = destinationWaypointLabel.getText().equals("") ?
                graph.getClosestWayPoint(source) :
                Long.parseUnsignedLong(destinationWaypointLabel.getText());

        // TODO Energy level? Add field as well?
        UserMote userMote = MoteFactory.createUserMote(Long.parseUnsignedLong(EUItextField.getText()), position.getLeft(),
                position.getRight(), (int) powerSpinner.getValue(),
                (int) SFSpinner.getValue(), this.getSelectedMoteSensors(), 20, new Path(environment.getGraph()),
                (double) movementSpeedSpinner.getValue(),
                (int) offsetMovementSpinner.getValue(), (int) periodSpinner.getValue(),
                (int) offsetSendingSpinner.getValue(), graph.getWayPoint(selectedDestinationIndex), environment);
        userMote.setActive(isActiveCheckBox.isSelected());
        environment.addMote(userMote);
    }

    private void updateMote(Mote mote, boolean isUserMote) {
        Pair<Integer, Integer> position = handleChosenStartingPosition();

        mote.updateInitialPosition(position);

        mote.setSF((int) SFSpinner.getValue());
        mote.setTransmissionPower((int) powerSpinner.getValue());
        mote.setMovementSpeed((double) movementSpeedSpinner.getValue());
        mote.setStartMovementOffset((int) offsetMovementSpinner.getValue());
        mote.setStartSendingOffset((int) offsetSendingSpinner.getValue());
        mote.setPeriodSendingPacket((int) periodSpinner.getValue());

        List<MoteSensor> moteSensors = new LinkedList<>();
        for (Object sensor : ((DefaultListModel) sensorList.getModel()).toArray()) {
            moteSensors.add((MoteSensor) sensor);
        }
        mote.setSensors(moteSensors);

        if (isUserMote) {
            UserMote userMote = (UserMote) mote;
            userMote.setActive(isActiveCheckBox.isSelected());

            // Destination label cannot be empty, since the usermote has to have had a destination already
            long index = Long.parseUnsignedLong(destinationWaypointLabel.getText());
            userMote.setDestination(environment.getGraph().getWayPoint(index));
        }
    }


    public JPanel getMainPanel() {
        return mainPanel;
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
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(19, 7, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setMinimumSize(new Dimension(800, 700));
        mainPanel.setPreferredSize(new Dimension(800, 700));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 5), new Dimension(-1, 5), new Dimension(-1, 5), 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("EUI");
        mainPanel.add(label1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        EUItextField = new JTextField();
        mainPanel.add(EUItextField, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        generateEUIButton = new JButton();
        generateEUIButton.setText("Generate");
        mainPanel.add(generateEUIButton, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        positionLabel = new JLabel();
        positionLabel.setText("Position");
        mainPanel.add(positionLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        mainPanel.add(spacer2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        mainPanel.add(spacer3, new GridConstraints(3, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Powersetting");
        mainPanel.add(label2, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        powerSpinner = new JSpinner();
        mainPanel.add(powerSpinner, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Spreading factor");
        mainPanel.add(label3, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        SFSpinner = new JSpinner();
        mainPanel.add(SFSpinner, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveButton = new JButton();
        saveButton.setText("Save");
        mainPanel.add(saveButton, new GridConstraints(17, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        mainPanel.add(spacer4, new GridConstraints(18, 2, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 5), new Dimension(-1, 5), new Dimension(-1, 5), 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Sensors");
        mainPanel.add(label4, new GridConstraints(15, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addMoteSensorButton = new JButton();
        addMoteSensorButton.setText("Add");
        mainPanel.add(addMoteSensorButton, new GridConstraints(15, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sensorList = new JList();
        mainPanel.add(sensorList, new GridConstraints(16, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        moteSensorComboBox = new JComboBox();
        mainPanel.add(moteSensorComboBox, new GridConstraints(15, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Movement speed");
        mainPanel.add(label5, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        movementSpeedSpinner = new JSpinner();
        mainPanel.add(movementSpeedSpinner, new GridConstraints(8, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("m/s");
        mainPanel.add(label6, new GridConstraints(8, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Offset start movement ");
        mainPanel.add(label7, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        offsetMovementSpinner = new JSpinner();
        mainPanel.add(offsetMovementSpinner, new GridConstraints(9, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Period for sending packet");
        mainPanel.add(label8, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("seconds");
        mainPanel.add(label9, new GridConstraints(6, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        periodSpinner = new JSpinner();
        mainPanel.add(periodSpinner, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Offset start sending packet");
        mainPanel.add(label10, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        offsetSendingSpinner = new JSpinner();
        mainPanel.add(offsetSendingSpinner, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("seconds");
        mainPanel.add(label11, new GridConstraints(7, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isUserMoteCheckBox = new JCheckBox();
        isUserMoteCheckBox.setText("is user mote");
        mainPanel.add(isUserMoteCheckBox, new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        destinationLabel = new JLabel();
        destinationLabel.setEnabled(true);
        destinationLabel.setText("Destination (waypoint ID)");
        mainPanel.add(destinationLabel, new GridConstraints(13, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        mainPanel.add(spacer5, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 5), new Dimension(-1, 5), new Dimension(-1, 5), 0, false));
        final Spacer spacer6 = new Spacer();
        mainPanel.add(spacer6, new GridConstraints(14, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 10), new Dimension(-1, 10), new Dimension(-1, 10), 0, false));
        chooseDestinationButton = new JButton();
        chooseDestinationButton.setEnabled(true);
        chooseDestinationButton.setText("Select");
        mainPanel.add(chooseDestinationButton, new GridConstraints(13, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        newPositionRadioButton = new JRadioButton();
        newPositionRadioButton.setText("New waypoint");
        mainPanel.add(newPositionRadioButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        existingPositionRadioButton = new JRadioButton();
        existingPositionRadioButton.setText("Existing waypoint");
        mainPanel.add(existingPositionRadioButton, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xPositionLabel = new JLabel();
        xPositionLabel.setText("Label");
        mainPanel.add(xPositionLabel, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        yPositionLabel = new JLabel();
        yPositionLabel.setText("Label");
        mainPanel.add(yPositionLabel, new GridConstraints(2, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        destinationWaypointLabel = new JLabel();
        destinationWaypointLabel.setText("Label");
        mainPanel.add(destinationWaypointLabel, new GridConstraints(13, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        latPositionLabel = new JLabel();
        latPositionLabel.setText("Label");
        mainPanel.add(latPositionLabel, new GridConstraints(3, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lonPositionLabel = new JLabel();
        lonPositionLabel.setText("Label");
        mainPanel.add(lonPositionLabel, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        mainPanel.add(spacer7, new GridConstraints(3, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, new Dimension(5, -1), new Dimension(5, -1), null, 0, false));
        isActiveCheckBox = new JCheckBox();
        isActiveCheckBox.setEnabled(true);
        isActiveCheckBox.setText("is active");
        mainPanel.add(isActiveCheckBox, new GridConstraints(12, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
