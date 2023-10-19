package me.pesekjak.hippo.core.skript;

import ch.njol.skript.lang.Trigger;
import me.pesekjak.hippo.core.*;
import me.pesekjak.hippo.elements.effects.EffSuperConstructorCall;
import me.pesekjak.hippo.utils.ConverterVisitor;
import org.objectweb.asm.Type;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * Wrapper for a constructor.
 * <p>
 * Serves as a bridge between user Skript code and compiled class.
 *
 * @param constructor wrapped constructor
 * @param arguments named parameters of the constructor
 * @param trigger trigger of the constructor
 * @param superCall effect used for calling the super constructor
 * @param argumentOffset offset of the arguments
 */
// Argument offset is used for Enum constructors, where first two constructor arguments are
// internally used for enum name and ordinal value, the offset is used when injecting the code
// to skip these two arguments running the code using Storage class.
public record ConstructorWrapper(Constructor constructor,
                                 List<NamedParameter> arguments,
                                 Trigger trigger,
                                 EffSuperConstructorCall superCall,
                                 int argumentOffset) implements ClassContentSkriptWrapper {

    @Override
    public ClassContent content() {
        return constructor;
    }

    /**
     * Injects code to the method writer and super writer of the constructor.
     */
    public void injectCode() {
        assert constructor.getSource() != null;
        String className = constructor.getSource().getName();

        constructor.setSuperWriter((constructor, methodVisitor) -> {
            int size = constructor.getSize();

            ASMUtil.pushConstant(methodVisitor, arguments.size());
            methodVisitor.visitTypeInsn(ANEWARRAY, Type.getType(Object.class).getInternalName());
            methodVisitor.visitVarInsn(ASTORE, size);

            int arrayCursor = 0;
            int sizeCursor = 1  + argumentOffset;
            for (NamedParameter parameter : arguments) {
                methodVisitor.visitVarInsn(ALOAD, size);

                ASMUtil.pushConstant(methodVisitor, arrayCursor);
                arrayCursor++;

                Type paramType = parameter.parameter().getType();
                methodVisitor.visitVarInsn(paramType.getOpcode(ILOAD), sizeCursor);
                sizeCursor += paramType.getSize();

                if (ASMUtil.isPrimitive(paramType) && !ASMUtil.isArray(paramType))
                    ConverterVisitor.convertTopPrimitiveToObject(methodVisitor, paramType);

                methodVisitor.visitInsn(AASTORE);
            }

            int superArgsSize = superCall.getTypes().size();

            methodVisitor.visitLdcInsn(className);
            methodVisitor.visitLdcInsn(constructor.getDescriptor());
            ASMUtil.pushConstant(methodVisitor, superArgsSize);
            methodVisitor.visitVarInsn(ALOAD, size);

            methodVisitor.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getType(Storage.class).getInternalName(),
                    "runConstructorSuper",
                    Type.getMethodDescriptor(
                            Type.getType(Object[].class),
                            Type.getType(String.class),
                            Type.getType(String.class),
                            Type.INT_TYPE,
                            Type.getType(Object[].class)
                    ),
                    false
            );
            methodVisitor.visitVarInsn(ASTORE, size);

            methodVisitor.visitVarInsn(ALOAD, 0);

            for (int i = 0; i < superArgsSize; i++) {
                Type type = superCall.getTypes().get(i);
                methodVisitor.visitVarInsn(ALOAD, size);
                ASMUtil.pushConstant(methodVisitor, i);
                methodVisitor.visitInsn(AALOAD);
                ConverterVisitor.convertTopObject(methodVisitor, type);
            }

            assert constructor.getSource() != null;

            methodVisitor.visitMethodInsn(
                    INVOKESPECIAL,
                    constructor.getSource().getSuperClass().getInternalName(),
                    Constants.CONSTRUCTOR_METHOD_NAME,
                    Type.getMethodDescriptor(Type.VOID_TYPE, superCall.getTypes().toArray(new Type[0])),
                    false);
        });

        constructor.setWriter((method, methodVisitor) -> {
            int size = method.getSize();

            ASMUtil.pushConstant(methodVisitor, arguments.size());
            methodVisitor.visitTypeInsn(ANEWARRAY, Type.getType(Object.class).getInternalName());
            methodVisitor.visitVarInsn(ASTORE, size);

            int arrayCursor = 0;
            int sizeCursor = 1 + argumentOffset;
            for (NamedParameter parameter : arguments) {
                methodVisitor.visitVarInsn(ALOAD, size);

                ASMUtil.pushConstant(methodVisitor, arrayCursor);
                arrayCursor++;

                Type paramType = parameter.parameter().getType();
                methodVisitor.visitVarInsn(paramType.getOpcode(ILOAD), sizeCursor);
                sizeCursor += paramType.getSize();

                if (ASMUtil.isPrimitive(paramType) && !ASMUtil.isArray(paramType))
                    ConverterVisitor.convertTopPrimitiveToObject(methodVisitor, paramType);

                methodVisitor.visitInsn(AASTORE);
            }

            methodVisitor.visitLdcInsn(className);
            methodVisitor.visitLdcInsn(method.getDescriptor());
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, size);

            methodVisitor.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getType(Storage.class).getInternalName(),
                    "runConstructor",
                    Type.getMethodDescriptor(
                            Type.getType(void.class),
                            Type.getType(String.class),
                            Type.getType(String.class),
                            Type.getType(Object.class),
                            Type.getType(Object[].class)
                    ),
                    false
            );
        });
    }

}
