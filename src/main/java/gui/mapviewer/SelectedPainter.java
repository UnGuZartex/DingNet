package gui.mapviewer;

import gui.util.GUISettings;
import iot.Environment;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.AbstractPainter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;

public class SelectedPainter  extends AbstractPainter<JXMapViewer> {
    private GeoPosition geo;
    private Environment environment;


    public SelectedPainter(Environment environment, String text) {
        this.setAntialiasing(GUISettings.USE_ANTIALIASING);
        this.setCacheable(true);
        this.geo = ToGeoPos(text);
        this.environment = environment;
    }

    private GeoPosition ToGeoPos(String position) {
        position = position.replace("[","");
        position = position.replace("]","");
        String[] longlat = position.split(",");
        return new GeoPosition(Double.parseDouble(longlat[0]), Double.parseDouble(longlat[1]));
    }

    @Override
    protected void doPaint(Graphics2D g, JXMapViewer jxMapViewer, int i, int i1) {
        g = (Graphics2D) g.create();
        Rectangle rect = jxMapViewer.getViewportBounds();
        g.translate(-rect.x, -rect.y);
        g.setColor(Color.WHITE);

        Point2D point = jxMapViewer.getTileFactory().geoToPixel(geo, jxMapViewer.getZoom());
        int x = (int) (point.getX()-7);
        int y = (int) (point.getY()-7);

        g.fillOval(x,y,14,14);
        g.setColor(Color.black);
        g.drawOval(x-2,y-2,16,16);
        g.translate(rect.getX(), rect.getY());


    }


    private Color getColor(float airQuality) {
        //return new Color(105,105,105, (int)(255*airQuality));
        float[] hsbVals = Color.RGBtoHSB((int) (255 * (airQuality)), (int) (255 * (1-airQuality)), 0, null);
        return Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2]);
    }
}
