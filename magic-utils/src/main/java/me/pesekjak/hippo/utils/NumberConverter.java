package me.pesekjak.hippo.utils;

/**
 * Converter that converts numbers between different types.
 */
public final class NumberConverter {

    private NumberConverter() {
        throw new UnsupportedOperationException();
    }

    public static Number convertNumber(Object o, Class<?> clazz) {
        if (o == null) return null;
        if (!(o instanceof Number n)) return null;

        if (clazz == Byte.class) return n.byteValue();
        if (clazz == Short.class) return n.shortValue();
        if (clazz == Integer.class) return n.intValue();
        if (clazz == Long.class) return n.longValue();
        if (clazz == Float.class) return n.floatValue();
        if (clazz == Double.class) return n.doubleValue();

        return n;
    }

    public static byte convertByte(Object o) {
        if (o == null) return 0;
        if (!(o instanceof Number n)) return 0;
        return n.byteValue();
    }

    public static short convertShort(Object o) {
        if (o == null) return 0;
        if (!(o instanceof Number n)) return 0;
        return n.shortValue();
    }

    public static int convertInt(Object o) {
        if (o == null) return 0;
        if (!(o instanceof Number n)) return 0;
        return n.intValue();
    }

    public static long convertLong(Object o) {
        if (o == null) return 0;
        if (!(o instanceof Number n)) return 0;
        return n.longValue();
    }

    public static float convertFloat(Object o) {
        if (o == null) return 0;
        if (!(o instanceof Number n)) return 0;
        return n.floatValue();
    }

    public static double convertDouble(Object o) {
        if (o == null) return 0;
        if (!(o instanceof Number n)) return 0;
        return n.doubleValue();
    }

}
