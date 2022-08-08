package me.pesekjak.hippo.classes.content;

import lombok.Getter;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.builder.IClassBuilder;
import me.pesekjak.hippo.skript.Pair;
import me.pesekjak.hippo.skript.classes.annotations.SkriptAnnotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;

public class Annotation implements ClassContent {

    @NotNull
    private final Type type;
    @Getter
    public final List<SkriptAnnotation.AnnotationElement> elements = new ArrayList<>();

    public Annotation(@NotNull Type type) {
        this.type = type;
    }

    @Override
    public String getIdentifier() {
        return getName();
    }

    @Override
    public String getName() {
        return "@" + type.dotPath();
    }

    @Override
    public @NotNull Type getType() {
        return type;
    }

    @Override
    public String getDescriptor() {
        return type.descriptor();
    }

    @Override
    public void setup(IClassBuilder CB) {
        throw new UnsupportedOperationException("Annotations have to be setup using" +
                "Annotation#setupFieldAnnotation, Annotation#setupMethodAnnotation and Annotation#setupClassAnnotation");
    }

    public void setupFieldAnnotation(IClassBuilder CB, FieldVisitor FV) {
        AnnotationVisitor AV = FV.visitAnnotation(type.descriptor(), true);
        setupAnnotation(CB, AV);
        AV.visitEnd();
    }

    public void setupMethodAnnotation(IClassBuilder CB, MethodVisitor MV) {
        AnnotationVisitor AV = MV.visitAnnotation(type.descriptor(), true);
        setupAnnotation(CB, AV);
        AV.visitEnd();
    }

    public void setupClassAnnotation(IClassBuilder CB) {
        setupClassAnnotation(CB, CB.visitor());
    }

    public void setupClassAnnotation(IClassBuilder CB, ClassVisitor CV) {
        AnnotationVisitor AV = CV.visitAnnotation(type.descriptor(), true);
        setupAnnotation(CB, AV);
        AV.visitEnd();
    }

    protected void setupAnnotation(IClassBuilder CB, AnnotationVisitor AV) {
        for(SkriptAnnotation.AnnotationElement element : elements) {
            if(!element.isArray()) {
                handleObject(AV, element.getName(), element.getSingle());
                continue;
            }
            AnnotationVisitor arrayVisitor = AV.visitArray(element.getName());
            for(Object o : element.getObjects())
                handleObject(arrayVisitor, null, o);
            arrayVisitor.visitEnd();
        }
    }

    private void handleObject(AnnotationVisitor AV, @Nullable String name, Object o) {
        if(o instanceof Pair pair)
            AV.visitEnum(name, pair.type().descriptor(), pair.key());
        else
            AV.visit(name, o);
    }

    public void addElements(SkriptAnnotation.AnnotationElement... elements) {
        this.elements.addAll(List.of(elements));
    }

}
