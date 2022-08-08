package me.pesekjak.hippo.classes.converters;

import com.btk5h.skriptmirror.Null;
import com.btk5h.skriptmirror.ObjectWrapper;

public class ReflectConverter {

    private ReflectConverter() {
        throw new UnsupportedOperationException();
    }

    protected static Object unwrapIfNecessary(Object o) {
        return ObjectWrapper.unwrapIfNecessary(o);
    }

    protected static Object nullIfNecessary(Object o) {
        return o instanceof Null ? null : o;
    }

    public static Object handle(Object o) {
        return nullIfNecessary(unwrapIfNecessary(o));
    }

}
