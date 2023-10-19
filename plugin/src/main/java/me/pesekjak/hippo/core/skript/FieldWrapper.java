package me.pesekjak.hippo.core.skript;

import ch.njol.skript.lang.Expression;
import me.pesekjak.hippo.core.ClassContent;
import me.pesekjak.hippo.core.Field;
import me.pesekjak.hippo.utils.ConverterVisitor;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * Wrapper for a field.
 * <p>
 * Serves as a bridge between user Skript code and compiled class.
 *
 * @param field wrapped field
 * @param value expression used to set the initial field value
 */
public record FieldWrapper(Field field, @Nullable Expression<?> value) implements ClassContentSkriptWrapper {

    @Override
    public ClassContent content() {
        return field;
    }

    /**
     * Injects code to the initializer of the field.
     */
    public void injectCode() {
        assert field.getSource() != null;
        String className = field.getSource().getName();

        field.setInitializer((method, methodVisitor) -> {
            methodVisitor.visitLdcInsn(className);
            methodVisitor.visitLdcInsn(field.getName());
            if (!field.isStatic())
                methodVisitor.visitVarInsn(ALOAD, 0);
            else
                methodVisitor.visitInsn(ACONST_NULL);

            methodVisitor.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getType(Storage.class).getInternalName(),
                    "getFieldValue",
                    Type.getMethodDescriptor(
                            Type.getType(Object.class),
                            Type.getType(String.class),
                            Type.getType(String.class),
                            Type.getType(Object.class)
                    ),
                    false
            );

            ConverterVisitor.convertTopObject(methodVisitor, field.getType().getType());
            int opcode;
            if (!field.isStatic()) {
                opcode = PUTFIELD;
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitInsn(SWAP);
            } else {
                opcode = PUTSTATIC;
            }
            methodVisitor.visitFieldInsn(opcode, field.getSource().getType().getInternalName(), field.getName(), field.getDescriptor());
        });
    }

}
