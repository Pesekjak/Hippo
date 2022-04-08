package me.pesekjak.hippo.classes.contents.annotation;

import me.pesekjak.hippo.classes.Type;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Annotation {

    private final Type type;
    public final List<AnnotationElement> elements = new ArrayList<>();

    public Annotation(@NotNull Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public List<AnnotationElement> getElements() {
        return elements;
    }

    public void addConstant(AnnotationElement element) {
        elements.add(element);
    }

    public void removeConstant(AnnotationElement element) {
        elements.remove(element);
    }

}
