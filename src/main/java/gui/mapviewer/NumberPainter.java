package gui.mapviewer;

import gui.util.GUISettings;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.AbstractPainter;
import org.jxmapviewer.viewer.Waypoint;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class NumberPainter<W extends Waypoint> extends AbstractPainter<JXMapViewer> {
    private Map<W,Integer> waypoints = new HashMap<>();
    private Type type;


    public NumberPainter(Type type) {
        this.type = type;

        setAntialiasing(GUISettings.USE_ANTIALIASING);
        setCacheable(false);
    }


    /**
     * Gets the current set of waypoints to paint
     * @return a typed Set of Waypoints
     */
    public Set<W> getWaypoints() {
        return Collections.unmodifiableSet(waypoints.keySet());
    }

    /**
     * Sets the current set of waypoints to paint
     * @param waypoints the new Set of Waypoints to use
     */
    public NumberPainter<W> setWaypoints(Map<? extends W, Integer> waypoints) {
        this.waypoints.clear();
        this.waypoints.putAll(waypoints);

        return this;
    }

    @Override
    protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height) {
        Rectangle viewportBounds = map.getViewportBounds();
        g.translate(-viewportBounds.getX(), -viewportBounds.getY());

        for (var wp : getWaypoints()) {
            Point2D point = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());

            int x = (int) point.getX() + this.type.xOffset;
            int y = (int) point.getY() + this.type.yOffset;

            g.drawImage(generateNumberGraphics(this.waypoints.get(wp)), x, y, null);
        }

        g.translate(viewportBounds.getX(), viewportBounds.getY());
    }


    private BufferedImage generateNumberGraphics(int number) {
        /*
           Because font metrics is based on a graphics context, we need to create
           a small, temporary image so we can ascertain the width and height
           of the final image
         */
        String numberString = Integer.toString(number);
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = img.createGraphics();
        Font font = new Font("Arial", this.type.fontType, this.type.fontSize);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(numberString);
        int height = fm.getHeight();
        g2d.dispose();

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        if (GUISettings.USE_ANTIALIASING) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.BLACK);
        g2d.drawString(numberString, 0, fm.getAscent());
        g2d.dispose();

        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale(0.35, 0.35);
        AffineTransformOp scaleOp =
            new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        img = scaleOp.filter(img, after);

        return img;
    }


    public enum Type {
        MOTE(20, -20, 48, Font.PLAIN),
        GATEWAY(15, -30, 48, Font.PLAIN),
        WAYPOINT(10, -10, 44, Font.PLAIN);

        public int xOffset;
        public int yOffset;
        public int fontSize;
        public int fontType;

        Type(int xOffset, int yOffset, int fontSize, int fontType) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.fontSize = fontSize;
            this.fontType = fontType;
        }
    }
}

