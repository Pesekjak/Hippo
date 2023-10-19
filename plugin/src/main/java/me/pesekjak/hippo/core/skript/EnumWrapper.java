package me.pesekjak.hippo.core.skript;

import me.pesekjak.hippo.core.ASMUtil;
import me.pesekjak.hippo.core.ClassContent;
import me.pesekjak.hippo.core.Constants;
import me.pesekjak.hippo.core.Field;
import me.pesekjak.hippo.elements.effects.EffEnum;
import me.pesekjak.hippo.utils.ConverterVisitor;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * Wrapper for a enum constant.
 * <p>
 * Serves as a bridge between user Skript code and compiled class.
 *
 * @param field wrapped field
 * @param enumEffect enum effect used by the enum constant
 */
public record EnumWrapper(Field field, EffEnum enumEffect) implements ClassContentSkriptWrapper {

    @Override
    public ClassContent content() {
        return field;
    }

    /**
     * Injects code to the initializer of the enum.
     */
    public void injectCode() {
        assert field.getSource() != null;
        String className = field.getSource().getName();

        field.setInitializer((method, methodVisitor) -> {
            int size = method.getSize();

            int superArgsSize = enumEffect.getTypes().size();

            methodVisitor.visitLdcInsn(className);
            methodVisitor.visitLdcInsn(field.getName());
            ASMUtil.pushConstant(methodVisitor, superArgsSize);

            methodVisitor.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getType(Storage.class).getInternalName(),
                    "getEnumValues",
                    Type.getMethodDescriptor(
                            Type.getType(Object[].class),
                            Type.getType(String.class),
                            Type.getType(String.class),
                            Type.INT_TYPE
                    ),
                    false
            );

            methodVisitor.visitVarInsn(ASTORE, size);

            methodVisitor.visitTypeInsn(NEW, field.getSource().getType().getInternalName());
            methodVisitor.visitInsn(DUP);

            methodVisitor.visitLdcInsn(field.getName());
            int enumIndex = field.getSource().getFields().stream().filter(Field::isEnum).toList().indexOf(field);
            ASMUtil.pushConstant(methodVisitor, enumIndex);
            for (int i = 0; i < superArgsSize; i++) {
                Type type = enumEffect.getTypes().get(i);
                methodVisitor.visitVarInsn(ALOAD, size);
                ASMUtil.pushConstant(methodVisitor, i);
                methodVisitor.visitInsn(AALOAD);
                ConverterVisitor.convertTopObject(methodVisitor, type);
            }

            List<Type> arguments = new ArrayList<>();
            arguments.add(Type.getType(String.class));
            arguments.add(Type.INT_TYPE);
            arguments.addAll(enumEffect.getTypes());

            methodVisitor.visitMethodInsn(
                    INVOKESPECIAL,
                    field.getSource().getType().getInternalName(),
                    Constants.CONSTRUCTOR_METHOD_NAME,
                    Type.getMethodDescriptor(
                            Type.VOID_TYPE,
                            arguments.toArray(new Type[0])
                    ),
                    false
            );

            methodVisitor.visitInsn(DUP);
            methodVisitor.visitFieldInsn(PUTSTATIC, field.getSource().getType().getInternalName(), field.getName(), field.getDescriptor());

            methodVisitor.visitFieldInsn(
                    GETSTATIC,
                    field.getSource().getType().getInternalName(),
                    "$VALUES",
                    ASMUtil.getArrayDescriptor(field.getSource().getType(), 1)
            );
            methodVisitor.visitInsn(SWAP);
            ASMUtil.pushConstant(methodVisitor, enumIndex);
            methodVisitor.visitInsn(SWAP);
            methodVisitor.visitInsn(AASTORE);
        });
    }

}
