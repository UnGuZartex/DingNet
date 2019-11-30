package gui.util;

import gui.MainGUI;
import iot.Environment;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.event.MouseListener;
import java.io.File;

/**
 * An abstract class used as a base class for configuration related GUI panels
 */
public abstract class AbstractConfigurePanel implements Refreshable {
    protected MainGUI mainGUI;
    protected Environment environment;

    protected JXMapViewer mapViewer = new JXMapViewer();
    protected TileFactoryInfo info = new OSMTileFactoryInfo();
    protected DefaultTileFactory tileFactory = new DefaultTileFactory(info);


    public AbstractConfigurePanel(MainGUI mainGUI) {
        this(mainGUI, 6);
    }

    public AbstractConfigurePanel(MainGUI mainGUI, int zoom) {
        this.mainGUI = mainGUI;
        this.environment = mainGUI.getEnvironment();

        if (GUISettings.USE_MAP_CACHING) {
            File cache = new File(GUISettings.PATH_CACHE_TILEFACTORY);
            tileFactory.setLocalCache(new FileBasedLocalCache(cache, false));
        }

        for (MouseListener ml : mapViewer.getMouseListeners()) {
            mapViewer.removeMouseListener(ml);
        }
        mapViewer.setZoom(zoom);

        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

        mapViewer.setTileFactory(tileFactory);
        tileFactory.setThreadPoolSize(GUISettings.THREADPOOLSIZE);
    }

    public void refresh() {
        loadMap(true);
        mainGUI.refresh();
    }

    protected abstract void loadMap(boolean refresh);

    public abstract JPanel getMainPanel();
}
