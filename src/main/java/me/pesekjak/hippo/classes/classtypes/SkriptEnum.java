package me.pesekjak.hippo.classes.classtypes;

import me.pesekjak.hippo.classes.ClassType;
import me.pesekjak.hippo.classes.ISkriptClass;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.builder.ClassBuilder;
import me.pesekjak.hippo.classes.builder.IClassBuilder;
import me.pesekjak.hippo.classes.content.ClassContent;
import me.pesekjak.hippo.classes.content.Constructor;
import me.pesekjak.hippo.classes.content.ContentType;
import me.pesekjak.hippo.classes.content.Enum;
import me.pesekjak.hippo.classes.content.Field;
import me.pesekjak.hippo.classes.converters.NumberConverter;
import me.pesekjak.hippo.classes.types.NonPrimitiveType;
import me.pesekjak.hippo.classes.types.primitives.IntType;
import me.pesekjak.hippo.skript.Pair;
import me.pesekjak.hippo.utils.Logger;
import me.pesekjak.hippo.utils.events.MethodCallEvent;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.*;

public class SkriptEnum extends ISkriptClass {

    private static final String ENUM_NAME_ARGUMENT = "$ENUM_NAME";
    private static final String ENUM_INDEX_ARGUMENT = "$ENUM_INDEX";

    public SkriptEnum(Type type) {
        super(type);
        setSuperClass(new NonPrimitiveType(java.lang.Enum.class));
    }

    @Override
    public ClassType getClassType() {
        return ClassType.ENUM;
    }

    @Override
    public boolean illegalModifiers(ContentType contentType, List<Modifier> modifiers) {
        if(contentType == ContentType.CONSTRUCTOR) {
            return !modifiers.contains(Modifier.PRIVATE);
        }
        if(contentType == ContentType.CLASS &&
                (modifiers.contains(Modifier.ABSTRACT) || modifiers.contains(Modifier.FINAL)))
            return true;
        if(contentType != ContentType.METHOD) return false;
        return modifiers.contains(Modifier.ABSTRACT);
    }

    @Override
    public boolean canHave(ContentType contentType) {
        return true;
    }

    @Override
    public boolean canExtend() {
        return false;
    }

    @Override
    public boolean setup(final IClassBuilder CB) {

        boolean hasConstructor = false;
        for(ClassContent content : getClassContent().values()) {
            if (content instanceof Constructor) {
                hasConstructor = true;
                break;
            }
        }
        if(!hasConstructor) {
            // Adds default Java Enum constructor
            Constructor enumConstructor = new Constructor();
            enumConstructor.addModifiers(Modifier.PRIVATE); // Enum constructors are private
            getClassContent().put(enumConstructor.getIdentifier(), enumConstructor);
        }

        List<Constructor> constructors = new ArrayList<>();
        List<Enum> enumFields = new ArrayList<>();

        // Setups all class contents except for constructors and
        // enums, because these need to be setup differently for enums
        for(ClassContent content : getClassContent().values()) {
            if(content instanceof Constructor constructor)
                constructors.add(constructor);
            else if(content instanceof Enum enumField)
                enumFields.add(enumField);
            else
                content.setup(CB);
        }

        // Setups all constructors to be functional for enums
        for(Constructor constructor : constructors) {

            // All enums constructors have String and int arguments
            Map<String, Type> arguments = new LinkedHashMap<>();
            arguments.put(ENUM_NAME_ARGUMENT, new NonPrimitiveType(String.class));
            arguments.put(ENUM_INDEX_ARGUMENT, new IntType());
            for(String key : constructor.getArguments().keySet())
                arguments.put(key, constructor.getArguments().get(key));

            // Re-adds the arguments
            constructor.getArguments().clear();
            for(String key : arguments.keySet())
                constructor.getArguments().put(key, arguments.get(key));

            // All constructors for enums use this as super constructor
            constructor.getSuperArguments().clear();
            constructor.addSuperArguments(
                    new NonPrimitiveType(String.class),
                    new IntType()
            );

            setupEnumConstructor(CB, constructor);

        }

        getAnnotations().forEach(annotation -> annotation.setupClassAnnotation(CB)); // setups annotations

        // Setups the static block (needs to be setup differently for enums)
        MethodVisitor MV = CB.visitor().visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, new String[0]);
        MV.visitCode();

        // Setups enums
        int enumIndex = 0;
        for(ClassContent content : getClassContent().values()) {
            if(!(content instanceof Enum enumField)) continue;

            MV.visitTypeInsn(Opcodes.NEW, CB.skriptClass().getType().internalName());
            MV.visitInsn(Opcodes.DUP);

            // Enum name and identifier
            ClassBuilder.pushValue(MV, enumField.getName());
            ClassBuilder.pushValue(MV, enumIndex);

            // Argument values
            int argumentIndex = 1; // used to get super value by ID
            for(Type argument : enumField.getSuperArguments()) {
                CB.pushClass(MV);
                ClassBuilder.pushValue(MV, enumField.getIdentifier());
                MV.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        new NonPrimitiveType(ISkriptClass.class).internalName(),
                        "getClassContent",
                        "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/content/ClassContent;",
                        false);
                ClassBuilder.cast(MV, new NonPrimitiveType(Enum.class));
                ClassBuilder.pushValue(MV, argumentIndex);
                MV.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        new NonPrimitiveType(Enum.class).internalName(),
                        "getSuperValue", "(I)Ljava/lang/Object;",
                        false);

