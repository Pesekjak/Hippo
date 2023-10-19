package me.pesekjak.hippo.utils;

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

public class ReflectionUtilTest {

    @Test
    public void methodLookup() throws Throwable {
        TestClass instance = new TestClass();

        MethodHandle blob1 = ReflectionUtil.unreflectSpecial(
                TestClass.class,
                ReflectionUtil.searchMethodFromArguments(TestClass.class, "blob", 0, 0),
                TestClass.class,
                true
        );
        assert blob1 != null;
        assert (Integer) ReflectionUtil.convertArgsAndInvokeMethod(blob1, instance, 0L, 0d) == 1;

        MethodHandle blob2 = ReflectionUtil.unreflectSpecial(
                TestClass.class,
                ReflectionUtil.searchMethodFromArguments(TestClass.class, "blob", "foo", 0),
                TestClass.class,
                true
        );
        assert blob2 != null;
        assert (Integer) ReflectionUtil.convertArgsAndInvokeMethod(blob2, instance, "foo", 0d) == 2;

        // string of length one should get converted to char
        MethodHandle blob3 = ReflectionUtil.unreflectSpecial(
                TestClass.class,
                ReflectionUtil.searchMethodFromArguments(TestClass.class, "blob", "f"),
                TestClass.class,
                true
        );
        assert blob3 != null;
        assert (Integer) ReflectionUtil.convertArgsAndInvokeMethod(blob3, instance, "f") == 3;

        Method none = ReflectionUtil.searchMethodFromArguments(TestClass.class, "blob", "Hello World!");
        assert none == null;

        MethodHandle varargs = ReflectionUtil.unreflectSpecial(
                TestClass.class,
                ReflectionUtil.searchMethodFromArguments(TestClass.class, "varargs", "foo", 1, 2, 3),
                TestClass.class,
                false
        );
        assert varargs != null;
        assert (Boolean) ReflectionUtil.convertArgsAndInvokeMethod(varargs, instance, "foo", 1, 2, 3);

    }

    static class TestClass {

        public int blob(int one, double two) {
            return 1;
        }

        public int blob(String one, double two) {
            return 2;
        }

        public int blob(char one) {
            return 3;
        }

        public boolean varargs(String one, int... something) {
            return true;
        }

    }

}
