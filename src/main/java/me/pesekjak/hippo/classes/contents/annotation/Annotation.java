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

    //    public String toJavaCode() {
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("@").append(type.getJavaClassPath()).append("(");
//        int i = 0;
//        for(AnnotationElement element : elements) {
//            stringBuilder.append(element.getConstantName()).append(" = ").append(element.getConstant().toJavaCode());
//            i++;
//            if(i != elements.size()) stringBuilder.append(", ");
//        }
//        stringBuilder.append(")");
//        return stringBuilder.toString();
//    }

}
