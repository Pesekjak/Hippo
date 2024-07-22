package me.pesekjak.hippo.core.skript;

import ch.njol.skript.lang.Trigger;
import me.pesekjak.hippo.core.*;
import me.pesekjak.hippo.utils.ConverterVisitor;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * Wrapper for a method.
 * <p>
 * Serves as a bridge between user Skript code and compiled class.
 *
 * @param method wrapped method
 * @param arguments named parameters of the method
 * @param trigger trigger of the method
 */
// Named parameters are used because Method object does not store info about parameters names, and
// they are later used to set variables for the method code execution.
public record MethodWrapper(Method method, List<NamedParameter> arguments, @Nullable Trigger trigger) implements ClassContentSkriptWrapper {

    @Override
    public ClassContent content() {
        return method;
    }

    /**
     * Injects code to the method writer of the method.
     */
    public void injectCode() {
        assert method.getSource() != null;
        String className = method.getSource().getName();

        if (trigger == null) return;

        method.setWriter((method, methodVisitor) -> {
            int size = method.getSize();
            List<Parameter> parameters = method.getParameters();

            ASMUtil.pushConstant(methodVisitor, parameters.size());
            methodVisitor.visitTypeInsn(ANEWARRAY, Type.getType(Object.class).getInternalName());
            methodVisitor.visitVarInsn(ASTORE, size);

            int arrayCursor = 0;
            int sizeCursor = method.isStatic() ? 0 : 1; // static methods do not have the first slot taken by instance
            for (Parameter parameter : parameters) {
                methodVisitor.visitVarInsn(ALOAD, size);

                ASMUtil.pushConstant(methodVisitor, arrayCursor);
                arrayCursor++;

                Type paramType = parameter.getType();
                methodVisitor.visitVarInsn(paramType.getOpcode(ILOAD), sizeCursor);
                sizeCursor += paramType.getSize();

                if (ASMUtil.isPrimitive(paramType) && !ASMUtil.isArray(paramType))
                    ConverterVisitor.convertTopPrimitiveToObject(methodVisitor, paramType);

                methodVisitor.visitInsn(AASTORE);
            }

            methodVisitor.visitLdcInsn(className);
            methodVisitor.visitLdcInsn(method.getName());
            methodVisitor.visitLdcInsn(method.getDescriptor());
            if (!method.isStatic())
                methodVisitor.visitVarInsn(ALOAD, 0);
            else
                methodVisitor.visitInsn(ACONST_NULL);
            methodVisitor.visitVarInsn(ALOAD, size);

            methodVisitor.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getType(Storage.class).getInternalName(),
                    "runMethod",
                    Type.getMethodDescriptor(
                            Type.getType(Object.class),
                            Type.getType(String.class),
                            Type.getType(String.class),
                            Type.getType(String.class),
                            Type.getType(Object.class),
                            Type.getType(Object[].class)
                    ),
                    false
            );

            if (method.getReturnType().getType().getDescriptor().equals(Type.VOID_TYPE.getDescriptor())) return;

            ConverterVisitor.convertTopObject(methodVisitor, method.getReturnType().getType());
        });
    }

}
