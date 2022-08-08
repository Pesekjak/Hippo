package me.pesekjak.hippo.classes.classtypes;

import lombok.Getter;
import me.pesekjak.hippo.classes.ClassType;
import me.pesekjak.hippo.classes.ISkriptClass;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.builder.ClassBuilder;
import me.pesekjak.hippo.classes.builder.IClassBuilder;
import me.pesekjak.hippo.classes.content.Constructor;
import me.pesekjak.hippo.classes.content.ContentType;
import me.pesekjak.hippo.classes.content.Field;
import me.pesekjak.hippo.classes.content.Method;
import me.pesekjak.hippo.classes.types.NonPrimitiveType;
import me.pesekjak.hippo.classes.types.primitives.BooleanType;
import me.pesekjak.hippo.classes.types.primitives.IntType;
import me.pesekjak.hippo.skript.Pair;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkriptRecord extends ISkriptClass {

    @Getter
    private final Map<String, Type> recordParameters = new LinkedHashMap<>();

    public SkriptRecord(Type type) {
        super(type);
        setSuperClass(new NonPrimitiveType(Record.class));
    }

    @Override
    public ClassType getClassType() {
        return ClassType.RECORD;
    }

    @Override
    public boolean illegalModifiers(ContentType contentType, List<Modifier> modifiers) {
        if(contentType == ContentType.FIELD)
            return !modifiers.contains(Modifier.STATIC);
        return modifiers.contains(Modifier.ABSTRACT);
    }

    @Override
    public boolean canHave(ContentType contentType) {
        return contentType == ContentType.FIELD ||
                contentType == ContentType.METHOD ||
                contentType == ContentType.STATIC_BLOCK;
    }

    @Override
    public boolean canExtend() {
        return false;
    }

    @Override
    public boolean setup(final IClassBuilder CB) {
        super.setup(CB);
        getClassContent().values().forEach(classContent -> classContent.setup(CB));

        for(String key : recordParameters.keySet()) {
            CB.visitor().visitRecordComponent(key, recordParameters.get(key).descriptor(), null);
            CB.visitor().visitEnd();
        }

        // Defining record constructor from parameters
        Constructor constructor = new Constructor();
        constructor.addModifiers(Modifier.PUBLIC);
        for(String key : recordParameters.keySet())
            constructor.addArguments(new Pair(key, recordParameters.get(key)));
        int modifiers = ClassBuilder.sumModifiers(getModifiers().toArray(new Modifier[0]));
        final MethodVisitor MV = CB.visitor().visitMethod(modifiers, constructor.getName(), constructor.getDescriptor(), null, new String[0]);

        // Calling record super constructor
        MV.visitCode();
        MV.visitVarInsn(Opcodes.ALOAD, 0);
        MV.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                new NonPrimitiveType(Record.class).internalName(),
                Constructor.METHOD_NAME,
                "()V",
                false);

        int i = 1;
        for(String argumentName : constructor.getArguments().keySet()) {
            Type argument = constructor.getArguments().get(argumentName);
            MV.visitVarInsn(Opcodes.ALOAD, 0);
            MV.visitVarInsn(argument.loadCode(), i);
            if(getClassContent(argumentName) instanceof Field field)
                ClassBuilder.putField(MV, field, getType());
            else return false;
            i += argument.size();
        }

        MV.visitInsn(Opcodes.RETURN); // Constructors should always have void type
        MV.visitMaxs(0, 0); // should be automatically calculated by ClassBuilder's ClassVisitor

        MV.visitEnd();
        CB.visitor().visitEnd();

        setupRecordMethods(CB); // Setups the record getters
        setupDefaultMethods(CB); // Setups the default methods

        return true;
    }

    public void setupRecordMethods(final IClassBuilder CB) {
        for(String parameterName : recordParameters.keySet()) {
            Method method = new Method(parameterName, recordParameters.get(parameterName));
            method.addModifiers(Modifier.PUBLIC);
            int modifiers = ClassBuilder.sumModifiers(getModifiers().toArray(new Modifier[0]));

            MethodVisitor MV = CB.visitor().visitMethod(modifiers, method.getName(), method.getDescriptor(), null, null);

            if(!(getClassContent(method.getName()) instanceof Field field))
                return;

            MV.visitCode();
            MV.visitVarInsn(Opcodes.ALOAD, 0);
            MV.visitFieldInsn(Opcodes.GETFIELD, getType().internalName(), field.getName(), field.getDescriptor());

            MV.visitInsn(field.getType().returnCode());
            MV.visitMaxs(0, 0); // should be automatically calculated by ClassBuilder's ClassVisitor

            MV.visitEnd();
            CB.visitor().visitEnd();
        }
    }

    public void setupDefaultMethods(final IClassBuilder CB) {
        int modifiers = Modifier.PUBLIC.getValue() + Modifier.FINAL.getValue();

        Method equals = new Method("equals", new BooleanType());
        equals.addArguments(new Pair("obj", new NonPrimitiveType(Object.class)));

        Method hashCode = new Method("hashCode", new IntType());

        Method toString = new Method("toString", new NonPrimitiveType(String.class));


        for(Method method : new Method[]{equals, hashCode, toString}) {
            MethodVisitor MV = CB.visitor().visitMethod(modifiers, method.getName(), method.getDescriptor(), null, new String[0]);

            MV.visitCode();
            MV.visitVarInsn(Opcodes.ALOAD, 0);
            int i = 1;
            for(Type argument : method.getArguments().values()) {
                MV.visitVarInsn(argument.loadCode(), i);
                i++;
            }
            String dynamicDescriptor = "(" + getType().descriptor() + method.getDescriptor().replaceFirst("\\(", "");
            MV.visitInvokeDynamicInsn(
                    method.getName(),
                    dynamicDescriptor,
                    getBootstrapHandle(),
                    getBootstrapArguments());

            MV.visitInsn(method.getType().returnCode());
            MV.visitMaxs(0, 0); // should be automatically calculated by ClassBuilder's ClassVisitor

            MV.visitEnd();
            CB.visitor().visitEnd();
        }
    }

    private Handle getBootstrapHandle() {
        return new Handle(
                Opcodes.H_INVOKESTATIC,
                new NonPrimitiveType(ObjectMethods.class).internalName(),
                "bootstrap",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;",
                false);
    }

    private Object[] getBootstrapArguments() {
        List<Object> args = new ArrayList<>();

        args.add(getType().toASM()); // First argument is the record class reference

        List<String> parameterNames = new ArrayList<>(recordParameters.keySet());
        args.add(String.join(";", parameterNames)); // Arguments connected by ';'

        for(String parameterName : recordParameters.keySet()) {
            Handle handle = new Handle(Opcodes.H_GETFIELD, getType().internalName(), parameterName, recordParameters.get(parameterName).descriptor(), false);
            args.add(handle);
        }

        return args.toArray();
    }

}
