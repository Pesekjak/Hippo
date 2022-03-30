package me.pesekjak.hippo.classes.contents.annotation;

import me.pesekjak.hippo.classes.Constant;

public record AnnotationElement(String constantName, Constant constant) {

    public String getConstantName() {
        return constantName;
    }

    public Constant getConstant() {
        return constant;
    }
}
