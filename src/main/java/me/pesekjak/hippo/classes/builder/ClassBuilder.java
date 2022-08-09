package me.pesekjak.hippo.classes.builder;

import me.pesekjak.hippo.classes.*;
import me.pesekjak.hippo.classes.content.Constructor;
import me.pesekjak.hippo.classes.content.Field;
import me.pesekjak.hippo.classes.converters.*;
import me.pesekjak.hippo.classes.types.NonPrimitiveType;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ClassBuilder implements IClassBuilder {

    public final ISkriptClass SKRIPT_CLASS;

    public final int JAVA_VERSION = Opcodes.V16;
    public final int ASM_VERSION = Opcodes.ASM9;

    public final ClassWriter CW = new ClassWriter(
            ASM_VERSION + ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS
    );

    public final ClassType CLASS_TYPE;
    public final String INTERNAL_NAME;

    private ClassBuilder(ISkriptClass skriptClass) {
        SKRIPT_CLASS = skriptClass;
        CLASS_TYPE = skriptClass.getClassType();
        INTERNAL_NAME = skriptClass.getType().internalName();
    }

    public static ClassBuilder forClass(ISkriptClass skriptClass) {
        return new ClassBuilder(skriptClass);
    }

    @Override
    public void build() {
        build(false, null);
    }

    /**
     * Builds the class and load it using the {@link DynamicClassLoader}
     * @param force if true, class will be forced to load
     */
    public void build(boolean force, AtomicReference<Throwable> result) {
        int modifiers = CLASS_TYPE.getValue() + sumModifiers(SKRIPT_CLASS.getModifiers().toArray(new Modifier[0]));
        List<String> interfaces = new ArrayList<>();
        for(Type implementing : SKRIPT_CLASS.getInterfaces()) {
            interfaces.add(implementing.internalName());
        }
        CW.visit(
                JAVA_VERSION,
                modifiers,
                INTERNAL_NAME,
                null,
                SKRIPT_CLASS.getSuperClass().internalName(),
                interfaces.toArray(new String[0])
        );
        boolean built = SKRIPT_CLASS.setup(this);
        CW.visitEnd();

        if(!built)
            return;
        DynamicClassLoader.CLASS_DATA.put(SKRIPT_CLASS.getType().dotPath(), CW.toByteArray());
        DynamicClassLoader.reload();
        if(force && result != null)
            result.set(DynamicClassLoader.getCurrentClassloader().forceDefine(SKRIPT_CLASS.getType().dotPath()));

    }

    @Override
    public ClassVisitor visitor() {
        return CW;
    }

    @Override
    public ISkriptClass skriptClass() {
        return SKRIPT_CLASS;
    }

    @Override
    public void pushClass(final MethodVisitor MV) {
        final Type REGISTRY = new NonPrimitiveType(SkriptClassBuilder.class);
        MV.visitLdcInsn(SKRIPT_CLASS.getType().dotPath());
        MV.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                REGISTRY.internalName(),
                "getSkriptClass",
                "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/ISkriptClass;",
                false);
    }

    /**
     * Pushes new literal to the stack.
     * @param MV Current MethodVisitor
     * @param o Object to push
     */
    public static void pushValue(final MethodVisitor MV, Object o) {
        int value;
        if(o instanceof Boolean)
            value = (Boolean) o ? 1 : 0;
        else if(o instanceof Character)
            value = (Character) o;
        else if(o instanceof Number)
            value = ((Number) o).intValue();
        else {
            MV.visitLdcInsn(o);
            return;
        }
        if(0 <= value && value <= 5)
            MV.visitInsn(Opcodes.ICONST_0 + value);
        else if(Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE)
            MV.visitIntInsn(Opcodes.BIPUSH, value);
        else if(Short.MIN_VALUE <= value && value <= Short.MAX_VALUE)
            MV.visitIntInsn(Opcodes.SIPUSH, value);
    }

    /**
     * Sums values of modifiers and returns.
     * @param modifiers Modifiers
     * @return Sum of values of modifiers
     */
    public static int sumModifiers(Modifier... modifiers) {
        int result = 0;
        for(Modifier modifier : modifiers) {
            result = result | modifier.getValue();
        }
        return result;
    }

    /**
     * Pushes the object on the top of the stack to a field.
     * @param MV Current MethodVisitor
     * @param field Field that should be changed
     * @param location Type of the owner (class of the field)
     */
    public static void putField(final MethodVisitor MV, Field field, Type location) {
        int opcode = field.getModifiers().contains(Modifier.STATIC) ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD;
        MV.visitFieldInsn(opcode, location.internalName(), field.getName(), field.getDescriptor());
    }

    /**
     * Casts the object on the top of the stack to given Type.
     * @param MV Current MethodVisitor
     * @param type Type to cast to
     */
    public static void cast(final MethodVisitor MV, Type type) {
        MV.visitTypeInsn(Opcodes.CHECKCAST, type.isArray() ? type.descriptor() : type.internalName());
    }

    /**
     * Converts NonPrimitive on the top of the stack using {@link ReflectConverter}.
     * @param MV Current MethodVisitor
     */
    public static void reflectConverter(final MethodVisitor MV) {
        MV.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                new NonPrimitiveType(ReflectConverter.class).internalName(),
                "handle",
                "(Ljava/lang/Object;)Ljava/lang/Object;",
                false);
    }

    /**
     * To prevent ClassCastException, this converter pushes null if
     * object on the top of the stack can't be cased to class of provided
     * type.
     * @param MV Current MethodVisitor
     * @param type Type to test cast for
     */
    public static void safeCastConverter(final MethodVisitor MV, Type type) {
        MV.visitLdcInsn(type.toASM());
        MV.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                new NonPrimitiveType(SafeCastConverter.class).internalName(),
                "safeCast",
                "(Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;",
                false);
    }

    /**
     * Converts Object on the top of the stack to Primitive counterpart using {@link PrimitiveConverter}.
     * @param MV Current MethodVisitor
     * @param type Primitive Type (Or its Non Primitive counterpart)
     */
    public static void primitiveConverter(final MethodVisitor MV, Type type) {
        Primitive primitive = Primitive.fromClass(type.findClass());
        if (primitive == null) return; // Given Type can't be converted to Primitive
        MV.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                new NonPrimitiveType(PrimitiveConverter.class).internalName(),
                "fromObject" + primitive.name(),
                "(Ljava/lang/Object;)" + primitive.getDescriptor(),
                false);
    }

    /**
     * Converts Primitive on the top of the stack to NonPrimitive counterpart using {@link NonPrimitiveConverter}.
     * @param MV Current MethodVisitor
     * @param type Primitive Type (Or its Non Primitive counterpart)
     */
    public static void nonPrimitiveConverter(final MethodVisitor MV, Type type) {
        Primitive primitive = Primitive.fromClass(type.findClass());
        if (primitive == null) return; // Given Type isn't Primitive
        MV.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                new NonPrimitiveType(NonPrimitiveConverter.class).internalName(),
                "fromPrimitive",
                "(" + primitive.getDescriptor() + ")" + new NonPrimitiveType(primitive.getNonPrimitiveClass()).descriptor(),
                false);
    }

    /**
     * Converts the Object on the top of the stack to provided Number NonPrimitive Type using {@link NumberConverter}.
     * @apiNote Does not cast to the converted Number type, returns back as Number
     * @param MV Current MethodVisitor
     * @param type Number Type to convert to (Or its Primitive counterpart)
     */
    public static void numberConverter(final MethodVisitor MV, Type type) {
        if(!NumberConverter.isNumber(type)) return; // Given Type is not a Number
        Primitive primitive = Primitive.fromClass(type.findClass());
        if(primitive == null) return;
        MV.visitTypeInsn(Opcodes.NEW, new NonPrimitiveType(NonPrimitiveType.class).internalName());
        MV.visitInsn(Opcodes.DUP);
        MV.visitLdcInsn(primitive.getNonPrimitiveClass().getName());
        MV.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                new NonPrimitiveType(NonPrimitiveType.class).internalName(),
                Constructor.METHOD_NAME,
                "(Ljava/lang/String;)V",
                false);
        MV.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                new NonPrimitiveType(NumberConverter.class).internalName(),
                "convertNumber",
                "(Ljava/lang/Object;Lme/pesekjak/hippo/classes/Type;)Ljava/lang/Number;",
                false);
    }

    /**
     * Converts the Object on the top of the stack to the Primitive number counterpart using {@link NumberConverter}.
     * @param MV Current MethodVisitor
     * @param type Primitive Number Type to convert to (Or its NonPrimitive counterpart)
     */
    public static void primitiveNumberConverter(final MethodVisitor MV, Type type) {
        if(!NumberConverter.isNumber(type)) return; // Given Type is not a Number
        Primitive primitive = Primitive.fromClass(type.findClass());
        if(primitive == null) return;
        MV.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                new NonPrimitiveType(NumberConverter.class).internalName(),
                "convert" + primitive.name(),
                "(Ljava/lang/Object;)" + primitive.getDescriptor(),
                false);
    }

}
