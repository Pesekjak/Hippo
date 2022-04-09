package me.pesekjak.hippo.classes.contents.annotation;

import me.pesekjak.hippo.classes.Constant;
import me.pesekjak.hippo.classes.ConstantArray;

public class AnnotationElement {

    private final String constantName;
    private Constant constant;
    private ConstantArray constantArray;

    public AnnotationElement(String constantName, Constant constant) {
        this.constantName = constantName;
        this.constant = constant;
    }

    public AnnotationElement(String constantName, ConstantArray constantArray) {
        this.constantName = constantName;
        this.constantArray = constantArray;
    }

    public String getConstantName() {
        return constantName;
    }

    public Constant getConstant() {
        return constant;
    }

    public ConstantArray getConstantArray() {
        return constantArray;
    }

}
