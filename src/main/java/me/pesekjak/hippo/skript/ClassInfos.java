package me.pesekjak.hippo.skript;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import me.pesekjak.hippo.classes.*;
import me.pesekjak.hippo.classes.contents.annotation.Annotation;
import me.pesekjak.hippo.classes.contents.annotation.AnnotationElement;
import me.pesekjak.hippo.skript.classes.Pair;

public class ClassInfos {

    static {

        Classes.registerClass(new ClassInfo<>(SkriptClass.class, "skriptclass")
                .user("skriptclasss?")
                .name("Skript Class")
                .description("Represents Java class made in Skript.")
                .since("1.0-BETA.1")
        );

        Classes.registerClass(new ClassInfo<>(Character.class, "character")
                .user("characters?")
                .name("Character")
                .description("Represents Java Character primitive type.")
                .since("1.0-BETA.1")
        );

        Classes.registerClass(new ClassInfo<>(ClassType.class, "skriptclasstype")
                .user("skriptclasstypes?")
                .name("Skript Class Type")
                .description("Type of Skript Java Class.")
                .since("1.0-BETA.1")
        );

        Classes.registerClass(new ClassInfo<>(Modifier.class, "javamodifier")
                .user("javamodifiers?")
                .name("Java Modifier")
                .description("Modifiers for objects.")
                .since("1.0-BETA.1")
        );

        Classes.registerClass(new ClassInfo<>(Type.class, "type")
                .user("types?")
                .name("Type")
                .description("Type used for creating classes.")
                .since("1.0-BETA.1")
        );

        Classes.registerClass(new ClassInfo<>(Pair.class, "pair")
                .user("pairs?")
                .name("Type Pair")
                .description("Pair of Type and String, used for specifying arguments.")
                .since("1.0-BETA.1")
        );

        Classes.registerClass(new ClassInfo<>(AnnotationElement.class, "annotationelement")
                .user("annotationelements?")
                .name("Annotation Element")
                .description("Used for specifying elements of annotations.")
                .since("1.0-BETA.1")
        );

        Classes.registerClass(new ClassInfo<>(Annotation.class, "annotation")
                .user("annotations?")
                .name("Annotation")
                .description("Java annotation.")
                .since("1.0-BETA.1")
        );

        Classes.registerClass(new ClassInfo<>(Primitive.class, "primitive")
                .user("primitives?")
                .name("Primitive")
                .description("Represents Java Primitive.")
                .since("1.0-BETA.1")
        );

        Classes.registerClass(new ClassInfo<>(Constant.class, "constant")
                .user("constants?")
                .name("Constant")
                .description("Special type used for constant values.")
                .since("1.0-BETA.1")
        );
    }

}
