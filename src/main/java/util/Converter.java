package util;

import org.jxmapviewer.viewer.GeoPosition;

import java.nio.ByteBuffer;

/**
 * Util class for common non basic conversion
 */
public class Converter {

    static public byte[] toRowType(Byte[] data) {
        var ret = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            ret[i] = data[i];
        }
        return ret;
    }

    static public Byte[] toObjectType(byte[] data) {
        var ret = new Byte[data.length];
        for (int i = 0; i < data.length; i++) {
            ret[i] = data[i];
        }
        return ret;
    }

    static public byte[] toByteArray(GeoPosition position) {
        byte[] data = new byte[8];
        ByteBuffer.wrap(data, 0, 4).putFloat((float)position.getLatitude());
        ByteBuffer.wrap(data, 4, 4).putFloat((float)position.getLongitude());
        return data;
    }

    static public GeoPosition toGeoPosition(byte[] data) {
        return toGeoPosition(data, 0);
    }

    static public GeoPosition toGeoPosition(byte[] data, int offset) {
        ByteBuffer buf = ByteBuffer.wrap(data);
        return new GeoPosition(buf.getFloat(offset), buf.getFloat(offset+Float.BYTES));
    }

    static public GeoPosition toFloatingGeoPosition(GeoPosition pos) {
        return new GeoPosition((float) pos.getLatitude(), (float) pos.getLongitude());
    }
}
