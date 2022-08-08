package me.pesekjak.hippo.skript;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.registrations.Classes;
import com.btk5h.skriptmirror.JavaType;
import com.btk5h.skriptmirror.skript.custom.CustomImport;
import com.btk5h.skriptmirror.util.SkriptUtil;
import me.pesekjak.hippo.classes.ClassType;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.content.Annotation;
import me.pesekjak.hippo.classes.types.NonPrimitiveType;
import me.pesekjak.hippo.classes.types.VoidType;
import me.pesekjak.hippo.classes.types.primitives.*;
import me.pesekjak.hippo.skript.classes.annotations.SkriptAnnotation;
import me.pesekjak.hippo.skript.classes.annotations.DefaultValue;
import me.pesekjak.hippo.utils.Reflectness;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Types {

    private static final Field
            CLASS_INFOS_BY_CODE_NAME_FIELD = Reflectness.getField("classInfosByCodeName", Classes.class),
            PARSER_FIELD = Reflectness.getField("parser", ClassInfo.class);

    static {
        hookJavaType();

        Classes.registerClass(new ClassInfo<>(Character.class, "character")
                .user("characters?")
                .parser(new Parser<Character>() {
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
                        return "char:" + character.toString();
                    }

                    public String getVariableNamePattern() {
                        return "char:.+";
                    }
                })
        );

        Classes.registerClass(new ClassInfo<>(Primitive.class, "primitivetype")
                .user("primitivetypes?")
                .parser(new Parser<Primitive>() {
                    @Override
                    public Primitive parse(@NotNull String s, @NotNull ParseContext context) {
                        if(!s.toLowerCase().equals(s)) return null;
                        try {
                            return Primitive.valueOf(s.toUpperCase());
                        } catch (IllegalArgumentException exception) {
                            return null;
                        }
                    }

                    @Override
                    public boolean canParse(@NotNull ParseContext context) {
                        return context == ParseContext.DEFAULT || context == ParseContext.EVENT;
                    }

                    @Override
                    public @NotNull String toString(Primitive o, int flags) {
                        return o.name().toLowerCase();
                    }

                    @Override
                    public @NotNull String toVariableNameString(Primitive o) {
                        return "primitivetype:" + toString(o, 0);
                    }

                    public String getVariableNamePattern() {
                        return "primitivetype:.+";
                    }
                })
        );

        Classes.registerClass(new ClassInfo<>(Modifier.class, "modifier")
                .user("modifiers?")
                .parser(new Parser<Modifier>() {
                    @Override
                    public Modifier parse(@NotNull String s, @NotNull ParseContext context) {
                        if(!s.toLowerCase().equals(s)) return null;
                        try {
                            return Modifier.valueOf(s.toUpperCase());
                        } catch (IllegalArgumentException exception) {
                            return null;
                        }
                    }

                    @Override
                    public boolean canParse(@NotNull ParseContext context) {
                        return context == ParseContext.EVENT;
                    }

                    @Override
                    public @NotNull String toString(Modifier o, int flags) {
                        return o.name().toLowerCase();
                    }

                    @Override
                    public @NotNull String toVariableNameString(Modifier o) {
                        return "modifier:" + toString(o, 0);
                    }

                    public String getVariableNamePattern() {
                        return "modifier:.+";
                    }
                })
        );

        Classes.registerClass(new ClassInfo<>(ClassType.class, "classtype")
                .user("classtypes?")
                .parser(new Parser<ClassType>() {
                    @Override
                    public ClassType parse(@NotNull String s, @NotNull ParseContext context) {
                        if(!s.toLowerCase().equals(s)) return null;
                        try {
                            return ClassType.valueOf(s.toUpperCase());
                        } catch (IllegalArgumentException exception) {
                            return null;
                        }
                    }

                    @Override
                    public boolean canParse(@NotNull ParseContext context) {
                        return context == ParseContext.EVENT;
                    }

                    @Override
                    public @NotNull String toString(ClassType classType, int flags) {
                        return classType.name().toLowerCase();
                    }

                    @Override
                    public @NotNull String toVariableNameString(ClassType classType) {
                        return "classtype:" + toString(classType, 0);
                    }

                    public String getVariableNamePattern() {
                        return "classtype:.+";
                    }
                })
        );

        Classes.registerClass(new ClassInfo<>(Type.class, "complextype")
                .user("complextypes?")
                .parser(new Parser<>() {

                    final String PATTERN = "[_a-zA-Z0-9]+(\\[])*";

                    @Override
                    public Type parse(@NotNull String s, @NotNull ParseContext context) {
                        if(!s.matches(PATTERN)) return null;
                        Pattern pattern = Pattern.compile("\\[]");

                        // Getting size of array
                        int size = 0;
                        Matcher arrayMatcher = pattern.matcher(s);
                        while(arrayMatcher.find()) size++;

                        // Getting Type
                        Type type = null;
                        String typeString = s.split("\\[")[0];

                        Primitive primitive = null;
                        try {
                            primitive = Primitive.valueOf(typeString.toUpperCase());
                        } catch (IllegalArgumentException ignored) {}
                        if(primitive != null) {
                            type = switch (primitive) {
                                case BOOLEAN -> new BooleanType();
                                case CHAR -> new CharType();
                                case BYTE -> new ByteType();
                                case SHORT -> new ShortType();
                                case INT -> new IntType();
                                case LONG -> new LongType();
                                case FLOAT -> new FloatType();
                                case DOUBLE -> new DoubleType();
                                case VOID -> new VoidType();
                            };
                        } else {
                            File script = SkriptUtil.getCurrentScript();
                            JavaType javaType = CustomImport.lookup(script, typeString);
                            PreImport preImport = PreImportSection.lookup(script, typeString);
                            if(javaType != null)
                                type = ExprType.typeFromObject(javaType);
                            else if(preImport != null)
                                type = preImport.asType();
                            else {
                                // Copying reflect behaviour of converting class info to JavaType
                                ClassInfo<?> ci = Classes.getClassInfoNoError(typeString.toLowerCase());
                                if(ci != null) type = new NonPrimitiveType(ci.getC());
                            }
                        }
                        if(type == null) return null;

                        for(int i = 0; i < size; i++) {
                            type = type.array();
                        }

                        return type;
                    }

                    @Override
                    public boolean canParse(@NotNull ParseContext context) {
                        return context == ParseContext.EVENT;
                    }

                    @Override
                    public @NotNull String toString(Type type, int flags) {
                        return "type of " + type.dotPath();
                    }

                    @Override
                    public @NotNull String toVariableNameString(Type type) {
                        return "type:" + type.dotPath();
                    }

                })
        );

        Classes.registerClass(new ClassInfo<>(Pair.class, "pair")
                .user("pairs?")
                .parser(new Parser<>() {

                    final String PATTERN = "[_a-zA-Z0-9]+(\\[])*[ ][a-zA-Z0-9_]+";

                    @Override
                    public Pair parse(@NotNull String s, @NotNull ParseContext context) {
                        if(!s.matches(PATTERN)) return null;
                        String[] parts = s.split(" ");
                        if(parts.length != 2) return null;

                        String name = parts[1];
                        // Name has to start with a letter
                        if(!name.matches("^[a-zA-Z].*"))
                            return null;

                        String complexTypeString = parts[0];
                        Pattern pattern = Pattern.compile("\\[]");

                        // Getting size of array
                        int size = 0;
                        Matcher arrayMatcher = pattern.matcher(complexTypeString);
                        while(arrayMatcher.find()) size++;

                        // Getting Type
                        Type type = null;
                        String typeString = complexTypeString.split("\\[")[0];

                        Primitive primitive = null;
                        try {
                            primitive = Primitive.valueOf(typeString.toUpperCase());
                        } catch (IllegalArgumentException ignored) {}
                        if(primitive != null) {
                            type = switch (primitive) {
                                case BOOLEAN -> new BooleanType();
                                case CHAR -> new CharType();
                                case BYTE -> new ByteType();
                                case SHORT -> new ShortType();
                                case INT -> new IntType();
                                case LONG -> new LongType();
                                case FLOAT -> new FloatType();
                                case DOUBLE -> new DoubleType();
                                case VOID -> new VoidType();
                            };
                        } else {
                            File script = SkriptUtil.getCurrentScript();
                            JavaType javaType = CustomImport.lookup(script, typeString);
                            PreImport preImport = PreImportSection.lookup(script, typeString);
                            if(javaType != null)
                                type = ExprType.typeFromObject(javaType);
                            else if(preImport != null)
                                type = preImport.asType();
                            else {
                                // Copying reflect behaviour of converting class info to JavaType
                                ClassInfo<?> ci = Classes.getClassInfoNoError(typeString.toLowerCase());
                                if(ci != null) type = new NonPrimitiveType(ci.getC());
                            }
                        }
                        if(type == null) return null ;

                        for(int i = 0; i < size; i++) {
                            type = type.array();
                        }

                        return new Pair(name, type);
                    }

                    @Override
                    public boolean canParse(@NotNull ParseContext context) {
                        return context == ParseContext.EVENT;
                    }

                    @Override
                    public @NotNull String toString(Pair pair, int flags) {
                        return pair.key() + " pair";
                    }

                    @Override
                    public @NotNull String toVariableNameString(Pair pair) {
                        return "pair:" + pair.key() + ":" + pair.type().descriptor();
                    }
                })
        );

        Classes.registerClass(new ClassInfo<>(Annotation.class, "annotation")
                .user("annotations?")
        );

        Classes.registerClass(new ClassInfo<>(SkriptAnnotation.AnnotationElement.class, "annotationelement")
                .user("annotationelements?")
        );

        Classes.registerClass(new ClassInfo<>(DefaultValue.class, "annotationvalue")
                .user("annotationvalues?")
        );

    }

    /**
     * Modifiers skript-reflect JavaType type with Hippo's type system
     */
    @SuppressWarnings("ConstantConditions")
    private static void hookJavaType() {
        Parser<Object> parser = new Parser<Object>() {
            @Override
            public Object parse(@NotNull String s, @NotNull ParseContext context) {
                // Reflect code
                File script = SkriptUtil.getCurrentScript();
                JavaType javaType = CustomImport.lookup(script, s);
                if(javaType != null) return javaType;

                // Hippo additional code
                PreImport preImport = PreImportSection.lookup(script, s);

                // Copying reflect behaviour of converting class info to JavaType
                ClassInfo<?> ci = Classes.getClassInfoNoError(s.toLowerCase());
                if(preImport == null && ci != null) return new JavaType(ci.getC());

                if(preImport == null) return null;
                javaType = preImport.asJavaType();
                if(javaType != null) return javaType;

                // All Hippo event syntax has to start without "on",
                // this check is here to prevent reflect Bukkit event
                // listeners for pre-imported classes.
                String key = ParserInstance.get().getNode().getKey();
                if(!key.startsWith("on") && !key.equals(s)) {
                    return preImport.asType();
                }
                return null;
            }

            @Override
            public boolean canParse(@NotNull ParseContext context) {
                return context != ParseContext.DEFAULT;
            }

            @Override
            public @NotNull String toString(Object o, int flags) {
                if(o instanceof Type type) {
                    return type.dotPath() != null ? type.dotPath() : type.simpleName();
                }
                return ((JavaType) o).getJavaClass().getName();
            }

            @Override
            public @NotNull String toVariableNameString(Object o) {
                return "type:" + toString(o, 0);
            }

            public String getVariableNamePattern() {
                return "type:.+";
            }
        };
        @SuppressWarnings("unchecked") HashMap<String, ClassInfo<?>> classInfosByCodeName = (HashMap<String, ClassInfo<?>>) Reflectness
                .getField(CLASS_INFOS_BY_CODE_NAME_FIELD, null);
        ClassInfo<?> javaTypeClassInfo = classInfosByCodeName.get("javatype");
        Reflectness.setField(PARSER_FIELD, javaTypeClassInfo, parser);
    }

}
