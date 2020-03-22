package gui.mapviewer;

import EnvironmentAPI.GeneralSources.Source;
import EnvironmentAPI.Sensor.Sensor;
import EnvironmentAPI.SensorEnvironment;
import gui.util.GUISettings;
import iot.Environment;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.AbstractPainter;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

public class SensorPainter extends AbstractPainter<JXMapViewer> {
    private SensorEnvironment pollutionEnv;
    private Environment environment;


    public SensorPainter(Environment environment, SensorEnvironment pollutionEnv) {
        this.setAntialiasing(GUISettings.USE_ANTIALIASING);
        this.setCacheable(true);

        this.pollutionEnv = pollutionEnv;
        this.environment = environment;
    }

    @Override
    protected void doPaint(Graphics2D g, JXMapViewer jxMapViewer, int i, int i1) {
        g = (Graphics2D) g.create();
        Rectangle rect = jxMapViewer.getViewportBounds();
        g.translate(-rect.x, -rect.y);
        List<Sensor> sensorList = pollutionEnv.getSensors();
        for(Sensor sensor : sensorList){
            float airQuality = (float) (sensor.generateData()/pollutionEnv.getMaxOfSensors());
            g.setColor(getColor(airQuality));

            Point2D point = jxMapViewer.getTileFactory().geoToPixel(sensor.getPosition(), jxMapViewer.getZoom());

            int x = (int) (point.getX()-5);
            int y = (int) (point.getY()-5);

            g.fillOval(x,y,10,10);
            g.setColor(Color.black);
            g.drawOval(x-1,y-1,11,11);

        }

        g.translate(rect.getX(), rect.getY());
     }

    private Color getColor(float airQuality) {
        //return new Color(105,105,105, (int)(255*airQuality));
        float[] hsbVals = Color.RGBtoHSB((int) (255 * (airQuality)), (int) (255 * (1-airQuality)), 0, null);
        return Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2]);
    }
}
