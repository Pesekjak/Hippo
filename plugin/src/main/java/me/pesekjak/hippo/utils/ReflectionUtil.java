package me.pesekjak.hippo.utils;

import me.pesekjak.hippo.core.loader.DynamicClassLoader;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Util for easier use of MethodHandle API.
 * <p>
 * Used for calling methods of the super classes.
 */
public final class ReflectionUtil {

    private static final MethodHandles.Lookup LOOKUP;

    static {
        Class<?> clazz;
        try {
            clazz = DynamicClassLoader.getInstance().loadClass(MethodHandles.class.getName());
        } catch (Exception exception) {
            clazz = MethodHandles.class; // for testing environment
        }
        try {
            LOOKUP = (MethodHandles.Lookup) clazz.getMethod("lookup").invoke(null);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private ReflectionUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts the arguments using conversion from {@link JavaUtil} and invokes the method.
     *
     * @param handle method handle to invoke
     * @param instance instance of the object
     * @param arguments arguments to invoke the method with
     * @return result of the method
     * @param <T> type of the result
     */
    public static <T> T convertArgsAndInvokeMethod(MethodHandle handle, Object instance, Object... arguments) throws Throwable {
        Object[] converted = new Object[handle.type().parameterCount() - 1];
        if (converted.length > arguments.length) throw new IllegalArgumentException();

        for (int i = 0; i < converted.length; i++) {
            Class<?> targetClass = handle.type().parameterType(i + 1);

            // handling varargs if handle is varargs collector
            if (handle.isVarargsCollector() && i == converted.length - 1) {
                targetClass = targetClass.getComponentType();
                int length = arguments.length - converted.length + 1;

                Object[] old = converted;
                converted = new Object[old.length + length - 1];
                System.arraycopy(old, 0, converted, 0, old.length - 1);

                for (int j = 0; j < length; j++) {
                    Object next = arguments[i + j];
                    if (!JavaUtil.canConvert(next, targetClass)) throw new IllegalArgumentException();
                    converted[i + j] = JavaUtil.convert(next, targetClass);
                }

                break;
            }

            if (!JavaUtil.canConvert(arguments[i], targetClass)) throw new IllegalArgumentException();
            converted[i] = JavaUtil.convert(arguments[i], targetClass);
        }

        return invokeMethod(handle, instance, converted);
    }

    /**
     * Invokes the provided method handle with given arguments.
     *
     * @param handle method handle to invoke
     * @param instance instance of the object
     * @param arguments arguments to invoke the method with
     * @return result of the method
     * @param <T> type of the result
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(MethodHandle handle, Object instance, Object... arguments) throws Throwable {
        Object[] merged = new Object[arguments.length + 1];
        merged[0] = instance;
        System.arraycopy(arguments, 0, merged, 1, arguments.length);
        Object result = handle.invokeWithArguments(merged);
        return (T) result;
    }

    /**
     * Searches for special method handle.
     *
     * @param source source of the method
     * @param name name of the method
     * @param returnType return type of the method
     * @param argumentTypes argument types of the method
     * @param specialCaller special caller
     * @return method handle
     */
    public static MethodHandle searchSpecialHandle(Class<?> source,
                                                             String name,
                                                             Class<?> returnType,
                                                             Class<?>[] argumentTypes,
                                                             Class<?> specialCaller) throws Throwable {
        return MethodHandles.privateLookupIn(source, LOOKUP).findSpecial(
                source,
                name,
                MethodType.methodType(returnType, argumentTypes),
                specialCaller
        );
    }

    /**
     * Creates special method handle from given Method instance.
     *
     * @param source source of the method
     * @param method method
     * @param specialCaller special caller
     * @param disableVarArgs whether the varargs should be disabled if the method is varargs
     * @return method handle
     */
    public static MethodHandle unreflectSpecial(Class<?> source,
                                                Method method,
                                                Class<?> specialCaller,
                                                boolean disableVarArgs) throws Throwable {
        MethodHandle handle = MethodHandles.privateLookupIn(source, LOOKUP).unreflectSpecial(
                method,
                specialCaller
        );

        if (method.isVarArgs()) {
            if (disableVarArgs) handle = handle.withVarargs(false);
            else handle = handle.withVarargs(true);
        }
        return handle;
    }

    /**
     * Searches for method using only name and provided arguments to invoke the method with.
     * @param source source of the method
     * @param name name of the method
     * @param arguments arguments the method will be invoked with
     * @return method or null if no method is found
     */
    public static @Nullable Method searchMethodFromArguments(Class<?> source,
                                                             String name,
                                                             Object... arguments) {
        Set<Method> methods = new LinkedHashSet<>();
        methods.addAll(List.of(source.getMethods()));
        methods.addAll(List.of(source.getDeclaredMethods()));
        return searchExecutableFromArguments(methods, name, arguments);
    }

    /**
     * Searches for executable using only name and provided arguments to invoke the executable with.
     * @param table set of executables to search from
     * @param name name of the executable
     * @param arguments arguments the executable will be invoked with
     * @param <T> executable type
     * @return executable or null if no executable is found
     */
    @SuppressWarnings("unchecked")
    private static @Nullable <T extends Executable> T searchExecutableFromArguments(Set<T> table,
                                                                                   String name,
                                                                                   Object... arguments) {
        List<Executable> executables = (List<Executable>) table.stream()
                .filter(executable -> {
                    if (executable instanceof Method)
                        return executable.getName().equals(name);
                    return true; // constructors do not have names
                })
                .distinct()
                .toList();

        Executable found = null;

        executableSearch: for (Executable executable : executables) {
            if (!executable.isVarArgs() && executable.getParameters().length != arguments.length) continue;

            Object[] fixedArguments = new Object[executable.getParameters().length];

            if (executable.isVarArgs()) {
                System.arraycopy(arguments, 0, fixedArguments, 0, fixedArguments.length - 1);

                Object[] varArgs = new Object[arguments.length - fixedArguments.length + 1];
                System.arraycopy(arguments, fixedArguments.length - 1, varArgs, 0, varArgs.length);

                Class<?> closest = JavaUtil.getClosestSuperClass(
                        Arrays.stream(varArgs)
                                .filter(Objects::nonNull)
                                .map(Object::getClass)
                                .toArray(Class[]::new)
                );

                Object varArgsArray = Array.newInstance(closest, varArgs.length);
                for (int i = 0; i < varArgs.length; i++)
                    Array.set(varArgsArray, i, varArgs[i]);

                fixedArguments[fixedArguments.length - 1] = varArgsArray;
            } else {
                System.arraycopy(arguments, 0, fixedArguments, 0, arguments.length);
            }

            for (int i = 0; i < fixedArguments.length; i++) {
                if (fixedArguments[i] == null) continue;
                if (!JavaUtil.canConvert(fixedArguments[i], executable.getParameters()[i].getType())) continue executableSearch;
            }

            found = executable;
            break;
        }

        return (T) found;
    }

}
