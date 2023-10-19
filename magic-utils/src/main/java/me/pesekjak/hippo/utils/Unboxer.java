package me.pesekjak.hippo.utils;

/**
 * Converter that unboxes complex types to primitives.
 */
public final class Unboxer {

    private Unboxer() {
        throw new UnsupportedOperationException();
    }

    public static boolean asBoolean(Object o) {
        return (o instanceof Boolean b) ? b : false;
    }

    public static char asChar(Object o) {
        return (o instanceof Character c) ? c : (char) 0;
    }

    public static byte asByte(Object o) {
        return (o instanceof Byte b) ? b : 0;
    }

    public static short asShort(Object o) {
        return (o instanceof Short s) ? s : 0;
    }

    public static int asInt(Object o) {
        return (o instanceof Integer i) ? i : 0;
    }

    public static long asLong(Object o) {
        return (o instanceof Long l) ? l : 0;
    }

    public static float asFloat(Object o) {
        return (o instanceof Float f) ? f : 0;
    }

    public static double asDouble(Object o) {
        return (o instanceof Double d) ? d : 0;
    }

}
