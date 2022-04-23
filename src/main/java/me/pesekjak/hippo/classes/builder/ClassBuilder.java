package me.pesekjak.hippo.classes.builder;

import me.pesekjak.hippo.classes.*;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.contents.Field;
import me.pesekjak.hippo.classes.contents.Method;
import me.pesekjak.hippo.classes.contents.annotation.Annotation;
import me.pesekjak.hippo.classes.contents.annotation.AnnotationElement;
import me.pesekjak.hippo.classes.registry.SkriptClassRegistry;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ClassBuilder {

    private final SkriptClass skriptClass;
    private final ClassType classType;
    private String internalName;

    private final ClassWriter cw = new ClassWriter(Opcodes.ASM9 + ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);

    public final static int JAVA_VERSION = Opcodes.V16;

    private final Type REGISTRY_TYPE = new Type(SkriptClassRegistry.class);

    private ClassBuilder(SkriptClass skriptClass) {
        this.skriptClass = skriptClass;
        this.classType = skriptClass.getClassType();
    }

    public static ClassBuilder forClass(SkriptClass skriptClass) {
        return new ClassBuilder(skriptClass);
    }

    public static int sumModifiers(Modifiable modifiable) {
        int result = 0;
        for(Modifier modifier : modifiable.getModifiers()) {
            result = result + modifier.getValue();
        }
        return result;
    }

    public SkriptClass getSkriptClass() {
        return skriptClass;
    }

    public void build() {

        // Handles attributes for class
        int modifiers = classType.getValue() + sumModifiers(skriptClass);
        internalName = skriptClass.getType().getInternalName();
        String superName = "java/lang/Object";
        if(skriptClass.getExtendingTypes().size() != 0) superName = skriptClass.getExtendingTypes().get(0).getInternalName();
        List<String> implementingNames = new ArrayList<>();
        for(Type implementing : skriptClass.getImplementingTypes()) {
            implementingNames.add(implementing.getInternalName());
        }
        for(Annotation annotation : skriptClass.getAnnotations()) {
            AnnotationVisitor av = cw.visitAnnotation(annotation.getType().getDescriptor(), true);
            setupAnnotation(av, annotation);
            av.visitEnd();
        }

        // Handles class
        cw.visit(JAVA_VERSION, modifiers, internalName, null, superName, implementingNames.toArray(new String[0]));

        // Add fields
        skriptClass.getFields().values().forEach(this::addField);

        // Add methods
        skriptClass.getMethods().values().forEach(this::addMethod);

        // Handles init and clinit methods
        generateInit();
        generateClInit();

        // End :)
        cw.visitEnd();

        // Define the class
        if(SkriptReflectHook.getLibraryLoader() == null) SkriptReflectHook.setupReflectLoader();
        SkriptReflectHook.getLibraryLoader().loadClass(skriptClass.getClassName(), cw.toByteArray());

        // Debug
        try {
            DataOutputStream outputStream =new DataOutputStream(new FileOutputStream("Generated.class"));
            outputStream.write(cw.toByteArray());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setupAnnotation(AnnotationVisitor av, Annotation annotation) {
        for(AnnotationElement element : annotation.getElements()) {
            if(element.getConstantArray() == null) {
                if(element.getConstant().getConstantObject() != null) {
                    av.visit(element.getConstantName(), element.getConstant().getConstantObject());
                } else {
                    av.visitEnum(element.getConstantName(), element.getConstant().getType().getDescriptor(), element.getConstant().getPath());
                }
            } else {
                AnnotationVisitor arrayVisitor = av.visitArray(element.getConstantName());
                for(Constant constant : element.getConstantArray().getConstants()) {
                    if(constant.getConstantObject() != null) {
                        arrayVisitor.visit(null, constant.getConstantObject());
                    } else {
                        arrayVisitor.visitEnum(null, constant.getType().getDescriptor(), constant.getPath());
                    }
                }
                arrayVisitor.visitEnd();
            }
        }
    }

    private void addField(Field field) {
        String descriptor = field.getType() != null ? field.getType().getDescriptor() : field.getPrimitiveType().getDescriptor();
        FieldVisitor fv = cw.visitField(sumModifiers(field), field.getName(), descriptor, null, null);
        field.getAnnotations().forEach(annotation -> setupFieldAnnotation(fv, annotation));
        fv.visitEnd();
        cw.visitEnd();
    }

    private void addMethod(Method method) {
        String descriptor = method.getDescriptor();
        List<String> exceptions = new ArrayList<>();
        for(Type exceptionType : method.getExceptions()) {
            exceptions.add(exceptionType.getInternalName());
        }
        int modifiers = sumModifiers(method);
        if(method.hasVarArg()) {
            modifiers =+ Opcodes.ACC_VARARGS;
        }
        MethodVisitor mv = cw.visitMethod(modifiers, method.getName(), descriptor, null, exceptions.size() > 0 ? exceptions.toArray(new String[0]) : null);
        method.getAnnotations().forEach(annotation -> setupMethodAnnotation(mv, annotation));
        if(method.isRunnable()) {
            mv.visitTypeInsn(Opcodes.NEW, "me/pesekjak/hippo/utils/events/classcontents/MethodCallEvent");
            mv.visitInsn(Opcodes.DUP);
            if(!method.getModifiers().contains(Modifier.STATIC)) {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
            } else {
                mv.visitInsn(Opcodes.ACONST_NULL);
            }
            mv.visitLdcInsn(method.getName() + ":" + method.getDescriptor());
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "me/pesekjak/hippo/utils/events/classcontents/MethodCallEvent", "<init>", "(Ljava/lang/Object;Ljava/lang/String;)V", false);
            int eventIndex = method.getArguments().size() + 1;
            mv.visitVarInsn(Opcodes.ASTORE, eventIndex);
            mv.visitVarInsn(Opcodes.ALOAD, eventIndex);
            int i = 0;
            for(Argument argument : method.getArguments()) {
                i++;
                pushValue(mv, i);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                int loadCode = Opcodes.ALOAD;
                switch (argument.getPrimitiveType().getPrimitive()) {
                    case BOOLEAN, BYTE, CHAR, SHORT, INT -> loadCode = Opcodes.ILOAD;
                    case LONG -> loadCode = Opcodes.LLOAD;
                    case FLOAT -> loadCode = Opcodes.FLOAD;
                    case DOUBLE -> loadCode = Opcodes.DLOAD;
                }
                mv.visitVarInsn(loadCode, i);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/utils/events/classcontents/MethodCallEvent", "addArgument", "(Ljava/lang/Number;Ljava/lang/Object;)V", false);
            }
            mv.visitFieldInsn(Opcodes.GETSTATIC, REGISTRY_TYPE.getInternalName(), "REGISTRY", REGISTRY_TYPE.getDescriptor());
            mv.visitLdcInsn(skriptClass.getClassName());
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, REGISTRY_TYPE.getInternalName(), "getSkriptClass", "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/SkriptClass;", false);
            mv.visitLdcInsn(method.getName() + ":" + method.getDescriptor());
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/SkriptClass", "getMethod", "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/contents/Method;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/contents/Method", "getTrigger", "()Lch/njol/skript/lang/Trigger;", false);
            mv.visitVarInsn(Opcodes.ASTORE, eventIndex + 1);
            mv.visitVarInsn(Opcodes.ALOAD, eventIndex + 1);
            mv.visitVarInsn(Opcodes.ALOAD, eventIndex);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "ch/njol/skript/lang/TriggerItem", "walk", "(Lch/njol/skript/lang/TriggerItem;Lorg/bukkit/event/Event;)Z", false);
            mv.visitInsn(Opcodes.POP);
            mv.visitVarInsn(Opcodes.ALOAD, eventIndex);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/utils/events/classcontents/MethodCallEvent", "getOutput", "()Ljava/lang/Object;", false);
            castToType(mv, method.getPrimitiveType(), method.getType());
            int returnCode = Opcodes.ARETURN;
            if(!((method.getType() != null) ? method.getType().isArray() : method.getPrimitiveType().isArray())) {
                switch (method.getPrimitiveType().getPrimitive()) {
                    case BOOLEAN, BYTE, CHAR, SHORT, INT -> returnCode = Opcodes.IRETURN;
                    case LONG -> returnCode = Opcodes.LRETURN;
                    case FLOAT -> returnCode = Opcodes.FRETURN;
                    case DOUBLE -> returnCode = Opcodes.DRETURN;
                    case VOID -> returnCode = Opcodes.RETURN;
                }
            }
            mv.visitInsn(returnCode);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
    }

    private void generateInit() {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        for(Field field : skriptClass.getFields().values()) {
            if(field.getModifiers().contains(Modifier.STATIC)) continue;
            if(field.getConstant() != null) {
                setupConstantField(mv, field);
            } else if(field.getConstantArray() != null) {
                setupConstantArrayField(mv, field);
            } else if(field.getValue() != null) {
                setupValueField(mv, field);
            }
        }
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateClInit() {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        for(Field field : skriptClass.getFields().values()) {
            if(!field.getModifiers().contains(Modifier.STATIC)) continue;
            if(field.getConstant() != null) {
                setupConstantField(mv, field);
            } else if(field.getConstantArray() != null) {
                setupConstantArrayField(mv, field);
            } else if(field.getValue() != null) {
                setupValueField(mv, field);
            }
        }
        mv.visitFieldInsn(Opcodes.GETSTATIC, REGISTRY_TYPE.getInternalName(), "REGISTRY", REGISTRY_TYPE.getDescriptor());
        mv.visitLdcInsn(skriptClass.getClassName());
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, REGISTRY_TYPE.getInternalName(), "getSkriptClass", "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/SkriptClass;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/SkriptClass", "runStaticInitialization", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void setupConstantField(MethodVisitor mv, Field field) {
        if(!field.getModifiers().contains(Modifier.STATIC)) mv.visitVarInsn(Opcodes.ALOAD, 0);
        if(field.getConstant().getConstantObject() != null) {
            mv.visitLdcInsn(field.getConstant().getConstantObject(field.getPrimitiveType().getPrimitive()));
        } else {
            mv.visitFieldInsn(Opcodes.GETSTATIC, field.getConstant().getType().getInternalName(), field.getConstant().getPath(), field.getConstant().getType().getDescriptor());
        }
        putField(mv, field);
    }

    private void setupConstantArrayField(MethodVisitor mv, Field field) {
        if(!field.getModifiers().contains(Modifier.STATIC)) mv.visitVarInsn(Opcodes.ALOAD, 0);
        pushValue(mv, field.getConstantArray().getConstants().size());
        if(field.getType() != null) {
            mv.visitTypeInsn(Opcodes.ANEWARRAY, field.getType().getInternalName());
        } else {
            mv.visitIntInsn(Opcodes.NEWARRAY, field.getPrimitiveType().getPrimitive().getTypeValue());
        }
        int i = 0;
        for(Constant constant : field.getConstantArray().getConstants()) {
            mv.visitInsn(Opcodes.DUP);
            pushValue(mv, i);
            if(constant.getConstantObject() != null) {
                pushValue(mv, constant.getConstantObject(field.getPrimitiveType().getPrimitive()));
            } else {
                mv.visitFieldInsn(Opcodes.GETSTATIC, constant.getType().getInternalName(), constant.getPath(), constant.getType().getDescriptor());
            }
            int storeCode = Opcodes.AASTORE;
            switch (field.getPrimitiveType().getPrimitive()) {
                case BOOLEAN, BYTE -> storeCode = Opcodes.BASTORE;
                case CHAR -> storeCode = Opcodes.CASTORE;
                case SHORT -> storeCode = Opcodes.SASTORE;
                case INT -> storeCode = Opcodes.IASTORE;
                case FLOAT -> storeCode = Opcodes.FASTORE;
                case LONG -> storeCode = Opcodes.LASTORE;
                case DOUBLE -> storeCode = Opcodes.DASTORE;
            }
            mv.visitInsn(storeCode);
            i++;
        }
        putField(mv, field);
    }

    private void setupValueField(MethodVisitor mv, Field field) {
        if(!field.getModifiers().contains(Modifier.STATIC)) mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETSTATIC, REGISTRY_TYPE.getInternalName(), "REGISTRY", REGISTRY_TYPE.getDescriptor());
        mv.visitLdcInsn(skriptClass.getClassName());
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, REGISTRY_TYPE.getInternalName(), "getSkriptClass", "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/SkriptClass;", false);
        mv.visitLdcInsn(field.getName() + ":" + field.getDescriptor());
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/SkriptClass", "getField", "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/contents/Field;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/contents/Field", "getValue", "()Lch/njol/skript/lang/Expression;", false);
        mv.visitFieldInsn(Opcodes.GETSTATIC, REGISTRY_TYPE.getInternalName(), "REGISTRY", REGISTRY_TYPE.getDescriptor());
        mv.visitLdcInsn(skriptClass.getClassName());
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, REGISTRY_TYPE.getInternalName(), "getSkriptClass", "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/SkriptClass;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/SkriptClass", "getDefineEvent", "()Lorg/bukkit/event/Event;", false);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "ch/njol/skript/lang/Expression", "getSingle", "(Lorg/bukkit/event/Event;)Ljava/lang/Object;", true);
        mv.visitVarInsn(Opcodes.ASTORE, 1);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitTypeInsn(Opcodes.INSTANCEOF, "com/btk5h/skriptmirror/ObjectWrapper");
        Label label = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, label);
        if(!field.getModifiers().contains(Modifier.STATIC)) mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/btk5h/skriptmirror/ObjectWrapper", "unwrapIfNecessary", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
        castToType(mv, field.getPrimitiveType(), field.getType());
        putField(mv, field);
        Label end = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, end);
        mv.visitLabel(label);
        if(!field.getModifiers().contains(Modifier.STATIC)) mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        castToType(mv, field.getPrimitiveType(), field.getType());
        putField(mv, field);
        mv.visitLabel(end);
    }

    public void putField(MethodVisitor mv, Field field) {
        int opcode = field.getModifiers().contains(Modifier.STATIC) ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD;
        mv.visitFieldInsn(opcode, internalName, field.getName(), field.getDescriptor());
    }

    private void setupFieldAnnotation(FieldVisitor fv, Annotation annotation) {
        AnnotationVisitor av = fv.visitAnnotation(annotation.getType().getDescriptor(), true);
        setupAnnotation(av, annotation);
        av.visitEnd();
    }

    private void setupMethodAnnotation(MethodVisitor mv, Annotation annotation) {
        AnnotationVisitor av = mv.visitAnnotation(annotation.getType().getDescriptor(), true);
        setupAnnotation(av, annotation);
        av.visitEnd();
    }

    private void pushValue(MethodVisitor mv, Object value) {
        double number = 0;
        if(value instanceof Boolean) {
            if(!(Boolean) value) number = 0;
            if((Boolean) value) number = 1;
        } else if(value instanceof Character) {
            number = (Character) value;
        } else if(value instanceof Number){
            number = ((Number) value).doubleValue();
        } else {
            mv.visitLdcInsn(value);
            return;
        }
        if(0 <= number && number <= 5 && number % 1 == 0) {
            mv.visitInsn(((Number) (Opcodes.ICONST_0 + number)).intValue());
        } else if(Byte.MIN_VALUE <= number && number <= Byte.MAX_VALUE) {
            mv.visitIntInsn(Opcodes.BIPUSH, ((Number) value).intValue());
        } else if(Short.MIN_VALUE <= number && number <= Short.MAX_VALUE) {
            mv.visitIntInsn(Opcodes.SIPUSH, ((Number) value).intValue());
        }
    }

    private void pushAsPrimitive(MethodVisitor mv, Primitive primitive) {
        Type counterType = new Type(primitive.getClassCounterpart());
        if(Number.class.isAssignableFrom(counterType.findClass())) {
            counterType = new Type(Number.class);
        }
        mv.visitTypeInsn(Opcodes.CHECKCAST, counterType.getInternalName());
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, counterType.getInternalName(), primitive.getPrimitive() + "Value", "()" + primitive.getDescriptor(), false);
    }

    private void castToType(MethodVisitor mv, PrimitiveType primitiveType, @Nullable Type type) {
        boolean isArray = (type != null) ? type.isArray() : primitiveType.isArray();
        if(!isArray) {
            if (type != null) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, type.getInternalName());
            } else if (primitiveType.getPrimitive() != Primitive.VOID) {
                pushAsPrimitive(mv, primitiveType.getPrimitive());
            }
        } else {
            mv.visitTypeInsn(Opcodes.CHECKCAST, (type != null) ? type.getDescriptor() : primitiveType.getDescriptor());
        }
    }

}
