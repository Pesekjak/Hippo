package me.pesekjak.hippo.classes.content;

import ch.njol.skript.lang.Trigger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.pesekjak.hippo.classes.*;
import me.pesekjak.hippo.classes.builder.ClassBuilder;
import me.pesekjak.hippo.classes.builder.IClassBuilder;
import me.pesekjak.hippo.classes.converters.NumberConverter;
import me.pesekjak.hippo.classes.types.NonPrimitiveType;
import me.pesekjak.hippo.skript.Pair;
import me.pesekjak.hippo.skript.classes.annotations.SkriptAnnotation;
import me.pesekjak.hippo.utils.events.MethodCallEvent;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.*;

@RequiredArgsConstructor
public class Method extends Modifiable implements ClassContent {

    protected final String name;
    protected final Type type;

    @Getter
    protected final Map<String, Type> arguments = new LinkedHashMap<>();

    @Getter
    protected final List<Type> exceptions = new ArrayList<>();

    @Getter @Setter
    protected Trigger trigger;

    @Getter @Setter
    private boolean vararg = false;

    // Default value for annotations
    @Getter @Setter
    private SkriptAnnotation.AnnotationElement defaultValue;

    public Method(String name, Type type, Pair... arguments) {
        this(name, type);
        Arrays.stream(arguments).forEach(argument ->
                this.arguments.put(argument.key(), argument.type()));
    }

    @Override
    public String getIdentifier() {
        StringBuilder argumentsDescriptor = new StringBuilder();
        arguments.values().forEach(argument ->
                argumentsDescriptor.append(argument.descriptor())
        );
        return name + "(" + argumentsDescriptor + ")";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getDescriptor() {
        String returnDescriptor = type.descriptor();
        StringBuilder argumentsDescriptor = new StringBuilder();
        arguments.values().forEach(argument ->
                argumentsDescriptor.append(argument.descriptor())
        );
        return "(" + argumentsDescriptor + ")" + returnDescriptor;
    }

    @Override
    public void setup(final IClassBuilder CB) {
        int modifiers = ClassBuilder.sumModifiers(getModifiers().toArray(new Modifier[0]));
        if(vararg) modifiers += Opcodes.ACC_VARARGS;
        List<String> exceptions = new ArrayList<>();
        this.exceptions.forEach(e -> exceptions.add(e.internalName()));
        final MethodVisitor MV = CB.visitor().visitMethod(modifiers, name, getDescriptor(), null, exceptions.toArray(new String[0]));

        getAnnotations().forEach(annotation -> annotation.setupMethodAnnotation(CB, MV)); // setups annotations

        if (!hasBody()) { // Method is abstract and has no code
            MV.visitEnd();
            CB.visitor().visitEnd();
            return;
        }

        MV.visitCode();

        // Use to determinate by what offset the variables should be offset.
        // (If method is static, this (index 0) is missing.)
        int VAR_OFFSET = getModifiers().contains(Modifier.STATIC) ? 0 : 1;
        for(Type argument : arguments.values())
            VAR_OFFSET += argument.size();

        // Creates and stores new MethodCall Event with var index VAR_OFFSET + 1
        MV.visitTypeInsn(Opcodes.NEW, new NonPrimitiveType(MethodCallEvent.class).internalName());
        MV.visitInsn(Opcodes.DUP);
        CB.pushClass(MV);
        ClassBuilder.pushValue(MV, getIdentifier());
        if (!getModifiers().contains(Modifier.STATIC))
            MV.visitVarInsn(Opcodes.ALOAD, 0);
        else
            MV.visitInsn(Opcodes.ACONST_NULL);
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
        int i = getModifiers().contains(Modifier.STATIC) ? 0 : 1; // Variable argument index, starts from 0 if method is static.
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

        // Triggers the section of the Method
        MV.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                new NonPrimitiveType(MethodCallEvent.class).internalName(),
                "trigger", "()V",
                false);

        // Gets the Method Output and returns
        if (!(type.findClass() == void.class)) {
            MV.visitVarInsn(Opcodes.ALOAD, VAR_OFFSET + 1); // MethodCall Event
            MV.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    new NonPrimitiveType(MethodCallEvent.class).internalName(),
                    "getOutput",
                    "()Ljava/lang/Object;",
                    false);

            ClassBuilder.reflectConverter(MV);
            if(NumberConverter.isNumber(type)) // Is Number and needs to be converted to right value.
                ClassBuilder.numberConverter(MV, type);
            if(type.returnCode() != Opcodes.ARETURN) { // Returns as Primitive, needs to be converted.
                if(NumberConverter.isNumber(type)) // Converts using Number Converter.
                    ClassBuilder.primitiveNumberConverter(MV, type);
                else // Converts using Primitive Converter.
                    ClassBuilder.primitiveConverter(MV, type);
            } else {
                ClassBuilder.safeCastConverter(MV, type);
                ClassBuilder.cast(MV, type);
            }
        }

        MV.visitInsn(type.returnCode());
        MV.visitMaxs(0, 0); // should be automatically calculated by ClassBuilder's ClassVisitor

        MV.visitEnd();
        CB.visitor().visitEnd();
    }


    public void addArguments(Pair... pairs) {
        for(Pair pair : pairs) {
            this.arguments.put(pair.key(), pair.type());
        }
    }

    public void addExceptions(Type... exceptions) {
        this.exceptions.addAll(Arrays.asList(exceptions));
    }

    public boolean hasBody() {
        return trigger != null;
    }

}
