package gui.util;

import gui.mapviewer.LinePainter;
import gui.mapviewer.MoteWayPoint;
import iot.Environment;
import iot.networkentity.UserMote;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.Waypoint;
import util.MapHelper;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class for GUI related operations.
 */
public class GUIUtil {

    /**
     * Generate a list of painters which paint the borders, based on the max x and y coordinates possible.
     * @param environment The environment which is bounded by a maximum x and y coordinate.
     * @return A list of painters which paint the borders of the {@code environment}.
     */
    public static List<LinePainter> getBorderPainters(Environment environment) {
        List<LinePainter> painters = new ArrayList<>();
        MapHelper mapHelper = environment.getMapHelper();
        int maxX = environment.getMaxXpos();
        int maxY = environment.getMaxYpos();

        painters.add(new LinePainter(List.of(mapHelper.toGeoPosition(0, 0), mapHelper.toGeoPosition(0, maxY))));
        painters.add(new LinePainter(List.of(mapHelper.toGeoPosition(0, 0), mapHelper.toGeoPosition(maxX, 0))));
        painters.add(new LinePainter(List.of(mapHelper.toGeoPosition(maxX, 0), mapHelper.toGeoPosition(maxX, maxY))));
        painters.add(new LinePainter(List.of(mapHelper.toGeoPosition(0, maxY), mapHelper.toGeoPosition(maxX, maxY))));

        return painters;
    }

    /**
     * Retrieve a map of mote waypoints to their visualization Ids.
     * @param environment The environment storing all the motes.
     * @return A map of mote waypoints (containing the mote positions) to their respective visualization Id.
     */
    public static Map<MoteWayPoint, Integer> getMoteMap(Environment environment) {
        Map<MoteWayPoint, Integer> map = new HashMap<>();
        var motes = environment.getMotes();

        var wraps = motes.stream()
            .map(m -> {
                var pos = environment.getMapHelper().toGeoPosition(m.getPosInt());
                if (m instanceof UserMote) {
                    return new MoteWayPoint(pos, true, ((UserMote)m).isActive());
                }
                return new MoteWayPoint(pos);
            })
            .collect(Collectors.toList());

        IntStream.range(0, wraps.size())
            .forEach(i -> map.put(wraps.get(i), i+1));
        return map;
    }

    /**
     * Retrieve a map of gateway waypoints to their visualization Ids.
     * @param environment The environment storing all the gateways.
     * @return A map of gateway waypoints (containing the gateway positions) to their respective visualization Id.
     */
    public static Map<Waypoint, Integer> getGatewayMap(Environment environment) {
        Map<Waypoint, Integer> map = new HashMap<>();
        var gateways = environment.getGateways();

        IntStream.range(0, gateways.size())
            .forEach(i -> map.put(new DefaultWaypoint(environment.getMapHelper().toGeoPosition(gateways.get(i).getPosInt())), i+1));

        return map;
    }

    /**
     * Retrieve an output file based on a given input file, which might not have the given extension yet.
     * @param givenFile The file to be (possible) converted.
     * @param extension The extension which the output file should have.
     * @return An output file which has the required extension.
     */
    public static File getOutputFile(File givenFile, String extension) {
        String name = givenFile.getName();

        if (name.length() < extension.length() + 2 || !name.substring(name.length() - (extension.length()+1)).equals("." + extension)) {
            // Either the filename is too short, or it is still missing the (right) extension
            return new File(givenFile.getPath() + "." + extension);
        } else {
            return new File(givenFile.getPath());
        }
    }


    public static void updateTextFieldCoordinate(JTextField field, double value, String alt1, String alt2) {
        field.setText(coordinateToString(value, alt1, alt2));
    }


    public static void updateLabelCoordinateLat(JLabel label, double value) {
        updateLabelCoordinate(label, value, "E", "W");
    }

    public static void updateLabelCoordinateLon(JLabel label, double value) {
        updateLabelCoordinate(label, value, "N", "S");
    }

    private static void updateLabelCoordinate(JLabel label, double value, String alt1, String alt2) {
        label.setText(coordinateToString(value, alt1, alt2));
    }

    /**
     * Convert a geo coordinate value to a representing string.
     * @param value The coordinate value.
     * @param alt1 The default direction (e.g. N).
     * @param alt2 The alternative direction (e.g. S).
     * @return A String which includes the geo coordinate.
     */
    private static String coordinateToString(double value, String alt1, String alt2) {
        // TODO clean up magic numbers
        int degrees = (int) Math.floor(value);
        int minutes = (int) Math.floor((value - degrees) * 60);
        double seconds = (double) Math.round(((value - degrees) * 60 - minutes) * 60 * 1000d) / 1000d;

        return String.format("%s %d° %d' %.2f\"", value > 0 ? alt1 : alt2, degrees, minutes, seconds);
    }
}
