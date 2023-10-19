package me.pesekjak.hippo.utils;

/**
 * Converter that boxes primitives to complex types.
 */
public final class AutoBoxer {

    private AutoBoxer() {
        throw new UnsupportedOperationException();
    }

    public static Boolean box(boolean o) {
        return o;
    }

    public static Character box(char o) {
        return o;
    }

    public static Byte box(byte o) {
        return o;
    }

    public static Short box(short o) {
        return o;
    }

    public static Integer box(int o) {
        return o;
    }

    public static Long box(long o) {
        return o;
    }

    public static Float box(float o) {
        return o;
    }

    public static Double box(double o) {
        return o;
    }

}
