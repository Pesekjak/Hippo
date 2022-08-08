package me.pesekjak.hippo.classes.classtypes;

import me.pesekjak.hippo.classes.ClassType;
import me.pesekjak.hippo.classes.ISkriptClass;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.builder.ClassBuilder;
import me.pesekjak.hippo.classes.builder.IClassBuilder;
import me.pesekjak.hippo.classes.content.ClassContent;
import me.pesekjak.hippo.classes.content.ContentType;
import me.pesekjak.hippo.classes.content.Method;
import me.pesekjak.hippo.skript.Pair;
import me.pesekjak.hippo.utils.Logger;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;

public class SkriptAnnotation extends ISkriptClass {

    public SkriptAnnotation(Type type) {
        super(type);
    }

    @Override
    public ClassType getClassType() {
        return ClassType.ANNOTATION;
    }

    @Override
    public boolean illegalModifiers(ContentType contentType, List<Modifier> modifiers) {
        if(contentType == ContentType.METHOD) {
            if(modifiers.size() != 2) return true;
            return !(modifiers.contains(Modifier.PUBLIC) && modifiers.contains(Modifier.ABSTRACT));
        }
        else if(contentType == ContentType.FIELD) {
            if(modifiers.size() != 3) return true;
            return !(modifiers.contains(Modifier.PUBLIC) &&
                    modifiers.contains(Modifier.STATIC) &&
                    modifiers.contains(Modifier.FINAL));
        }
        else if(contentType == ContentType.CLASS) {
            if(modifiers.size() != 1) return true;
            return !modifiers.contains(Modifier.PUBLIC);
        }
        return true;
    }

    @Override
    public boolean canHave(ContentType contentType) {
        return !(contentType != ContentType.METHOD && contentType != ContentType.FIELD);
    }

    @Override
    public boolean canExtend() {
        return false;
    }

    @Override
    public boolean setup(final IClassBuilder CB) {
        for(ClassContent content : getClassContent().values()) {
            if(!(content instanceof Method method)) {
                content.setup(CB);
                continue;
            }
            int modifiers = ClassBuilder.sumModifiers(method.getModifiers().toArray(new Modifier[0]));
            List<String> exceptions = new ArrayList<>();
            method.getExceptions().forEach(e -> exceptions.add(e.internalName()));
            final MethodVisitor MV = CB.visitor().visitMethod(modifiers, method.getName(), method.getDescriptor(), null, exceptions.toArray(new String[0]));

            getAnnotations().forEach(annotation -> annotation.setupMethodAnnotation(CB, MV)); // setups annotations

            if (method.getArguments().values().size() > 0) { // Members may not have parameters
                Logger.severe("Class '" + CB.skriptClass().getType().dotPath() + "' failed to compile because its member '" + method.getName() + "' have parameters.");
                return false;
            }

            if(method.getDefaultValue() != null) {
                AnnotationVisitor AV = MV.visitAnnotationDefault();
                if(!method.getDefaultValue().isArray()) {
                    handleObject(AV, method.getDefaultValue().getName(), method.getDefaultValue().getSingle());
                    continue;
                }
                AnnotationVisitor arrayVisitor = AV.visitArray(method.getDefaultValue().getName());
                for(Object o : method.getDefaultValue().getObjects())
                    handleObject(arrayVisitor, null, o);
                arrayVisitor.visitEnd();
            }
        }
        return super.setup(CB);
    }

    private void handleObject(final AnnotationVisitor AV, @Nullable String name, Object o) {
        if(o instanceof Pair pair)
            AV.visitEnum(name, pair.type().descriptor(), pair.key());
        else
            AV.visit(name, o);
    }

}
