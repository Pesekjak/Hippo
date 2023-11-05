package me.pesekjak.hippo.utils;

import me.pesekjak.hippo.core.ASMUtil;
import me.pesekjak.hippo.core.loader.DynamicClassLoader;
import me.pesekjak.hippo.utils.parser.SuperSignatureParser;
import org.objectweb.asm.Type;
import org.skriptlang.skript.lang.script.Script;

import java.lang.reflect.Method;

/**
 * Descriptor for a method.
 * <p>
 * Can contain not yet compiled classes.
 * @param stringDescriptor descriptor (follows Skript format)
 */
public record MethodDescriptor(String stringDescriptor) {

    /**
     * Returns Method of this descriptor for given class.
     * @param script script
     * @param methodName name of the method
     * @param source source of the method
     * @return method
     */
    public Method get(Script script, String methodName, Class<?> source) throws NoSuchMethodException, ClassNotFoundException {
        String toParse = stringDescriptor.trim();
        if (toParse.isEmpty()) return source.getMethod(methodName);

        Type[] types;
        try {
            types = SuperSignatureParser.parse(toParse, script).toArray(new Type[0]);
        } catch (Exception exception) {
            throw new NoSuchMethodException();
        }

        Class<?>[] argumentsTypes = new Class[types.length];
        for (int i = 0; i < types.length; i++)
            argumentsTypes[i] = ASMUtil.getClassFromType(types[i], DynamicClassLoader.getInstance());

        return source.getMethod(methodName, argumentsTypes);
    }

}
