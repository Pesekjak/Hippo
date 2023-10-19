package me.pesekjak.hippo.core.classes;

import me.pesekjak.hippo.core.*;
import me.pesekjak.hippo.core.annotations.Annotation;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;

import java.util.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Represents an enum class.
 */
public class Enum extends AbstractClass {

    public Enum(@Nullable AbstractClass outerClass,
                 Type type,
                 Collection<Type> interfaces,
                 int modifier,
                 Collection<Annotation> annotations) throws IllegalModifiersException {
        super(
                outerClass,
                type,
                Type.getType(java.lang.Enum.class),
                interfaces,
                modifier | ACC_FINAL | ACC_SUPER | ACC_ENUM,
                annotations
        );
    }

    @Override
    protected void checkField(Field field) {

    }

    @Override
    protected void checkConstructor(Constructor constructor) throws IllegalClassContentException {
        if (!constructor.isPrivate())
            throw new IllegalClassContentException("Enum constructors need to be private");
        List<Parameter> parameters = constructor.getParameters();
        if (parameters.size() < 2) throw new IllegalClassContentException("Invalid enum constructor");
        if (!parameters.get(0).getType().getDescriptor().equals(Type.getDescriptor(String.class)))
            throw new IllegalClassContentException("First argument of enum constructor needs to be String");
        if (!parameters.get(1).getType().getDescriptor().equals(Type.getDescriptor(int.class)))
            throw new IllegalClassContentException("First argument of enum constructor needs to be int");
    }

    @Override
    protected void checkMethod(Method method) throws IllegalClassContentException {
        if (method.isAbstract())
            throw new IllegalClassContentException("Method '" + method.getName() + "' cannot be abstract");
    }

    @Override
    public void visitOuterInit(Constructor constructor, MethodVisitor visitor) {

    }

    @Override
    public void visitOuterClinit(Method method, MethodVisitor visitor) {

    }

    @Override
    public void visit(ClassVisitor visitor) {
        String[] interfaces = getInterfaces().stream().map(Type::getInternalName).toArray(String[]::new);
        visitor.visit(V17, getModifier(), getType().getInternalName(), null, getSuperClass().getInternalName(), interfaces);
        getAnnotations().forEach(annotation -> annotation.visit(visitor));
        getContents().forEach(content -> content.visit(visitor));

        List<Field> enums = getFields().stream().filter(Field::isEnum).toList();

        $valuesMethod: {
            MethodVisitor methodVisitor = visitor.visitMethod(
                    ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC,
                    "$values",
                    Type.getMethodDescriptor(
                            ASMUtil.getArrayType(getType(), 1)
                    ),
                    null,
                    new String[0]
            );
            methodVisitor.visitCode();
            ASMUtil.pushConstant(methodVisitor, enums.size());
            methodVisitor.visitTypeInsn(ANEWARRAY, getType().getInternalName());
            for (int i = 0; i < enums.size(); i++) {
                methodVisitor.visitInsn(DUP);
                ASMUtil.pushConstant(methodVisitor, i);
                methodVisitor.visitFieldInsn(
                        GETSTATIC,
                        getType().getInternalName(),
                        enums.get(i).getName(),
                        getType().getDescriptor()
                );
                methodVisitor.visitInsn(AASTORE);
            }

            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitMaxs(0, 0); // should be automatically calculated by ClassBuilder's ClassVisitor

            methodVisitor.visitEnd();
            visitor.visitEnd();
        }

        valueOfMethod: {
            MethodVisitor methodVisitor = visitor.visitMethod(
                    ACC_PUBLIC | ACC_STATIC,
                    "valueOf",
                    Type.getMethodDescriptor(
                            getType(),
                            Type.getType(String.class)
                    ),
                    null,
                    new String[0]
            );
            methodVisitor.visitCode();
            methodVisitor.visitLdcInsn(getType());
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getType(java.lang.Enum.class).getInternalName(),
                    "valueOf",
                    Type.getMethodDescriptor(
                            Type.getType(java.lang.Enum.class),
                            Type.getType(java.lang.Class.class),
                            Type.getType(String.class)
                    ),
                    false);
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, getType().getInternalName());

            methodVisitor.visitInsn(Opcodes.ARETURN);
            methodVisitor.visitMaxs(0, 0); // should be automatically calculated by ClassBuilder's ClassVisitor

            methodVisitor.visitEnd();
            visitor.visitEnd();
        }

        valuesMethod: {
            MethodVisitor methodVisitor = visitor.visitMethod(
                    ACC_PUBLIC | ACC_STATIC,
                    "values",
                    Type.getMethodDescriptor(
                            ASMUtil.getArrayType(getType(), 1)
                    ),
                    null,
                    new String[0]
            );
            methodVisitor.visitCode();
            methodVisitor.visitFieldInsn(
                    Opcodes.GETSTATIC,
                    getType().getInternalName(),
                    "$VALUES",
                    ASMUtil.getArrayDescriptor(getType(), 1));
            methodVisitor.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    ASMUtil.getArrayDescriptor(getType(), 1),
                    "clone",
                    Type.getMethodDescriptor(
                            Type.getType(Object.class)
                    ),
                    false);
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, ASMUtil.getArrayInternalName(getType(), 1));

            methodVisitor.visitInsn(Opcodes.ARETURN);
            methodVisitor.visitMaxs(0, 0); // should be automatically calculated by ClassBuilder's ClassVisitor

            methodVisitor.visitEnd();
            visitor.visitEnd();
        }

        $VALUESField: {
            FieldVisitor fieldVisitor = visitor.visitField(
                    ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_SYNTHETIC,
                    "$VALUES",
                    ASMUtil.getArrayDescriptor(getType(), 1),
                    null,
                    null
            );
            fieldVisitor.visitEnd();
            visitor.visitEnd();
        }

        visitor.visitEnd();
    }

    @Override
    public void visitInit(Constructor constructor, MethodVisitor visitor) {

    }

    @Override
    public void visitClinit(Method method, MethodVisitor visitor) {
        List<Field> enums = getFields().stream().filter(Field::isEnum).toList();
        ASMUtil.pushConstant(visitor, enums.size());
        visitor.visitTypeInsn(ANEWARRAY, getType().getInternalName());
        visitor.visitFieldInsn(PUTSTATIC, getType().getInternalName(), "$VALUES", ASMUtil.getArrayDescriptor(getType(), 1));
    }

    @Override
    public Collection<ClassContent> getContents() {
        List<ClassContent> content = new ArrayList<>(super.getContents());
        List<Constructor> constructors = content.stream()
                .map(c -> c instanceof Constructor constructor ? constructor : null)
                .filter(Objects::nonNull)
                .toList();
        content.removeAll(constructors);

        assert getSource() != null;

        for (Constructor c : constructors) {
            c.setSuperWriter((constructor, methodVisitor) -> {
                assert constructor.getSource() != null;
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitVarInsn(ALOAD, 1);
                methodVisitor.visitVarInsn(ILOAD, 2);
                methodVisitor.visitMethodInsn(
                        INVOKESPECIAL,
                        constructor.getSource().getSuperClass().getInternalName(),
                        Constants.CONSTRUCTOR_METHOD_NAME,
                        Type.getMethodDescriptor(
                                Type.VOID_TYPE,
                                Type.getType(String.class),
                                Type.INT_TYPE
                        ),
                        false);
            });
        }

        content.addAll(constructors);
        return content;
    }
}
