package gui.mapviewer;

import gui.util.GUISettings;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Paints a route
 * @author Martin Steiger
 */
public class LinePainter implements Painter<JXMapViewer> {
    private Color color;

    private List<GeoPosition> track;
    private int lineSize;


    public LinePainter(List<GeoPosition> track) {
        this(track, Color.BLACK, 1);
    }

    public LinePainter(List<GeoPosition> track, Color color, int lineSize) {
        // copy the list so that changes in the
        // original list do not have an effect here
        this.track = new ArrayList<>(track);
        this.color = color;
        this.lineSize = lineSize;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        g = (Graphics2D) g.create();

        // convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        if (GUISettings.USE_ANTIALIASING) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        // do the drawing
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(this.lineSize+1));

        drawRoute(g, map);

        // do the drawing again
        g.setColor(color);
        g.setStroke(new BasicStroke(this.lineSize));

        drawRoute(g, map);

        g.dispose();
    }

    /**
     * @param g the graphics object
     * @param map the map
     */
    private void drawRoute(Graphics2D g, JXMapViewer map) {
        int lastX = 0;
        int lastY = 0;

        boolean first = true;

        for (GeoPosition gp : track) {
            // convert geo-coordinate to world bitmap pixel
            Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());

            if (first) {
                first = false;
            } else {
                g.drawLine(lastX, lastY, (int) pt.getX(), (int) pt.getY());
            }

            lastX = (int) pt.getX();
            lastY = (int) pt.getY();
        }
    }
}
