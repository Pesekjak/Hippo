package me.pesekjak.hippo.classes.converters;

public class NonPrimitiveConverter {

    private NonPrimitiveConverter() {
        throw new UnsupportedOperationException();
    }

    public static Boolean fromPrimitive(boolean o) {
        return o;
    }

    public static Character fromPrimitive(char o) {
        return o;
    }

    public static Byte fromPrimitive(byte o) {
        return o;
    }

    public static Short fromPrimitive(short o) {
        return o;
    }

    public static Integer fromPrimitive(int o) {
        return o;
    }

    public static Long fromPrimitive(long o) {
        return o;
    }

    public static Float fromPrimitive(float o) {
        return o;
    }

    public static Double fromPrimitive(double o) {
        return o;
    }

}
