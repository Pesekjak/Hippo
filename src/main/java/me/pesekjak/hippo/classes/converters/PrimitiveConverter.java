package me.pesekjak.hippo.classes.converters;

public class PrimitiveConverter {

    private PrimitiveConverter() {
        throw new UnsupportedOperationException();
    }

    public static boolean fromObjectBOOLEAN(Object o) {
        return (o instanceof Boolean b) ? b : false;
    }

    public static char fromObjectCHARACTER(Object o) {
        return (o instanceof Character c) ? c : (char) 0;
    }

    public static byte fromObjectBYTE(Object o) {
        return (o instanceof Byte b) ? b : 0;
    }

    public static short SFromObject(Object o) {
        return (o instanceof Short s) ? s : 0;
    }

    public static int fromObjectINT(Object o) {
        return (o instanceof Integer i) ? i : 0;
    }

    public static long fromObjectLONG(Object o) {
        return (o instanceof Long l) ? l : 0;
    }

    public static float fromObjectFLOAT(Object o) {
        return (o instanceof Float f) ? f : 0;
    }

    public static double fromObjectDOUBLE(Object o) {
        return (o instanceof Double d) ? d : 0;
    }

}
