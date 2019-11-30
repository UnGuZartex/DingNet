package gui.mapviewer;

import gui.util.ImageLoader;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;

import java.awt.image.BufferedImage;

public class MoteWayPoint implements Waypoint {
    private final GeoPosition position;
    private BufferedImage icon;

    public MoteWayPoint(GeoPosition position) {
        this(position, false);
    }

    private MoteWayPoint(GeoPosition position, boolean isUserMote) {
        this(position, isUserMote, false);
    }

    public MoteWayPoint(GeoPosition position, boolean isUserMote, boolean isActive) {
        this.position = position;
        this.icon = !isUserMote ? ImageLoader.IMAGE_MOTE :
                    isActive ? ImageLoader.IMAGE_USERMOTE_ACTIVE : ImageLoader.IMAGE_USERMOTE_INACTIVE;
    }

    BufferedImage getIcon() {
        return icon;
    }

    @Override
    public GeoPosition getPosition() {
        return position;
    }
}
