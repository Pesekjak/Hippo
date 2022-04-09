package me.pesekjak.hippo.classes.builder;

import me.pesekjak.hippo.classes.*;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.contents.Field;
import me.pesekjak.hippo.classes.contents.annotation.Annotation;
import me.pesekjak.hippo.classes.contents.annotation.AnnotationElement;
import me.pesekjak.hippo.classes.registry.SkriptClassRegistry;
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
        int modifiers = skriptClass.getClassType().getValue() + sumModifiers(skriptClass);
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

        // Handles init method
        generateInit();

        // End :)
        cw.visitEnd();

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

    private void generateInit() {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        for(Field field : skriptClass.getFields().values()) {
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

    private void setupConstantField(MethodVisitor mv, Field field) {
        String descriptor = field.getDescriptor();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        if(field.getConstant().getConstantObject() != null) {
            mv.visitLdcInsn(field.getConstant().getConstantObject());
        } else {
            mv.visitFieldInsn(Opcodes.GETSTATIC, field.getConstant().getType().getInternalName(), field.getConstant().getPath(), field.getConstant().getType().getDescriptor());
            mv.visitTypeInsn(Opcodes.CHECKCAST, descriptor);
        }
        mv.visitFieldInsn(Opcodes.PUTFIELD, internalName, field.getName(), descriptor);
    }

    private void setupConstantArrayField(MethodVisitor mv, Field field) {
        String descriptor = field.getDescriptor();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitIntInsn(Opcodes.BIPUSH, field.getConstantArray().getConstants().size());
        mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
        int i = 0;
        for(Constant constant : field.getConstantArray().getConstants()) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitIntInsn(Opcodes.BIPUSH, i);
            if(constant.getConstantObject() != null) {
                mv.visitLdcInsn(constant.getConstantObject());
            } else {
                mv.visitFieldInsn(Opcodes.GETSTATIC, constant.getType().getInternalName(), constant.getPath(), constant.getType().getDescriptor());
            }
            mv.visitInsn(Opcodes.AASTORE);
            i++;
        }
        mv.visitTypeInsn(Opcodes.CHECKCAST, descriptor);
        mv.visitFieldInsn(Opcodes.PUTFIELD, internalName, field.getName(), descriptor);
    }

    private void setupValueField(MethodVisitor mv, Field field) {
        String descriptor = field.getDescriptor();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETSTATIC, REGISTRY_TYPE.getInternalName(), "REGISTRY", REGISTRY_TYPE.getDescriptor());
        mv.visitLdcInsn(skriptClass.getClassName());
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, REGISTRY_TYPE.getInternalName(), "getSkriptClass", "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/SkriptClass;", false);
        mv.visitLdcInsn(field.getName());
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/SkriptClass", "getField", "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/contents/Field;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/contents/Field", "getValue", "()Lch/njol/skript/lang/Expression;", false);
        mv.visitFieldInsn(Opcodes.GETSTATIC, REGISTRY_TYPE.getInternalName(), "REGISTRY", REGISTRY_TYPE.getDescriptor());
        mv.visitLdcInsn(skriptClass.getClassName());
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, REGISTRY_TYPE.getInternalName(), "getSkriptClass", "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/SkriptClass;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/pesekjak/hippo/classes/SkriptClass", "getDefineEvent", "()Lorg/bukkit/event/Event;", false);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "ch/njol/skript/lang/Expression", "getSingle", "(Lorg/bukkit/event/Event;)Ljava/lang/Object;", true);
        mv.visitTypeInsn(Opcodes.CHECKCAST, field.getType().getInternalName());
        mv.visitFieldInsn(Opcodes.PUTFIELD, internalName, field.getName(), descriptor);
    }

    private void setupFieldAnnotation(FieldVisitor fv, Annotation annotation) {
        AnnotationVisitor av = fv.visitAnnotation(annotation.getType().getDescriptor(), true);
        setupAnnotation(av, annotation);
        av.visitEnd();
    }

}
