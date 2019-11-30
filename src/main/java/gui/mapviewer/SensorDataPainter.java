package gui.mapviewer;

import gui.util.GUISettings;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class SensorDataPainter implements Painter<JXMapViewer> {
    private GeoPosition position;
    private Paint color;

    public SensorDataPainter(GeoPosition position, Paint color) {
        this.position = position;
        this.color = color;

    }
    @Override
    public void paint(Graphics2D g, JXMapViewer jxMapViewer, int i, int i1) {
        g = (Graphics2D) g.create();

        // convert from viewport to world bitmap
        Rectangle rect = jxMapViewer.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        if (GUISettings.USE_ANTIALIASING) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }


        Point2D pt = jxMapViewer.getTileFactory().geoToPixel(position, jxMapViewer.getZoom());
        Ellipse2D.Double circle = new Ellipse2D.Double(pt.getX(), pt.getY(), 15, 15);
        g.setPaint(color);
        g.fill(circle);
    }
}
