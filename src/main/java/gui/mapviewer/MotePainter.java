package gui.mapviewer;

import gui.util.GUISettings;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.AbstractPainter;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Paints waypoints on the JXMapViewer. This is an
 * instance of Painter that only can draw on to JXMapViewers.
 * @param <W> the waypoint type
 * @author rbair
 */
public class MotePainter<W extends MoteWayPoint> extends AbstractPainter<JXMapViewer> {
    private Set<W> waypoints = new HashSet<>();

    public MotePainter() {
        setAntialiasing(GUISettings.USE_ANTIALIASING);
        setCacheable(false);
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
    public MotePainter<W> setWaypoints(Set<? extends W> waypoints) {
        this.waypoints.clear();
        this.waypoints.addAll(waypoints);

        return this;
    }

    @Override
    protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height) {
        Rectangle viewportBounds = map.getViewportBounds();
        g.translate(-viewportBounds.getX(), -viewportBounds.getY());

        for (var wp : getWaypoints()) {
            Point2D point = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());

            int x = (int) (point.getX() - (wp.getIcon().getWidth() * 0.1));
            int y = (int) (point.getY() - (wp.getIcon().getHeight() * 0.1));

            g.drawImage(wp.getIcon(), x, y, null);
        }

        g.translate(viewportBounds.getX(), viewportBounds.getY());
    }

}
