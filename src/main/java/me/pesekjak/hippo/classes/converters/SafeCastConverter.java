package me.pesekjak.hippo.classes.converters;

public class SafeCastConverter {

    private SafeCastConverter() {
        throw new UnsupportedOperationException();
    }

    public static Object safeCast(Object o, Class<?> c){
        if(o == null) return null;
        return (c.isAssignableFrom(o.getClass())) ? o : null;
    }

}
