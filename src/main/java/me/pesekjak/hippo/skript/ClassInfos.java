package me.pesekjak.hippo.skript;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import me.pesekjak.hippo.classes.*;
import me.pesekjak.hippo.classes.contents.annotation.Annotation;
import me.pesekjak.hippo.classes.contents.annotation.AnnotationElement;
import me.pesekjak.hippo.skript.classes.Pair;
import org.jetbrains.annotations.NotNull;

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
                .parser(new Parser<>() {
                    @Override
                    public Character parse(@NotNull String s, @NotNull ParseContext parseContext) {
                        if (!s.startsWith("'") || !s.endsWith("'")) return null;
                        if (s.length() != 3) return null;
                        return s.charAt(1);
                    }

                    @Override
                    public boolean canParse(@NotNull ParseContext context) {
                        return true;
                    }

                    @Override
                    public @NotNull String toString(Character character, int i) {
                        return character.toString();
                    }

                    @Override
                    public @NotNull String toVariableNameString(Character character) {
                        return character.toString();
                    }
                })
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

        Classes.registerClass(new ClassInfo<>(Type.class, "asmtype")
                .user("asmtypes?")
                .name("Type")
                .description("Type used for creating classes.")
                .since("1.0-BETA.1")
        );

        Classes.registerClass(new ClassInfo<>(PrimitiveType.class, "primitivetype")
                .user("primitive types?")
                .name("Primitive Type")
                .description("Type of Primitives used for creating classes.")
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
