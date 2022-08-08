package me.pesekjak.hippo.classes.classtypes;

import me.pesekjak.hippo.classes.ClassType;
import me.pesekjak.hippo.classes.ISkriptClass;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.builder.IClassBuilder;
import me.pesekjak.hippo.classes.content.ClassContent;
import me.pesekjak.hippo.classes.content.Constructor;
import me.pesekjak.hippo.classes.content.ContentType;

import java.util.List;

public class SkriptClass extends ISkriptClass {

    public SkriptClass(Type type) {
        super(type);
    }

    @Override
    public ClassType getClassType() {
        return ClassType.CLASS;
    }

    @Override
    public boolean illegalModifiers(ContentType contentType, List<Modifier> modifiers) {
        if(contentType != ContentType.METHOD) return false;
        if(!getModifiers().contains(Modifier.ABSTRACT))
            return modifiers.contains(Modifier.ABSTRACT);
        return false;
    }

    @Override
    public boolean canHave(ContentType contentType) {
        return contentType != ContentType.ENUM;
    }

    @Override
    public boolean canExtend() {
        return true;
    }

    @Override
    public boolean setup(final IClassBuilder CB) {

        boolean hasConstructor = false;
        for(ClassContent content : getClassContent().values()) {
            if (content instanceof Constructor) {
                hasConstructor = true;
                break;
            }
        }
        if(!hasConstructor)
            // Adds default Java constructor
            getClassContent().put(Constructor.defaultConstructor().getIdentifier(), Constructor.defaultConstructor());
        getClassContent().values().forEach(classContent -> classContent.setup(CB));

        super.setup(CB); // Handles the static block and annotations

        return true;
    }

}
