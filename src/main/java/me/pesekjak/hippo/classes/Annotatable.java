package me.pesekjak.hippo.classes;

import lombok.Getter;
import me.pesekjak.hippo.classes.content.Annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Annotatable {

    @Getter
    private final List<Annotation> annotations = new ArrayList<>();

    public void addAnnotations(Annotation... annotations) {
        this.annotations.addAll(Arrays.asList(annotations));
    }

}
