package me.pesekjak.hippo.classes;

public enum Modifier {

    PUBLIC("public"),
    PRIVATE("private"),
    DEFAULT("default"),
    PROTECTED("protected"),
    FINAL("final"),
    STATIC("static"),
    ABSTRACT("abstract"),
    TRANSIENT("transient"),
    SYNCHRONIZED("synchronized"),
    VOLATILE("volatile");

    public String identifier;

    Modifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
