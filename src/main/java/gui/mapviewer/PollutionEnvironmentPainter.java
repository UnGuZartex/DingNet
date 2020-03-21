package gui.mapviewer;

import EnvironmentAPI.SensorEnvironment;
import EnvironmentAPI.util.EnvSettings;
import gui.util.GUISettings;
import iot.Environment;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.AbstractPainter;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;
import util.MapHelper;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class PollutionEnvironmentPainter extends AbstractPainter<JXMapViewer> {
    private SensorEnvironment pollutionEnv;
    private Environment environment;
    private int counter = 0;


    public PollutionEnvironmentPainter(Environment environment, SensorEnvironment pollutionEnv) {
        this.setAntialiasing(GUISettings.USE_ANTIALIASING);
        this.setCacheable(true);

        this.pollutionEnv = pollutionEnv;
        this.environment = environment;
    }

    @Override
    protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height) {
        g = (Graphics2D) g.create();


        // convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        if (GUISettings.USE_ANTIALIASING) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        int maxX = environment.getMaxXpos() + 1;
        int maxY = environment.getMaxYpos() + 1;

        // Can decide to be more fine grained later on
        final int DIVISION = GUISettings.POLLUTION_GRID_SQUARES;
        TileFactory factory = map.getTileFactory();
        MapHelper mapHelper = environment.getMapHelper();

        for (int i = 0; i < DIVISION; i++) {
            for (int j = 0; j < DIVISION; j++) {
                // The starting position of the square is specified by the point in the upper left corner
                Point2D topLeft = factory.geoToPixel(mapHelper.toGeoPosition(
                    Math.round(i * ((float) maxX / DIVISION)),
                    Math.round((j+1) * ((float) maxY / DIVISION))
                ), map.getZoom());
                Point2D bottomRight = factory.geoToPixel(mapHelper.toGeoPosition(
                    Math.round((i+1) * ((float) maxX / DIVISION)),
                    Math.round(j * ((float) maxY / DIVISION))
                ), map.getZoom());
                GeoPosition middle = mapHelper.toGeoPosition(
                    (int) ((i+.5) * maxX / DIVISION),
                    (int) ((j+.5) * maxY / DIVISION));


                float airQuality = (float) pollutionEnv.getPoll().getDensity(middle, environment,EnvSettings.MAX_POLLUTION_SOURCES_VALUE)/EnvSettings.MAX_POLLUTION_SOURCES_VALUE;
                g.setColor(this.getColor(airQuality));
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, GUISettings.TRANSPARENCY_POLLUTIONGRID));
                g.fill(new Rectangle2D.Double(topLeft.getX(), topLeft.getY(),
                    Math.abs(topLeft.getX() - bottomRight.getX()), Math.abs(topLeft.getY() - bottomRight.getY())));
            }
        }

        g.dispose();
    }


    /**
     * Get a color according to the given airQuality
     * @param airQuality The air quality in question (1 being good, 0 being bad).
     * @return A color between green and red representing the air quality.
     */
    private Color getColor(float airQuality) {
        //return new Color(105,105,105, (int)(255*airQuality));
        float[] hsbVals = Color.RGBtoHSB((int) (255 * (airQuality)), (int) (255 * (1-airQuality)), 0, null);
        return Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2]);
    }
}