                ClassBuilder.reflectConverter(MV);
                if(NumberConverter.isNumber(argument)) // Is Number and needs to be converted to right value.
                    ClassBuilder.numberConverter(MV, argument);
                if(argument.loadCode() != Opcodes.ALOAD) { // Loads as Primitive, needs to be converted.
                    if(NumberConverter.isNumber(argument)) // Converts using Number Converter.
                        ClassBuilder.primitiveNumberConverter(MV, argument);
                    else // Converts using Primitive Converter.
                        ClassBuilder.primitiveConverter(MV, argument);
                } else {
                    ClassBuilder.safeCastConverter(MV, argument);
                    ClassBuilder.cast(MV, argument);
                }

                argumentIndex++;
            }

            int i = 0;
            List<Pair> dummyArguments = new ArrayList<>();
            for(Type argument : enumField.getSuperArguments())
                dummyArguments.add(new Pair("A" + ++i, argument));
            ClassContent constructorContent = CB.skriptClass().getClassContent(new Constructor(dummyArguments.toArray(new Pair[0])).getIdentifier());
            Constructor constructor;
            if(constructorContent instanceof Constructor)
                constructor = (Constructor) constructorContent;
            else {
                Logger.severe("Class '" + CB.skriptClass().getType().dotPath() + "' failed to compile because enum '" + enumField.getName() + "' is using non-existing constructor.");
                return false;
            }

            MV.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    CB.skriptClass().getType().internalName(),
                    Constructor.METHOD_NAME,
                    constructor.getDescriptor(),
                    false);

            ClassBuilder.putField(MV, enumField, CB.skriptClass().getType());

            enumIndex++;
        }

        // Setups the static pre-set fields
        for(ClassContent content : getClassContent().values()) {
            if(!(content instanceof Field field)) continue;
            if(content instanceof Enum) continue; // Enums are handled separately
            if(!field.getModifiers().contains(Modifier.STATIC)) continue; // Non static fields are handled in constructors

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
            if(NumberConverter.isNumber(field.getType())) // Is Number and needs to be converted to right value.
                ClassBuilder.numberConverter(MV, field.getType());
            if(field.getType().storeCode() != Opcodes.ASTORE) { // Stores as Primitive, needs to be converted.
                if(NumberConverter.isNumber(field.getType())) // Converts using Number Converter.
                    ClassBuilder.primitiveNumberConverter(MV, field.getType());
                else // Converts using Primitive Converter.
                    ClassBuilder.primitiveConverter(MV, field.getType());
            } else {
                ClassBuilder.safeCastConverter(MV, field.getType());
                ClassBuilder.cast(MV, field.getType());
            }
            ClassBuilder.putField(MV, field, CB.skriptClass().getType());
        }

        // Triggers the static section
        CB.pushClass(MV);
        MV.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                new NonPrimitiveType(ISkriptClass.class).internalName(),
                "triggerStatic", "()V",
                false);

        MV.visitInsn(Opcodes.RETURN);
        MV.visitMaxs(0, 0); // should be automatically calculated by ClassBuilder's ClassVisitor

        MV.visitEnd();
        CB.visitor().visitEnd();

        // Creates enum fields
        for(Enum enumField : enumFields) {
            enumField.setup(CB);
        }

        // Setup rest of the default enum methods
        setupEnumClass(CB);

        return true;
    }

    /**
     * Setups the constructor as Enum Constructor.
     * @param CB ClassBuilder building the class
     * @param constructor Constructor that should be setup
     */
    protected void setupEnumConstructor(final IClassBuilder CB, Constructor constructor) {
        // Gets the identifier used in map (without the default String and int arguments)
        String identifier = null;
        for(String contentIdentifier : getClassContent().keySet())
            if(getClassContent().get(contentIdentifier) == constructor) {
                identifier = contentIdentifier;
                break;
            }
        if(identifier == null) return;

        int modifiers = ClassBuilder.sumModifiers(constructor.getModifiers().toArray(new Modifier[0]));
        if(constructor.isVararg()) modifiers += Opcodes.ACC_VARARGS;
        List<String> exceptions = new ArrayList<>();
        constructor.getExceptions().forEach(e -> exceptions.add(e.internalName()));
        final MethodVisitor MV = CB.visitor().visitMethod(modifiers, constructor.getName(), constructor.getDescriptor(), null, exceptions.toArray(new String[0]));

        getAnnotations().forEach(annotation -> annotation.setupMethodAnnotation(CB, MV)); // setups annotations

        MV.visitCode();

        // Use to determinate by what offset the variables should be offset.
        final int VAR_OFFSET = constructor.getArguments().size() + 1;

        // Calls for the Java Enum super
        MV.visitVarInsn(Opcodes.ALOAD, 0);
        MV.visitVarInsn(Opcodes.ALOAD, 1); // For argument 1, always String
        MV.visitVarInsn(Opcodes.ILOAD, 2); // For argument 2, always int
        MV.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                new NonPrimitiveType(java.lang.Enum.class).internalName(),
                Constructor.METHOD_NAME,
                constructor.getSuperDescriptor(), // Always "(Ljava/lang/String;I)V"
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
            if(NumberConverter.isNumber(field.getType())) // Is Number and needs to be converted to right value.
                ClassBuilder.numberConverter(MV, field.getType());
            if(field.getType().storeCode() != Opcodes.ASTORE) { // Stores as Primitive, needs to be converted.
                if(NumberConverter.isNumber(field.getType())) // Converts using Number Converter.
                    ClassBuilder.primitiveNumberConverter(MV, field.getType());
                else // Converts using Primitive Converter.
                    ClassBuilder.primitiveConverter(MV, field.getType());
            } else {
                ClassBuilder.safeCastConverter(MV, field.getType());
                ClassBuilder.cast(MV, field.getType());
            }
            ClassBuilder.putField(MV, field, CB.skriptClass().getType());
        }

        // Creates and stores new MethodCall Event with var index VAR_OFFSET + 1
        MV.visitTypeInsn(Opcodes.NEW, new NonPrimitiveType(MethodCallEvent.class).internalName());
        MV.visitInsn(Opcodes.DUP);
        CB.pushClass(MV);
        ClassBuilder.pushValue(MV, identifier);
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
        int i = 1;
        for (Type argument : constructor.getArguments().values()) {
            // Skips first 2 enum constructor arguments, because
            // they're not defined by user
            if(i == 1 || i == 2) {
                i++;
                continue;
            }
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

    /**
     * Setups the additional class contents for enums.
     * @param CB ClassBuilder for the enum class
     */
    protected void setupEnumClass(final IClassBuilder CB) {
        List<Enum> enumFields = new ArrayList<>();
        getClassContent().values().stream()
                .filter(content -> content instanceof Enum)
                .forEach(enumField -> enumFields.add((Enum) enumField));

        Type type = CB.skriptClass().getType();
        Type arrayType = type.array();

        // Setups $values() enum method
        int modifiers = Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_SYNTHETIC;
        MethodVisitor MV = CB.visitor().visitMethod(modifiers, "$values", "()" + arrayType.descriptor(), null, new String[0]);
        MV.visitCode();

        ClassBuilder.pushValue(MV, enumFields.size() - 1);
        MV.visitTypeInsn(Opcodes.ANEWARRAY, type.internalName());
        int i = 0;
        for(Enum enumField : enumFields) {
            MV.visitInsn(Opcodes.DUP);
            ClassBuilder.pushValue(MV, i);
            MV.visitFieldInsn(
                    Opcodes.GETSTATIC,
                    type.internalName(),
                    enumField.getName(),
                    enumField.getDescriptor());
            MV.visitInsn(Opcodes.AASTORE);
            i++;
        }

        MV.visitInsn(Opcodes.ARETURN); // Always used by arrays
        MV.visitMaxs(0, 0); // should be automatically calculated by ClassBuilder's ClassVisitor

        MV.visitEnd();
        CB.visitor().visitEnd();

        // Setups valueOf(java.lang.String) enum method
        modifiers = Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC;
        MV = CB.visitor().visitMethod(modifiers, "valueOf", "(Ljava/lang/String;)" + type.descriptor(), null, new String[0]);
        MV.visitCode();

        MV.visitLdcInsn(type.toASM());
        MV.visitVarInsn(Opcodes.ALOAD, 0);
        MV.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                new NonPrimitiveType(java.lang.Enum.class).internalName(),
                "valueOf",
                "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;",
                false);
        ClassBuilder.cast(MV, type);

        MV.visitInsn(Opcodes.ARETURN); // Always used by enums
        MV.visitMaxs(0, 0); // should be automatically calculated by ClassBuilder's ClassVisitor

        MV.visitEnd();
        CB.visitor().visitEnd();

        // Setups values() enum method
        modifiers = Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC;
        MV = CB.visitor().visitMethod(modifiers, "values", "()" + arrayType.descriptor(), null, new String[0]);
        MV.visitCode();

        MV.visitFieldInsn(
                Opcodes.GETSTATIC,
                type.internalName(),
                "$VALUES",
                arrayType.descriptor());
        MV.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                arrayType.descriptor(),
                "clone",
                "()Ljava/lang/Object;",
                false);
        ClassBuilder.cast(MV, arrayType);

        MV.visitInsn(Opcodes.ARETURN); // Always used by arrays
        MV.visitMaxs(0, 0); // should be automatically calculated by ClassBuilder's ClassVisitor

        MV.visitEnd();
        CB.visitor().visitEnd();

        // Setups $VALUES field
        modifiers = Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL + Opcodes.ACC_SYNTHETIC;
        final FieldVisitor FV = CB.visitor().visitField(modifiers, "$VALUES", arrayType.descriptor(), null, null);
        FV.visitEnd();
        CB.visitor().visitEnd();
    }
}
