package gui.mapviewer;

import gui.util.GUISettings;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.AbstractPainter;
import org.jxmapviewer.viewer.Waypoint;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Set;

/**
 * Paints waypoints on the JXMapViewer. This is an
 * instance of Painter that only can draw on to JXMapViewers.
 * @param <W> the waypoint type
 */
public class WayPointPainter<W extends Waypoint> extends AbstractPainter<JXMapViewer> {
    private Set<W> waypoints;
    private BufferedImage img;

    public WayPointPainter() {
        this(GUISettings.DEFAULT_WAYPOINT_COLOR);
    }

    public WayPointPainter(Color color) {
        setAntialiasing(GUISettings.USE_ANTIALIASING);
        setCacheable(false);

        this.img = new BufferedImage(12, 12, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = this.img.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        g.fillRect(0, 0, 12, 12);
        //reset composite
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        g.setColor(color);
        g.fill(new Ellipse2D.Double(0, 0, 12, 12));
    }

    /**
     * Gets the current set of waypoints to paint
     * @return a typed Set of Waypoints
     */
    public Set<W> getWaypoints() {
        return Collections.unmodifiableSet(waypoints);
    }

    /**
     * Sets the current set of waypoints to paint
     * @param waypoints the new Set of Waypoints to use
     */
    public WayPointPainter<W> setWaypoints(Set<W> waypoints) {
        this.waypoints = waypoints;
        return this;
    }

    @Override
    protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height) {
        Rectangle viewportBounds = map.getViewportBounds();
        g.translate(-viewportBounds.getX(), -viewportBounds.getY());

        for (var wp : getWaypoints()) {
            Point2D point = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());

            int x = (int) Math.round(point.getX() - (img.getWidth() / 2.0));
            int y = (int) Math.round(point.getY() - (img.getHeight() / 2.0));

            g.drawImage(img, x, y, null);
        }

        g.translate(viewportBounds.getX(), viewportBounds.getY());
    }
}

