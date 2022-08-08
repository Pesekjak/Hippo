package me.pesekjak.hippo.classes.content;

import ch.njol.skript.lang.Expression;
import lombok.Getter;
import lombok.Setter;
import me.pesekjak.hippo.classes.ISkriptClass;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.builder.ClassBuilder;
import me.pesekjak.hippo.classes.builder.IClassBuilder;
import me.pesekjak.hippo.classes.converters.NumberConverter;
import me.pesekjak.hippo.classes.types.NonPrimitiveType;
import me.pesekjak.hippo.classes.types.VoidType;
import me.pesekjak.hippo.skript.Pair;
import me.pesekjak.hippo.utils.events.MethodCallEvent;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constructor extends Method {

    public static final String METHOD_NAME = "<init>";

    @Getter
    private final List<Type> superArguments = new ArrayList<>();
    @Getter @Setter
    private Expression<?> superValues;

    public Constructor() {
        super(METHOD_NAME, new VoidType());
    }

    public Constructor(Pair... arguments) {
        super(METHOD_NAME, new VoidType(), arguments);
    }

    @Override
    public void setup(final IClassBuilder CB) {
        int modifiers = ClassBuilder.sumModifiers(getModifiers().toArray(new Modifier[0]));
        if(isVararg()) modifiers += Opcodes.ACC_VARARGS;
        List<String> exceptions = new ArrayList<>();
        this.exceptions.forEach(e -> exceptions.add(e.internalName()));
        final MethodVisitor MV = CB.visitor().visitMethod(modifiers, name, getDescriptor(), null, exceptions.toArray(new String[0]));

        getAnnotations().forEach(annotation -> annotation.setupMethodAnnotation(CB, MV)); // setups annotations

        MV.visitCode();

        // Use to determinate by what offset the variables should be offset.
        int VAR_OFFSET = 1;
        for(Type argument : arguments.values())
            VAR_OFFSET += argument.size();

        // Calls to the super from expression used in super call effect.
        // Uses converters to prevent bad types on operand stack.
        MV.visitVarInsn(Opcodes.ALOAD, 0); // Uninitialised class
        int i = 1;
        int argumentIndex = 1; // used to get super value by ID
        for(Type argument : superArguments) {
            CB.pushClass(MV);
            ClassBuilder.pushValue(MV, getIdentifier());
            MV.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    new NonPrimitiveType(ISkriptClass.class).internalName(),
                    "getClassContent",
                    "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/content/ClassContent;",
                    false);
            ClassBuilder.cast(MV, new NonPrimitiveType(Constructor.class));
            ClassBuilder.pushValue(MV, argumentIndex);
            MV.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    new NonPrimitiveType(Constructor.class).internalName(),
                    "getSuperValue", "(I)Ljava/lang/Object;",
                    false);

            ClassBuilder.reflectConverter(MV);
            if(NumberConverter.isNumber(argument)) // Is Number and needs to be converted to right value.
                ClassBuilder.numberConverter(MV, argument);
            if(argument.storeCode() != Opcodes.ASTORE) { // Stores as Primitive, needs to be converted.
                if(NumberConverter.isNumber(argument)) // Converts using Number Converter.
                    ClassBuilder.primitiveNumberConverter(MV, argument);
                else // Converts using Primitive Converter.
                    ClassBuilder.primitiveConverter(MV, argument);
            } else {
                ClassBuilder.safeCastConverter(MV, argument);
                ClassBuilder.cast(MV, argument);
            }
            i += argument.size();
            argumentIndex++;
        }
        MV.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                CB.skriptClass().getSuperClass().internalName(),
                name,
                getSuperDescriptor(),
                false);

        // Sets all fields with preset values
        for(ClassContent content : CB.skriptClass().getClassContent().values()) {
            if(!(content instanceof Field field)) continue;
            if(field.getModifiers().contains(Modifier.STATIC)) continue; // Static fields are handled in static block
            if(field.getValue() == null) continue; // Has no pre-set value
            MV.visitVarInsn(Opcodes.ALOAD, 0);
            CB.pushClass(MV);
            ClassBuilder.pushValue(MV, field.getIdentifier());
            MV.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    new NonPrimitiveType(ISkriptClass.class).internalName(),
                    "getClassContent",
                    "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/content/ClassContent;",
                    false);
            ClassBuilder.cast(MV, new NonPrimitiveType(Field.class));
            MV.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    new NonPrimitiveType(Field.class).internalName(),
                    "getActualValue", "()Ljava/lang/Object;",
                    false);

            ClassBuilder.reflectConverter(MV);
            if(NumberConverter.isNumber(field.type)) // Is Number and needs to be converted to right value.
                ClassBuilder.numberConverter(MV, field.type);
            if(field.type.storeCode() != Opcodes.ASTORE) { // Stores as Primitive, needs to be converted.
                if(NumberConverter.isNumber(field.type)) // Converts using Number Converter.
                    ClassBuilder.primitiveNumberConverter(MV, field.type);
                else // Converts using Primitive Converter.
                    ClassBuilder.primitiveConverter(MV, field.type);
            } else {
                ClassBuilder.safeCastConverter(MV, field.type);
                ClassBuilder.cast(MV, field.type);
            }
            ClassBuilder.putField(MV, field, CB.skriptClass().getType());
        }

        // Creates and stores new MethodCall Event with var index VAR_OFFSET + 1
        MV.visitTypeInsn(Opcodes.NEW, new NonPrimitiveType(MethodCallEvent.class).internalName());
        MV.visitInsn(Opcodes.DUP);
        CB.pushClass(MV);
        ClassBuilder.pushValue(MV, getIdentifier());
        MV.visitVarInsn(Opcodes.ALOAD, 0);
        MV.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                new NonPrimitiveType(MethodCallEvent.class).internalName(),
                Constructor.METHOD_NAME,
                "(Lme/pesekjak/hippo/classes/ISkriptClass;Ljava/lang/String;Ljava/lang/Object;)V",
                false);
        MV.visitVarInsn(Opcodes.ASTORE, VAR_OFFSET + 1);

        // Loads the event and adds the arguments to it.
        // Uses converters to prevent bad types on operand stack.
        MV.visitVarInsn(Opcodes.ALOAD, VAR_OFFSET + 1);
        i = 1;
        for (Type argument : arguments.values()) {
            MV.visitVarInsn(argument.loadCode(), i);
            if (!(argument.loadCode() == Opcodes.ALOAD)) // Loaded as Primitive, needs to be converted.
                ClassBuilder.nonPrimitiveConverter(MV, argument);
            MV.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    new NonPrimitiveType(MethodCallEvent.class).internalName(),
                    "addArgument",
                    "(Ljava/lang/Object;)Lme/pesekjak/hippo/utils/events/MethodCallEvent;",
                    false);
            i += argument.size();
        }

        // Triggers the section of the Constructor
        MV.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                new NonPrimitiveType(MethodCallEvent.class).internalName(),
                "trigger", "()V",
                false);

        MV.visitInsn(Opcodes.RETURN); // Constructors should always have void type
        MV.visitMaxs(0, 0); // should be automatically calculated by ClassBuilder's ClassVisitor

        MV.visitEnd();
        CB.visitor().visitEnd();
    }

    public String getSuperDescriptor() {
        StringBuilder argumentsDescriptor = new StringBuilder();
        for(Type argument : superArguments) {
            argumentsDescriptor.append(argument.descriptor());
        }
        return "(" + argumentsDescriptor + ")V";
    }

    public void addSuperArguments(Type... arguments) {
        superArguments.addAll(Arrays.asList(arguments));
    }

    public Object getSuperValue(int index) {
        if(--index < 0) return null;
        Object[] values = superValues.getAll(new MethodCallEvent(null, getIdentifier(), this));
        if(!(values.length > index))
            return null;
        return values[index];
    }

    public static Constructor defaultConstructor() {
        Constructor defaultConstructor = new Constructor();
        defaultConstructor.addModifiers(Modifier.PUBLIC);
        return defaultConstructor;
    }

}
