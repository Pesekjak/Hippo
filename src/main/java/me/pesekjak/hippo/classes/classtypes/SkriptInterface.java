package me.pesekjak.hippo.classes.classtypes;

import me.pesekjak.hippo.classes.ClassType;
import me.pesekjak.hippo.classes.ISkriptClass;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.builder.IClassBuilder;
import me.pesekjak.hippo.classes.content.ContentType;

import java.util.List;

public class SkriptInterface extends ISkriptClass {

    public SkriptInterface(Type type) {
        super(type);
    }

    @Override
    public ClassType getClassType() {
        return ClassType.INTERFACE;
    }

    @Override
    public boolean illegalModifiers(ContentType contentType, List<Modifier> modifiers) {
        if(!modifiers.contains(Modifier.PUBLIC))
            return true;
        if(contentType == ContentType.CLASS && modifiers.contains(Modifier.ABSTRACT))
            return true;
        if(contentType == ContentType.FIELD)
            return !modifiers.contains(Modifier.STATIC);
        return false;
    }

    @Override
    public boolean canHave(ContentType contentType) {
        if(contentType == ContentType.ENUM ||
                contentType == ContentType.STATIC_BLOCK) return false;
        return contentType != ContentType.CONSTRUCTOR;
    }

    @Override
    public boolean canExtend() {
        return false;
    }

    @Override
    public boolean setup(final IClassBuilder CB) {
        super.setup(CB); // Handles the static block and annotations
        getClassContent().values().forEach(classContent -> classContent.setup(CB));
        return true;
    }

}
