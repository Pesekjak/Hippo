package me.pesekjak.hippo.classes.converters;

import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.classes.Type;

public class NumberConverter {

    private NumberConverter() {
        throw new UnsupportedOperationException();
    }

    public static Number convertNumber(Object o, Type type) {
        if(o == null) return null;
        if(!(o instanceof Number n)) return null;
        Class<?> numberClass = type.findClass();
        Primitive primitive = Primitive.fromClass(numberClass);
        if(primitive == null) return n;
        return switch (primitive) {
            case BYTE -> n.byteValue();
            case SHORT -> n.shortValue();
            case INT -> n.intValue();
            case LONG -> n.longValue();
            case FLOAT -> n.floatValue();
            case DOUBLE -> n.doubleValue();
            default -> n;
        };
    }

    public static byte convertBYTE(Object o) {
        if(o == null) return 0;
        if(!(o instanceof Number n)) return 0;
        return n.byteValue();
    }

    public static short convertSHORT(Object o) {
        if(o == null) return 0;
        if(!(o instanceof Number n)) return 0;
        return n.shortValue();
    }

    public static int convertINT(Object o) {
        if(o == null) return 0;
        if(!(o instanceof Number n)) return 0;
        return n.intValue();
    }

    public static long convertLONG(Object o) {
        if(o == null) return 0;
        if(!(o instanceof Number n)) return 0;
        return n.longValue();
    }

    public static float convertFLOAT(Object o) {
        if(o == null) return 0;
        if(!(o instanceof Number n)) return 0;
        return n.floatValue();
    }

    public static double convertDOUBLE(Object o) {
        if(o == null) return 0;
        if(!(o instanceof Number n)) return 0;
        return n.doubleValue();
    }

    public static boolean isNumber(Type type) {
        if(type.isArray()) return false;
        Primitive primitive = Primitive.fromClass(type.findClass());
        return primitive != null && Number.class.isAssignableFrom(primitive.getNonPrimitiveClass());
    }

}
