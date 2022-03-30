package me.pesekjak.hippo.classes;

public enum ClassType {

    CLASS("class"),
    INTERFACE("interface"),
    RECORD("record"),
    ENUM("enum"),
    ANNOTATION("annotation");

    public String identifier;

    ClassType(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
