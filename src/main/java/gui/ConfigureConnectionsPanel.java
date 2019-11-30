package gui;


import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import gui.mapviewer.LinePainter;
import gui.mapviewer.WayPointPainter;
import gui.util.AbstractConfigurePanel;
import gui.util.CompoundPainterBuilder;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import util.Connection;
import util.GraphStructure;
import util.MapHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.*;

public class ConfigureConnectionsPanel extends AbstractConfigurePanel {
    private JPanel mainPanel;
    private JPanel drawPanel;
    private JRadioButton addRadioBtn;
    private JPanel configurePanel;
    private JRadioButton deleteRadioBtn;
    private JButton cancelButton;

    private CompoundPainter<JXMapViewer> painter;
    private Mode mode;
    private long firstWayPoint;
    private long secondWayPoint;


    ConfigureConnectionsPanel(MainGUI parent) {
        super(parent, 5);
        mapViewer.addMouseListener(new MapMouseAdapter());

        this.painter = new CompoundPainter<>();
        this.mode = Mode.ADD;
        firstWayPoint = -1;
        secondWayPoint = -1;

        addRadioBtn.addActionListener(e -> {
            if (deleteRadioBtn.isSelected()) {
                deleteRadioBtn.setSelected(false);
            }

            addRadioBtn.setSelected(true);
            this.mode = Mode.ADD;
        });

        deleteRadioBtn.addActionListener(e -> {
            if (addRadioBtn.isSelected()) {
                addRadioBtn.setSelected(false);
            }
            deleteRadioBtn.setSelected(true);
            this.mode = Mode.DELETE;
        });

        cancelButton.addActionListener(e -> {
            this.resetSelectedWayPoints();
            refresh();
        });

        loadMap(false);
    }

    protected void loadMap(boolean isRefresh) {
        GraphStructure graph = mainGUI.getEnvironment().getGraph();
        painter = new CompoundPainterBuilder()
            .withBorders(environment)
            .withWaypoints(graph, false)
            .withConnections(graph)
            .build();

        mapViewer.setOverlayPainter(painter);

        if (!isRefresh) {
            mapViewer.setAddressLocation(environment.getMapCenter());
        }

        drawPanel.add(mapViewer);
    }

    private void resetSelectedWayPoints() {
        firstWayPoint = -1;
        secondWayPoint = -1;
    }


    public JPanel getMainPanel() {
        return mainPanel;
    }



    private enum Mode {
        ADD, DELETE
    }

    private class MapMouseAdapter implements MouseListener {


        MapMouseAdapter() {
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1) {
                Point p = e.getPoint();
                GeoPosition geo = mapViewer.convertPointToGeoPosition(p);
                GraphStructure graph = mainGUI.getEnvironment().getGraph();

                // Calculate the distances to the closest wayPoints
                Map<Long, Double> distances = new HashMap<>();
                graph.getWayPoints().forEach((k, v) -> distances.put(k, MapHelper.distance(v, geo)));
                var closestWayPoint = distances.entrySet().stream()
                    .min(Comparator.comparing(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElse(-1L);

                if (closestWayPoint == -1) {
                    return;
                }

                if (firstWayPoint == -1) {
                    firstWayPoint = closestWayPoint;

                    // color the selected waypoint to indicate the first step
                    var wayPointPainter = new WayPointPainter<DefaultWaypoint>(Color.BLUE);
                    wayPointPainter.setWaypoints(Set.of(new DefaultWaypoint(graph.getWayPoint(firstWayPoint))));
                    painter.addPainter(wayPointPainter);


                    // Also color the outgoing connections from the selected mote separately
                    for (var conn : graph.getOutgoingConnections(firstWayPoint)) {
                        painter.addPainter(
                            new LinePainter(List.of(graph.getWayPoint(conn.getFrom()), graph.getWayPoint(conn.getTo())), Color.GREEN, 2)
                        );
                    }
                } else {
                    secondWayPoint = closestWayPoint;

                    if (ConfigureConnectionsPanel.this.mode == Mode.ADD && firstWayPoint != secondWayPoint) {
                        graph.addConnection(new Connection(firstWayPoint, secondWayPoint));
                    } else if (ConfigureConnectionsPanel.this.mode == Mode.DELETE) {
                        graph.deleteConnection(firstWayPoint, secondWayPoint, environment);
                    }

                    loadMap(true);
                    mainGUI.refresh();
                    resetSelectedWayPoints();
                }
            }
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
        mainPanel.setLayout(new GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
        drawPanel = new JPanel();
        drawPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.add(drawPanel, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(454, 337), null, 0, false));
        drawPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 5), new Dimension(454, 5), null, 0, false));
        final Spacer spacer2 = new Spacer();
        mainPanel.add(spacer2, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 15), new Dimension(454, 14), null, 0, false));
        final Spacer spacer3 = new Spacer();
        mainPanel.add(spacer3, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(15, -1), new Dimension(14, 337), null, 0, false));
        final Spacer spacer4 = new Spacer();
        mainPanel.add(spacer4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(15, -1), new Dimension(14, 337), null, 0, false));
        configurePanel = new JPanel();
        configurePanel.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(configurePanel, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 40), new Dimension(-1, 40), new Dimension(-1, 40), 0, false));
        addRadioBtn = new JRadioButton();
        addRadioBtn.setSelected(true);
        addRadioBtn.setText("Add");
        configurePanel.add(addRadioBtn, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deleteRadioBtn = new JRadioButton();
        deleteRadioBtn.setText("Delete");
        configurePanel.add(deleteRadioBtn, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        configurePanel.add(cancelButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}

