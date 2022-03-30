package me.pesekjak.hippo.classes;

import me.pesekjak.hippo.classes.contents.annotation.Annotation;

import java.util.ArrayList;
import java.util.List;

public class Annotatable extends Modifiable {

    private final List<Annotation> annotations = new ArrayList<>();

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void addAnnotation(Annotation annotation) {
        if(!annotations.contains(annotation)) annotations.add(annotation);
    }

    public void removeAnnotation(Annotation annotation) {
        annotations.remove(annotation);
    }

}
