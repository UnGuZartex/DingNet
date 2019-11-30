package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import gui.mapviewer.CharacteristicPainter;
import gui.mapviewer.LinePainter;
import gui.util.AbstractConfigurePanel;
import iot.Characteristic;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

public class ConfigureRegionPanel extends AbstractConfigurePanel {
    private JPanel mainPanel;
    private JPanel drawPanel;
    private JPanel legendPanel;

    private int amountOfSquares;

    public ConfigureRegionPanel(MainGUI mainGUI) {
        super(mainGUI);
        mapViewer.addMouseListener(new RegionMouseAdapter(this));

        // FIXME Is using math.round a good idea here?
        amountOfSquares = (int) Math.round(Math.sqrt(environment.getNumberOfZones()));

        loadMap(false);
        loadLegend();
    }

    public void update() {
        drawPanel.removeAll();
        loadMap(true);
        legendPanel.removeAll();
        loadLegend();
        drawPanel.repaint();
        drawPanel.revalidate();
        legendPanel.repaint();
        legendPanel.revalidate();
    }

    private void loadLegend() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        for (Characteristic characteristic : EnumSet.allOf(Characteristic.class)) {
            legendPanel.add(new JLabel(characteristic.name() + ": "), c);
            BufferedImage img = new BufferedImage(15, 15, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
            g.fillRect(0, 0, 15, 15);
            //reset composite
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.setColor(characteristic.getColor());
            g.fill(new Ellipse2D.Double(0, 0, 15, 15));
            legendPanel.add(new JLabel(new ImageIcon(img)), c);
        }
    }

    protected void loadMap(boolean isRefresh) {
        mapViewer.removeAll();

        List<List<GeoPosition>> points = new LinkedList<>();
        List<List<GeoPosition>> verticalLines = new LinkedList<>();
        List<List<GeoPosition>> horizontalLines = new LinkedList<>();

        for (int i = 0; i <= amountOfSquares; i++) {
            List<GeoPosition> rowPoints = new LinkedList<>();
            horizontalLines.add(new LinkedList<>());
            verticalLines.add(new LinkedList<>());

            for (int j = 0; j <= amountOfSquares; j++) {
                rowPoints.add(new GeoPosition(
                    environment.getMapHelper().toLatitude((int) Math.round(j * ((double) environment.getMaxYpos()) / amountOfSquares)),
                    environment.getMapHelper().toLongitude((int) Math.round(i * ((double) environment.getMaxXpos()) / amountOfSquares)))
                );
            }
            points.add(rowPoints);
        }


        for (int i = 0; i < points.size(); i++) {
            for (int j = 0; j < points.get(i).size(); j++) {
                verticalLines.get(i).add(points.get(i).get(j));
                horizontalLines.get(j).add(points.get(i).get(j));
            }
        }


        List<GeoPosition> centerPoints = new LinkedList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            for (int j = 0; j < points.get(i).size() - 1; j++) {
                centerPoints.add(new GeoPosition(
                    (points.get(i).get(j).getLatitude() + points.get(i + 1).get(j + 1).getLatitude()) / 2.0,
                    (points.get(i).get(j).getLongitude() + points.get(i + 1).get(j + 1).getLongitude()) / 2.0
                ));
            }

        }

        List<Painter<JXMapViewer>> painters = new ArrayList<>();

        for (GeoPosition geoPosition : centerPoints) {
            Characteristic ch = environment.getCharacteristic(
                environment.getMapHelper().toMapXCoordinate(geoPosition),
                environment.getMapHelper().toMapYCoordinate(geoPosition)
            );
            painters.add(new CharacteristicPainter(geoPosition, ch.getColor()));
        }

        for (List<GeoPosition> verticalLine : verticalLines) {
            painters.add(new LinePainter(verticalLine));
        }

        for (List<GeoPosition> horizontalLine : horizontalLines) {
            painters.add(new LinePainter(horizontalLine));
        }

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(painter);

        if (!isRefresh) {
            mapViewer.setAddressLocation(environment.getMapCenter());
        }
        drawPanel.add(mapViewer);
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
        mainPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        drawPanel = new JPanel();
        drawPanel.setLayout(new BorderLayout(0, 0));
        panel1.add(drawPanel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        drawPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 15), null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 15), null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(15, -1), null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel1.add(spacer4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(15, -1), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 3, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(30, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Legend:");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        legendPanel = new JPanel();
        legendPanel.setLayout(new GridBagLayout());
        panel2.add(legendPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel2.add(spacer5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }


    private class RegionMouseAdapter implements MouseListener {

        private ConfigureRegionPanel configureRegionPanel;

        public RegionMouseAdapter(ConfigureRegionPanel configureRegionPanel) {
            this.configureRegionPanel = configureRegionPanel;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1) {
                Point p = e.getPoint();
                GeoPosition geo = mapViewer.convertPointToGeoPosition(p);
                int xPos = environment.getMapHelper().toMapXCoordinate(geo);
                int yPos = environment.getMapHelper().toMapYCoordinate(geo);
                int i = 0;
                while (xPos > 1) {
                    xPos -= environment.getMaxXpos() / amountOfSquares;
                    i++;
                }
                int j = 0;
                while (yPos > 1) {
                    yPos -= environment.getMaxYpos() / amountOfSquares;
                    j++;
                }

                JFrame frame = new JFrame("Choose Characteristics");
                CharacteristicGUI characteristicGUI = new CharacteristicGUI(environment, i - 1, j - 1, amountOfSquares, configureRegionPanel, frame);
                frame.setContentPane(characteristicGUI.getMainPanel());
                frame.setMinimumSize(characteristicGUI.getMainPanel().getMinimumSize());
                frame.setPreferredSize(characteristicGUI.getMainPanel().getPreferredSize());
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
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
}
