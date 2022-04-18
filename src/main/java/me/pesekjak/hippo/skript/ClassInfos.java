package me.pesekjak.hippo.skript;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import me.pesekjak.hippo.classes.*;
import me.pesekjak.hippo.classes.contents.annotation.Annotation;
import me.pesekjak.hippo.classes.contents.annotation.AnnotationElement;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.preimport.PreImport;
import me.pesekjak.hippo.preimport.PreImportManager;
import me.pesekjak.hippo.skript.classes.Pair;
import me.pesekjak.hippo.utils.Logger;
import me.pesekjak.hippo.utils.Reflectness;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

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
                .parser(new Parser<>() {
                    @Override
                    public ClassType parse(@NotNull String s, @NotNull ParseContext parseContext) {
                        try { return ClassType.valueOf(s.toUpperCase(Locale.ROOT)); }
                        catch (Exception e) { return null; }
                    }

                    @Override
                    public boolean canParse(@NotNull ParseContext context) {
                        return true;
                    }

                    @Override
                    public @NotNull String toString(ClassType classType, int i) {
                        return classType.toString();
                    }

                    @Override
                    public @NotNull String toVariableNameString(ClassType classType) {
                        return classType.toString();
                    }
                })
        );

        Classes.registerClass(new ClassInfo<>(Modifier.class, "javamodifier")
                .user("javamodifiers?")
                .name("Java Modifier")
                .description("Modifiers for objects.")
                .since("1.0-BETA.1")
                .parser(new Parser<>() {
                    @Override
                    public Modifier parse(@NotNull String s, @NotNull ParseContext parseContext) {
                        try { return Modifier.valueOf(s.toUpperCase(Locale.ROOT)); }
                        catch (Exception e) { return null; }
                    }

                    @Override
                    public boolean canParse(@NotNull ParseContext context) {
                        return true;
                    }

                    @Override
                    public @NotNull String toString(Modifier modifier, int i) {
                        return modifier.toString();
                    }

                    @Override
                    public @NotNull String toVariableNameString(Modifier modifier) {
                        return modifier.toString();
                    }
                })
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

        Classes.registerClass(new ClassInfo<>(ConstantArray.class, "constantarray")
                .user("constantarrays?")
                .name("Constant Array")
                .description("Array of constants.")
                .since("1.0-BETA.1")
        );

        hookHippoJavaType();
    }

    public static void hookHippoJavaType() {
        HashMap<String, ClassInfo<?>> classInfosByCodeName = (HashMap<String, ClassInfo<?>>) Reflectness.getField(Reflectness.getField("classInfosByCodeName", Classes.class), null);
        if(classInfosByCodeName == null) {
            Logger.warn("Reflect JavaType classinfo hook failed. Preimported classes can't be used for javatype expressions in events.");
            return;
        }
        ClassInfo<?> javaTypeClassInfo = classInfosByCodeName.get("javatype");
        Reflectness.setField(Reflectness.getField("parser", ClassInfo.class), javaTypeClassInfo, getJavaTypeParser());
        Logger.info("Hippo successfully hooked to reflect's JavaType class info.");
    }

    private static Parser<?> getJavaTypeParser() {
        return new Parser<Object>() {

            private static Class<?> skriptUtilClass = null;
            private static Class<?> customImportClass = null;

            static {
                try {
                    skriptUtilClass = Class.forName("com.btk5h.skriptmirror.util.SkriptUtil");
                    customImportClass = Class.forName("com.btk5h.skriptmirror.skript.custom.CustomImport");
                } catch (Exception ignored) { }
            }

            @Override
            public Object parse(@NotNull String s, @NotNull ParseContext context) {
                File script = (File) Reflectness.invoke(Reflectness.getMethod(skriptUtilClass, "getCurrentScript"), null);
                Object reflectJavaType = Reflectness.invoke(Reflectness.getMethod(customImportClass, "lookup", File.class, String.class), null, script, s);
                if(reflectJavaType != null) return reflectJavaType;
                if(script == null) return null;
                PreImportManager.PreImporting preImporting = PreImportManager.MANAGER.getPreImporting(script.getPath());
                if(preImporting == null) return null;
                PreImport preImport = preImporting.getPreImport(s);
                if(preImport == null) return null;
                Object javaType = null;
                try {
                    javaType = SkriptReflectHook.buildJavaType(SkriptReflectHook.getLibraryLoader().loadClass(preImport.getType().getDotPath()));
                } catch (Exception ignored) { }
                if(javaType != null) return javaType;
                return preImport.getType();
            }

            @Override
            public boolean canParse(@NotNull ParseContext context) {
                return context != ParseContext.DEFAULT;
            }

            @Override
            public @NotNull String toString(Object o, int flags) {
                if(SkriptReflectHook.getJavaTypeClass().isInstance(o)) return SkriptReflectHook.classOfJavaType(o).getName();
                return ((Type) o).getDotPath();
            }

            @Override
            public @NotNull String toVariableNameString(Object o) {
                StringBuilder sb = new StringBuilder();
                sb.append("type:");
                if(SkriptReflectHook.getJavaTypeClass().isInstance(o)) sb.append(SkriptReflectHook.classOfJavaType(o).getName());
                else sb.append(((Type) o).getDotPath());
                return sb.toString();
            }

            public String getVariableNamePattern() {
                return "type:.+";
            }
        };
    }

}
