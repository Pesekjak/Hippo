package me.pesekjak.hippo.classes.builder;

import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.*;
import me.pesekjak.hippo.classes.classtypes.TypeEnum;
import me.pesekjak.hippo.classes.classtypes.TypeRecord;
import me.pesekjak.hippo.classes.contents.Constructor;
import me.pesekjak.hippo.classes.contents.Enum;
import me.pesekjak.hippo.classes.contents.Field;
import me.pesekjak.hippo.classes.contents.Method;
import me.pesekjak.hippo.classes.contents.annotation.Annotation;
import me.pesekjak.hippo.classes.contents.annotation.AnnotationElement;
import me.pesekjak.hippo.classes.registry.SkriptClassRegistry;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.preimport.PreImport;
import me.pesekjak.hippo.preimport.PreImportManager;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClassBuilder {

    private final SkriptClass skriptClass;
    private final ClassType classType;
    private String internalName;

    private final ClassWriter cw = new ClassWriter(ASM_VERSION + ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);

    public static int JAVA_VERSION = Opcodes.V16;
    public static int ASM_VERSION = Opcodes.ASM9;

    private final Type REGISTRY_TYPE = new Type(SkriptClassRegistry.class);

    private ClassBuilder(SkriptClass skriptClass) {
        this.skriptClass = skriptClass;
        this.classType = skriptClass.getClassType();
    }

    public static ClassBuilder forClass(SkriptClass skriptClass) {
        return new ClassBuilder(skriptClass);
    }

    public SkriptClass getSkriptClass() {
        return skriptClass;
    }

    public void build() {

        if(SkriptReflectHook.getLibraryLoader().tryFindClass(skriptClass.getClassName()) != null) {
            return;
        }

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

        // Handles records
        if(skriptClass instanceof TypeRecord recordClass) {
            for(Argument argument : recordClass.getRecordConstructorArguments()) {
                String descriptor = argument.getDescriptor();
                cw.visitRecordComponent(argument.getName(), descriptor, null).visitEnd();
                Field field = new Field(argument.getPrimitiveType(), argument.getType(), argument.getName());
                field.addModifier(Modifier.PRIVATE);
                field.addModifier(Modifier.FINAL);
                addField(field);
            }
            RecordBuilder recordBuilder = new RecordBuilder(this);
            recordBuilder.setupConstructor();
            recordBuilder.setupRecordMethods();
            recordBuilder.generateObjectMethods();
        }

        // Add methods
        skriptClass.getMethods().values().forEach(this::addMethod);

        // Handles clinit method
        generateClInit();

        // Handles init methods
        if(classType != ClassType.INTERFACE && classType != ClassType.ANNOTATION && classType != ClassType.RECORD) {
            if(skriptClass.getConstructors().keySet().size() == 0) {
                if(classType != ClassType.ENUM) skriptClass.addConstructor("<init>:()V", Constructor.getDefault());
                else skriptClass.addConstructor("<init>:(Ljava/lang/String;I)V", Constructor.getDefaultEnumConstructor());
            }
            for(Constructor constructor : skriptClass.getConstructors().values()) {
                addConstructor(constructor);
            }
        }

        if(classType == ClassType.ENUM) {
            EnumBuilder enumBuilder = new EnumBuilder(this);
            enumBuilder.setupField$VALUES();
            enumBuilder.setupMethod$values();
            enumBuilder.setupMethodValues();
            enumBuilder.setupMethodValueOf();
        }

        // End :)
        cw.visitEnd();

        // Define the class
        Class<?> compiled = SkriptReflectHook.getLibraryLoader().loadClass(skriptClass.getClassName(), cw.toByteArray());
        for(String path : PreImportManager.MANAGER.getPreImportingScripts().keySet()) {
            PreImportManager.PreImporting preImporting = PreImportManager.MANAGER.getPreImporting(path);
            for(String alias : preImporting.getPreImportMap().keySet()) {
                PreImport preImport = preImporting.getPreImport(alias);
                if(!skriptClass.getType().getDotPath().equals(preImport.getType().getDotPath())) {
                    continue;
                }
                SkriptReflectHook.getReflectCustomImportsMap().get(new File(path)).put(alias, SkriptReflectHook.buildJavaType(compiled));
                break;
            }
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
        int modifiers = sumModifiers(field);
        if(field instanceof Enum) modifiers += Opcodes.ACC_ENUM;
        FieldVisitor fv = cw.visitField(modifiers, field.getName(), descriptor, null, null);
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
        if(method.hasVarArg()) modifiers += Opcodes.ACC_VARARGS;
        MethodVisitor mv = cw.visitMethod(modifiers, method.getName(), descriptor, null, exceptions.size() > 0 ? exceptions.toArray(new String[0]) : null);
        method.getAnnotations().forEach(annotation -> setupMethodAnnotation(mv, annotation));
        if(classType == ClassType.ANNOTATION) {
            AnnotationVisitor defaultAV;
            if(method.getDefaultConstant() != null) {
                Constant defaultConstant = method.getDefaultConstant();
                defaultAV = mv.visitAnnotationDefault();
                if(defaultConstant.getConstantObject() != null) {
                    defaultAV.visit(null, defaultConstant.getConstantObject());
                } else {
                    defaultAV.visitEnum(null, defaultConstant.getType().getDescriptor(), defaultConstant.getPath());
                }
                defaultAV.visitEnd();
            } else if(method.getDefaultConstantArray() != null) {
                ConstantArray defaultConstantArray = method.getDefaultConstantArray();
                defaultAV = mv.visitAnnotationDefault();
                AnnotationVisitor defaultArrayAV = defaultAV.visitArray(null);
                for(Constant constant : defaultConstantArray.getConstants()) {
                    if(constant.getConstantObject() != null) {
                        defaultArrayAV.visit(null, constant.getConstantObject());
                    } else {
                        defaultArrayAV.visitEnum(null, constant.getType().getDescriptor(), constant.getPath());
                    }
                }
                defaultArrayAV.visitEnd();
                defaultAV.visitEnd();
            }
        }
        if(method.isRunnable()) {
            mv.visitCode();
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
            if(method.getModifiers().contains(Modifier.STATIC)) eventIndex -= 1;
            mv.visitVarInsn(Opcodes.ASTORE, eventIndex);
            int i = 0;
            for(Argument argument : method.getArguments()) {
                mv.visitVarInsn(Opcodes.ALOAD, eventIndex);
                i++;
                pushValue(mv, i);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                int loadCode = getLoadCode(argument.getPrimitiveType());
                if(!method.getModifiers().contains(Modifier.STATIC)) {
                    mv.visitVarInsn(loadCode, i);
                } else {
                    mv.visitVarInsn(loadCode, i - 1);
                }
                if(argument.getPrimitiveType().getPrimitive() != Primitive.NONE && !argument.getPrimitiveType().isArray()) pushAsType(mv, argument.getPrimitiveType().getPrimitive());
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/utils/events/classcontents/MethodCallEvent", "addArgument", "(Ljava/lang/Number;Ljava/lang/Object;)V", false);
            }
            pushClassInstance(mv);
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
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/btk5h/skriptmirror/ObjectWrapper", "unwrapIfNecessary", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
            if(method.getPrimitiveType().getPrimitive() == Primitive.NONE && !method.getType().isArray()) {
                Label nullLabel = new Label();
                mv.visitJumpInsn(Opcodes.IFNULL, nullLabel);
                mv.visitVarInsn(Opcodes.ALOAD, eventIndex);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/utils/events/classcontents/MethodCallEvent", "getOutput", "()Ljava/lang/Object;", false);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/btk5h/skriptmirror/ObjectWrapper", "unwrapIfNecessary", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                if (method.getType() != null && Number.class.isAssignableFrom(method.getType().findClass())) convertNumber(mv, method.getType());
                castToType(mv, method.getPrimitiveType(), method.getType());
                Label end = new Label();
                mv.visitJumpInsn(Opcodes.GOTO, end);
                mv.visitLabel(nullLabel);
                mv.visitInsn(Opcodes.ACONST_NULL);
                mv.visitLabel(end);
            } else {
                castToType(mv, method.getPrimitiveType(), method.getType());
            }
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
            mv.visitMaxs(0, 0);
        }
        mv.visitEnd();
        cw.visitEnd();
    }

    private void addConstructor(Constructor constructor) {
        String descriptor = constructor.getDescriptor();
        List<String> exceptions = new ArrayList<>();
        for(Type exceptionType : constructor.getExceptions()) {
            exceptions.add(exceptionType.getInternalName());
        }
        int modifiers = sumModifiers(constructor);
        if(constructor.hasVarArg()) modifiers += Opcodes.ACC_VARARGS;
        MethodVisitor mv = cw.visitMethod(modifiers, constructor.getName(), descriptor, null, exceptions.size() > 0 ? exceptions.toArray(new String[0]) : null);
        constructor.getAnnotations().forEach(annotation -> setupMethodAnnotation(mv, annotation));
        mv.visitCode();
        int argumentOffset = constructor.getArguments().size();
        mv.visitTypeInsn(Opcodes.NEW, "me/pesekjak/hippo/utils/events/classcontents/constructors/InitEvent");
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "me/pesekjak/hippo/utils/events/classcontents/constructors/InitEvent", "<init>", "()V", false);
        mv.visitVarInsn(Opcodes.ASTORE, 1 + argumentOffset);
        int i = 0;
        for (Argument argument : constructor.getArguments()) {
            i++;
            if(classType == ClassType.ENUM && (i == 1 || i == 2)) continue;
            mv.visitVarInsn(Opcodes.ALOAD, 1 + argumentOffset);
            pushValue(mv, (classType != ClassType.ENUM) ? i : i - 2);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            int loadCode = getLoadCode(argument.getPrimitiveType());
            mv.visitVarInsn(loadCode, i);
            if(argument.getPrimitiveType().getPrimitive() != Primitive.NONE && !argument.getPrimitiveType().isArray()) pushAsType(mv, argument.getPrimitiveType().getPrimitive());
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/utils/events/classcontents/constructors/InitEvent", "addArgument", "(Ljava/lang/Number;Ljava/lang/Object;)V", false);
        }
        pushClassInstance(mv);
        mv.visitLdcInsn(constructor.getName() + ":" + constructor.getDescriptor());
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/SkriptClass", "getConstructor", "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/contents/Constructor;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/contents/Constructor", "getTrigger", "()Lch/njol/skript/lang/Trigger;", false);
        mv.visitVarInsn(Opcodes.ASTORE, 2 + argumentOffset);
        mv.visitVarInsn(Opcodes.ALOAD, 2 + argumentOffset);
        mv.visitVarInsn(Opcodes.ALOAD, 1 + argumentOffset);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "ch/njol/skript/lang/TriggerItem", "walk", "(Lch/njol/skript/lang/TriggerItem;Lorg/bukkit/event/Event;)Z", false);
        mv.visitInsn(Opcodes.POP);
        if(classType != ClassType.ENUM) { // Enums super is handled separately and automatically
            i = 0;
            for(Argument argument : constructor.getSuperArguments()) {
                mv.visitVarInsn(Opcodes.ALOAD, 1 + argumentOffset);
                i++;
                pushValue(mv, i);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/utils/events/classcontents/constructors/InitEvent", "getSuperResult", "(Ljava/lang/Number;)Ljava/lang/Object;", false);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/btk5h/skriptmirror/ObjectWrapper", "unwrapIfNecessary", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                if(argument.getPrimitiveType().getPrimitive() == Primitive.NONE && !argument.getType().isArray()) {
                    Label nullLabel = new Label();
                    mv.visitJumpInsn(Opcodes.IFNULL, nullLabel);
                    mv.visitVarInsn(Opcodes.ALOAD, 1 + argumentOffset);
                    pushValue(mv, i);
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/utils/events/classcontents/constructors/InitEvent", "getSuperResult", "(Ljava/lang/Number;)Ljava/lang/Object;", false);
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/btk5h/skriptmirror/ObjectWrapper", "unwrapIfNecessary", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                    if (argument.getType() != null && Number.class.isAssignableFrom(argument.getType().findClass())) convertNumber(mv, argument.getType());
                    castToType(mv, argument.getPrimitiveType(), argument.getType());
                    Label end = new Label();
                    mv.visitJumpInsn(Opcodes.GOTO, end);
                    mv.visitLabel(nullLabel);
                    mv.visitInsn(Opcodes.ACONST_NULL);
                    mv.visitLabel(end);
                } else if(argument.getType() == null && !argument.getPrimitiveType().isArray()) {
                    pushAsPrimitive(mv, argument.getPrimitiveType().getPrimitive());
                } else {
                    castToType(mv, argument.getPrimitiveType(), argument.getType());
                }
                int storeCode = Opcodes.ASTORE;
                if(!(argument.getType() != null ? argument.getType().isArray() : argument.getPrimitiveType().isArray())) {
                    switch (argument.getPrimitiveType().getPrimitive()) {
                        case BOOLEAN, BYTE, CHAR, SHORT, INT -> storeCode = Opcodes.ISTORE;
                        case LONG -> storeCode = Opcodes.LSTORE;
                        case FLOAT -> storeCode = Opcodes.FSTORE;
                        case DOUBLE -> storeCode = Opcodes.DSTORE;
                    }
                }
                mv.visitVarInsn(storeCode, i + 2 + argumentOffset);
            }
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            i = 0;
            for(Argument argument : constructor.getSuperArguments()) {
                i++;
                int loadCode = getLoadCode(argument.getPrimitiveType());
                mv.visitVarInsn(loadCode, i + 2 + argumentOffset);
            }
            String superName = "java/lang/Object";
            if(skriptClass.getExtendingTypes().size() != 0) superName = skriptClass.getExtendingTypes().get(0).getInternalName();
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", constructor.getSuperDescriptor(), false);
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ILOAD, 2);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL,"java/lang/Enum", "<init>", "(Ljava/lang/String;I)V", false);
        }
        for(Field field : skriptClass.getFields().values()) {
            if(field.getModifiers().contains(Modifier.STATIC)) continue;
            if(field.getConstant() != null) {
                setupConstantField(mv, field);
            } else if(field.getConstantArray() != null) {
                setupConstantArrayField(mv, field);
            } else if(field.getValue() != null) {
                setupValueField(mv, field, argumentOffset + 2);
            }
        }
        mv.visitTypeInsn(Opcodes.NEW, "me/pesekjak/hippo/utils/events/classcontents/constructors/PostInitEvent");
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "me/pesekjak/hippo/utils/events/classcontents/constructors/PostInitEvent", "<init>", "(Ljava/lang/Object;)V", false);
        mv.visitVarInsn(Opcodes.ASTORE, 3 + argumentOffset);
        i = 0;
        for(Argument argument : constructor.getArguments()) {
            i++;
            if(classType == ClassType.ENUM && (i == 1 || i == 2)) continue;
            mv.visitVarInsn(Opcodes.ALOAD, 3 + argumentOffset);
            pushValue(mv, (classType != ClassType.ENUM) ? i : i - 2);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            int loadCode = getLoadCode(argument.getPrimitiveType());
            mv.visitVarInsn(loadCode, i);
            if(argument.getPrimitiveType().getPrimitive() != Primitive.NONE && !argument.getPrimitiveType().isArray()) pushAsType(mv, argument.getPrimitiveType().getPrimitive());
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/utils/events/classcontents/constructors/PostInitEvent", "addArgument", "(Ljava/lang/Number;Ljava/lang/Object;)V", false);
        }
        pushClassInstance(mv);
        mv.visitLdcInsn(constructor.getName() + ":" + constructor.getDescriptor());
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/SkriptClass", "getConstructor", "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/contents/Constructor;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/contents/Constructor", "getPostInitTrigger", "()Lch/njol/skript/lang/Trigger;", false);
        mv.visitVarInsn(Opcodes.ASTORE, 4 + argumentOffset);
        mv.visitVarInsn(Opcodes.ALOAD, 4 + argumentOffset);
        mv.visitVarInsn(Opcodes.ALOAD, 3 + argumentOffset);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "ch/njol/skript/lang/TriggerItem", "walk", "(Lch/njol/skript/lang/TriggerItem;Lorg/bukkit/event/Event;)Z", false);
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
    }

    private void generateClInit() {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        int stackOffset = 0;
        if(classType == ClassType.ENUM) {
            new EnumBuilder(this).setupEnums(mv);
            stackOffset += 2;
        }
        for(Field field : skriptClass.getFields().values()) {
            if(!field.getModifiers().contains(Modifier.STATIC)) continue;
            if(field instanceof Enum) continue;
            if(field.getConstant() != null) {
                setupConstantField(mv, field);
            } else if(field.getConstantArray() != null) {
                setupConstantArrayField(mv, field);
            } else if(field.getValue() != null) {
                setupValueField(mv, field, stackOffset);
            }
        }
        pushClassInstance(mv);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/SkriptClass", "runStaticInitialization", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
    }

    private void setupConstantField(MethodVisitor mv, Field field) {
        if(!field.getModifiers().contains(Modifier.STATIC)) mv.visitVarInsn(Opcodes.ALOAD, 0);
        if(field.getConstant().getConstantObject() != null) {
            if(field.getType() == null) {
                mv.visitLdcInsn(field.getConstant().getConstantObject(field.getPrimitiveType().getPrimitive()));
            } else {
                Object constant = field.getConstant().getConstantObject();
                mv.visitLdcInsn(constant);
                if(!(constant instanceof String)) {
                    Primitive counterPrimitive = Primitive.NONE;
                    for(Primitive primitive : Primitive.values()) {
                        if(constant.getClass().isAssignableFrom(primitive.getClassCounterpart())) {
                            counterPrimitive = primitive;
                            break;
                        }
                    }
                    pushAsType(mv, counterPrimitive);
                }
                if (Number.class.isAssignableFrom(field.getType().findClass())) convertNumber(mv, field.getType());
                castToType(mv, field.getPrimitiveType(), field.getType());
            }
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
                Object constantObject = constant.getConstantObject();
                if(field.getType() == null) {
                    mv.visitLdcInsn(constant.getConstantObject(field.getPrimitiveType().getPrimitive()));
                } else {
                    mv.visitLdcInsn(constantObject);
                    if(!(constantObject instanceof String)) {
                        Primitive counterPrimitive = Primitive.NONE;
                        for(Primitive primitive : Primitive.values()) {
                            if(constantObject.getClass().isAssignableFrom(primitive.getClassCounterpart())) {
                                counterPrimitive = primitive;
                                break;
                            }
                        }
                        pushAsType(mv, counterPrimitive);
                    }
                    if (Number.class.isAssignableFrom(field.getType().findClass())) convertNumber(mv, field.getType());
                    castToType(mv, new PrimitiveType(Primitive.NONE), new Type(field.getType().getDotPath()));
                }
            } else {
                mv.visitFieldInsn(Opcodes.GETSTATIC, constant.getType().getInternalName(), constant.getPath(), constant.getType().getDescriptor());
            }
            int storeCode = Opcodes.AASTORE;
            if(field.getType() == null) {
                switch (field.getPrimitiveType().getPrimitive()) {
                    case BOOLEAN, BYTE -> storeCode = Opcodes.BASTORE;
                    case CHAR -> storeCode = Opcodes.CASTORE;
                    case SHORT -> storeCode = Opcodes.SASTORE;
                    case INT -> storeCode = Opcodes.IASTORE;
                    case FLOAT -> storeCode = Opcodes.FASTORE;
                    case LONG -> storeCode = Opcodes.LASTORE;
                    case DOUBLE -> storeCode = Opcodes.DASTORE;
                }
            }
            mv.visitInsn(storeCode);
            i++;
        }
        putField(mv, field);
    }

    private void setupValueField(MethodVisitor mv, Field field, int stackOffset) {
        if(!field.getModifiers().contains(Modifier.STATIC)) mv.visitVarInsn(Opcodes.ALOAD, 0);
        pushClassInstance(mv);
        mv.visitLdcInsn(field.getName());
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/SkriptClass", "getField", "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/contents/Field;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/contents/Field", "getValue", "()Lch/njol/skript/lang/Expression;", false);
        pushClassInstance(mv);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/SkriptClass", "getDefineEvent", "()Lorg/bukkit/event/Event;", false);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "ch/njol/skript/lang/Expression", "getSingle", "(Lorg/bukkit/event/Event;)Ljava/lang/Object;", true);
        mv.visitVarInsn(Opcodes.ASTORE, 1 + stackOffset);
        mv.visitVarInsn(Opcodes.ALOAD, 1 + stackOffset);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/btk5h/skriptmirror/ObjectWrapper", "unwrapIfNecessary", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
        Label nullLabel = new Label();
        Label end = null;
        if(field.getPrimitiveType().getPrimitive() == Primitive.NONE && !field.getType().isArray()) {
            mv.visitJumpInsn(Opcodes.IFNULL, nullLabel);
            if(!field.getModifiers().contains(Modifier.STATIC)) mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1 + stackOffset);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/btk5h/skriptmirror/ObjectWrapper", "unwrapIfNecessary", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
            if (field.getType() != null && Number.class.isAssignableFrom(field.getType().findClass())) convertNumber(mv, field.getType());
            castToType(mv, field.getPrimitiveType(), field.getType());
            putField(mv, field);
            end = new Label();
            mv.visitJumpInsn(Opcodes.GOTO, end);
            mv.visitLabel(nullLabel);
            if (!field.getModifiers().contains(Modifier.STATIC)) mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitInsn(Opcodes.ACONST_NULL);
        } else if(field.getType() == null && !field.getPrimitiveType().isArray()) {
            pushAsPrimitive(mv, field.getPrimitiveType().getPrimitive());
        } else {
            castToType(mv, field.getPrimitiveType(), field.getType());
        }
        putField(mv, field);
        if(end != null) mv.visitLabel(end);
    }

    public int sumModifiers(Modifiable modifiable) {
        int result = 0;
        for(Modifier modifier : modifiable.getModifiers()) {
            result = result + modifier.getValue();
        }
        return result;
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

    private void pushAsType(MethodVisitor mv, Primitive primitive) {
        Type counterType = new Type(primitive.getClassCounterpart());
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, counterType.getInternalName(), "valueOf", "(" + primitive.getDescriptor() + ")" + counterType.getDescriptor(), false);
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

    private void convertNumber(MethodVisitor mv, Type endType) {
        Primitive counterPrimitive = Primitive.NONE;
        for(Primitive primitive : Primitive.values()) {
            if(primitive.getClassCounterpart() == endType.findClass()) {
                counterPrimitive = primitive;
                break;
            }
        }
        if(counterPrimitive == Primitive.NONE) return;
        pushAsPrimitive(mv, counterPrimitive);
        pushAsType(mv, counterPrimitive);
    }

    public void pushClassInstance(MethodVisitor mv) {
        mv.visitFieldInsn(Opcodes.GETSTATIC, REGISTRY_TYPE.getInternalName(), "REGISTRY", REGISTRY_TYPE.getDescriptor());
        mv.visitLdcInsn(skriptClass.getClassName());
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, REGISTRY_TYPE.getInternalName(), "getSkriptClass", "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/SkriptClass;", false);
    }

    public int getLoadCode(PrimitiveType primitiveType) {
        int loadCode = Opcodes.ALOAD;
        if(!primitiveType.isArray()) {
            switch (primitiveType.getPrimitive()) {
                case BOOLEAN, BYTE, CHAR, SHORT, INT -> loadCode = Opcodes.ILOAD;
                case LONG -> loadCode = Opcodes.LLOAD;
                case FLOAT -> loadCode = Opcodes.FLOAD;
                case DOUBLE -> loadCode = Opcodes.DLOAD;
            }
        }
        return loadCode;
    }

    private static class EnumBuilder {

        private final ClassBuilder cb;

        private final SkriptClass skriptClass;
        private final ClassType classType;
        private String internalName;

        private final ClassWriter cw;


        public EnumBuilder(ClassBuilder cb) {
            this.cb = cb;
            this.skriptClass = cb.skriptClass;
            this.classType = cb.classType;
            this.internalName = cb.internalName;
            this.cw = cb.cw;
        }

        public void setupField$VALUES() {
            int modifiers = Modifier.PRIVATE.value + Modifier.STATIC.value + Modifier.FINAL.value + Opcodes.ACC_SYNTHETIC;
            FieldVisitor fv = cw.visitField(modifiers, "$VALUES", skriptClass.getType().arrayType().getDescriptor(), null, null);
            fv.visitEnd();
            cw.visitEnd();
        }

        public void setupMethodValues() {
            int modifiers = Modifier.PUBLIC.value + Modifier.STATIC.value;
            MethodVisitor mv = cw.visitMethod(modifiers, "values", "()" + skriptClass.getType().arrayType().getDescriptor(), null, null);
            mv.visitCode();
            mv.visitFieldInsn(Opcodes.GETSTATIC, internalName, "$VALUES", skriptClass.getType().arrayType().getDescriptor());
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, skriptClass.getType().arrayType().getDescriptor(), "clone", "()Ljava/lang/Object;", false);
            cb.castToType(mv, new PrimitiveType(Primitive.NONE), skriptClass.getType().arrayType());
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            cw.visitEnd();
        }

        public void setupMethodValueOf() {
            int modifiers = Modifier.PUBLIC.value + Modifier.STATIC.value;
            MethodVisitor mv = cw.visitMethod(modifiers, "valueOf", "(Ljava/lang/String;)" + skriptClass.getType().getDescriptor(), null, null);
            mv.visitCode();
            mv.visitLdcInsn(skriptClass.getType().toASMType());
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Enum", "valueOf", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;", false);
            cb.castToType(mv, new PrimitiveType(Primitive.NONE), skriptClass.getType());
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            cw.visitEnd();
        }

        public void setupMethod$values() {
            List<Enum> enums = ((TypeEnum) skriptClass).getEnumFields();
            int modifiers = Modifier.PRIVATE.value + Modifier.STATIC.value + Opcodes.ACC_SYNTHETIC;
            MethodVisitor mv = cw.visitMethod(modifiers, "$values", "()" + skriptClass.getType().arrayType().getDescriptor(), null, null);
            mv.visitCode();
            cb.pushValue(mv, enums.size());
            mv.visitTypeInsn(Opcodes.ANEWARRAY, internalName);
            int i = 0;
            for(Enum enumField : enums) {
                mv.visitInsn(Opcodes.DUP);
                cb.pushValue(mv, i);
                mv.visitFieldInsn(Opcodes.GETSTATIC, internalName, enumField.getName(), enumField.getDescriptor());
                mv.visitInsn(Opcodes.AASTORE);
                i++;
            }
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            cw.visitEnd();
        }

        // Has to be MethodVisitor for static block
        public void setupEnums(MethodVisitor mv) {
            List<Enum> enums = ((TypeEnum) skriptClass).getEnumFields();
            cb.pushClassInstance(mv);
            mv.visitVarInsn(Opcodes.ASTORE, 0);
            int i = 0;
            for(Enum enumField : enums) {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitLdcInsn(enumField.getName());
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/SkriptClass", "getField", "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/contents/Field;", false);
                cb.castToType(mv, new PrimitiveType(Primitive.NONE), new Type(Enum.class));
                mv.visitVarInsn(Opcodes.ASTORE, 1);
                mv.visitTypeInsn(Opcodes.NEW, internalName);
                mv.visitInsn(Opcodes.DUP);
                mv.visitLdcInsn(enumField.getName());
                cb.pushValue(mv, i);
                int argumentIndex = 0;
                for(Argument argument : enumField.getSuperArguments()) {
                    argumentIndex++;
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/SkriptClass", "getDefineEvent", "()Lorg/bukkit/event/Event;", false);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/contents/Enum", "getAllFromExpression", "(Lorg/bukkit/event/Event;)[Ljava/lang/Object;", false);
                    cb.pushValue(mv, argumentIndex - 1);
                    mv.visitInsn(Opcodes.AALOAD);
                    mv.visitVarInsn(Opcodes.ASTORE, 1 + argumentIndex);
                }
                argumentIndex = 0;
                for(Argument argument : enumField.getSuperArguments()) {
                    argumentIndex++;
                    mv.visitVarInsn(Opcodes.ALOAD, 1 + argumentIndex);
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/btk5h/skriptmirror/ObjectWrapper", "unwrapIfNecessary", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                    if(argument.getPrimitiveType().getPrimitive() == Primitive.NONE && !argument.getType().isArray()) {
                        Label nullLabel = new Label();
                        mv.visitJumpInsn(Opcodes.IFNULL, nullLabel);
                        mv.visitVarInsn(Opcodes.ALOAD, 1 + argumentIndex);
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/btk5h/skriptmirror/ObjectWrapper", "unwrapIfNecessary", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                        if (argument.getType() != null && Number.class.isAssignableFrom(argument.getType().findClass()))
                            cb.convertNumber(mv, argument.getType());
                        cb.castToType(mv, argument.getPrimitiveType(), argument.getType());
                        Label end = new Label();
                        mv.visitJumpInsn(Opcodes.GOTO, end);
                        mv.visitLabel(nullLabel);
                        mv.visitInsn(Opcodes.ACONST_NULL);
                        mv.visitLabel(end);
                    } else {
                        cb.castToType(mv, argument.getPrimitiveType(), argument.getType());
                    }
                }
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, internalName, "<init>", enumField.getSuperDescriptor(), false);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, internalName, enumField.getName(), enumField.getDescriptor());
                i++;
            }
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, internalName, "$values", "()" + skriptClass.getType().arrayType().getDescriptor(), false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, internalName, "$VALUES", skriptClass.getType().arrayType().getDescriptor());
        }

    }

    private static class RecordBuilder {

        private final ClassBuilder cb;

        private final SkriptClass skriptClass;
        private final ClassType classType;
        private String internalName;

        private final ClassWriter cw;

        public RecordBuilder(ClassBuilder cb) {
            this.cb = cb;
            this.skriptClass = cb.skriptClass;
            this.classType = cb.classType;
            this.internalName = cb.internalName;
            this.cw = cb.cw;
        }

        public void setupConstructor() {
            Constructor constructor = Constructor.getDefault();
            ((TypeRecord)skriptClass).getRecordConstructorArguments().forEach(constructor::addArgument);
            MethodVisitor mv = cw.visitMethod(Modifier.PUBLIC.getValue(), constructor.getName(), constructor.getDescriptor(), null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Record", "<init>", "()V", false);
            int i = 0;
            for(Argument argument : ((TypeRecord)skriptClass).getRecordConstructorArguments()) {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                i++;
                int loadCode = cb.getLoadCode(argument.getPrimitiveType());
                mv.visitVarInsn(loadCode, i);
                String descriptor = argument.getDescriptor();
                mv.visitFieldInsn(Opcodes.PUTFIELD, internalName, argument.getName(), descriptor);
            }
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            cw.visitEnd();
        }

        public void setupRecordMethods() {
            int i = 0;
            for(Argument argument : ((TypeRecord)skriptClass).getRecordConstructorArguments()) {
                Method method = new Method(argument.getPrimitiveType(), argument.getType(), argument.getName());
                MethodVisitor mv = cw.visitMethod(Modifier.PUBLIC.getValue(), method.getName(), method.getDescriptor(), null, null);
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                String descriptor = argument.getDescriptor();
                mv.visitFieldInsn(Opcodes.GETFIELD, internalName, argument.getName(), descriptor);
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
                mv.visitMaxs(0, 0);
                mv.visitEnd();
                cw.visitEnd();
            }
        }

        public void generateObjectMethods() {
            int modifiers = Modifier.PUBLIC.getValue() + Modifier.FINAL.getValue();
            Method equalsMethod = new Method(new PrimitiveType(Primitive.BOOLEAN), null, "equals");
            equalsMethod.addArgument(new Argument(new Type(Object.class), "obj"));
            Method hashCodeMethod = new Method(new PrimitiveType(Primitive.INT), null, "hashCode");
            Method toStringMethod = new Method(new PrimitiveType(Primitive.NONE), new Type(String.class), "toString");
            for(Method method : new Method[]{equalsMethod, hashCodeMethod, toStringMethod}) {
                MethodVisitor mv = cw.visitMethod(modifiers, method.getName(), method.getDescriptor(), null, null);
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                int i = 0;
                for(Argument argument : method.getArguments()) {
                    i++;
                    mv.visitVarInsn(cb.getLoadCode(argument.getPrimitiveType()), i);
                }
                String dynamicDescriptor = "(L" + internalName + ";" + method.getDescriptor().replaceFirst("\\(", "");
                mv.visitInvokeDynamicInsn(method.getName(), dynamicDescriptor, getBootstrapHandle(), getBootstrapMethodArgs());
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
                mv.visitMaxs(0, 0);
                mv.visitEnd();
                cw.visitEnd();
            }
        }

        public Handle getBootstrapHandle() {
            return new Handle(Opcodes.H_INVOKESTATIC, "java/lang/runtime/ObjectMethods", "bootstrap",
                    "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;",
                    false);
        }

        public Object[] getBootstrapMethodArgs() {
            List<Object> args = new ArrayList<>();
            args.add(skriptClass.getType().toASMType());
            List<String> parameterNames = new ArrayList<>();
            ((TypeRecord)skriptClass).getRecordConstructorArguments().forEach(argument -> parameterNames.add(argument.getName()));
            args.add(String.join(";", parameterNames));
            for(Argument argument : ((TypeRecord)skriptClass).getRecordConstructorArguments()) {
                String descriptor = argument.getDescriptor();
                Handle handle = new Handle(Opcodes.H_GETFIELD, internalName, argument.getName(), descriptor, false);
                args.add(handle);
            }
            return args.toArray();
        }

    }

}
