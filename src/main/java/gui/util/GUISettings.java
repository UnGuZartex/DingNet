package gui.util;

import java.awt.*;

/**
 * Class used to store GUI specific settings
 */
public class GUISettings {
    // TODO: provide a configuration file specifically for the GUI in the future

    public static final int THREADPOOLSIZE = 8;
    public static final int POLLUTION_GRID_SQUARES = 50;
    public static final int BASE_VISUALIZATION_SPEED = 1;

    public static final boolean USE_ANTIALIASING = true;
    public static final boolean USE_MAP_CACHING = true;
    public static final boolean START_FULL_SCREEN = true;

    public static final float TRANSPARENCY_POLLUTIONGRID = .3f;
    public static final Color DEFAULT_WAYPOINT_COLOR = new Color(102,0,153);

    public static final Color CONNECTION_LINE_COLOR = Color.RED;
    public static final int CONNECTION_LINE_SIZE = 2;

    public static final Color ROUTING_PATH_LINE_COLOR = Color.CYAN;
    public static final int ROUTING_PATH_LINE_SIZE = 1;

    public static final Color MOTE_PATH_LINE_COLOR = Color.RED;
    public static final int MOTE_PATH_LINE_SIZE = 1;

    public static final String PATH_MOTE_IMAGE = "/images/Mote.png";
    public static final String PATH_USERMOTE_ACTIVE_IMAGE = "/images/cyclist.png";
    public static final String PATH_USERMOTE_INACTIVE_IMAGE = "/images/bicycle.png";
    public static final String PATH_GATEWAY_IMAGE = "/images/Gateway.png";

    public static final String PATH_CIRCLE_SELECTED = "/images/Circle_selected.png";
    public static final String PATH_CIRCLE_UNSELECTED = "/images/Circle_unselected.png";
    public static final String PATH_EDIT_ICON = "/images/Edit_icon.png";

    public static final String PATH_CACHE_TILEFACTORY = System.getProperty("user.dir") + "/.cache";

    public static final int SAMPLERATE = 500;
}
