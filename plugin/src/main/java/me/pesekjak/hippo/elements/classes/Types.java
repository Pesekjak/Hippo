package me.pesekjak.hippo.elements.classes;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.registrations.Classes;
import me.pesekjak.hippo.Hippo;
import me.pesekjak.hippo.core.ASMUtil;
import me.pesekjak.hippo.core.NamedParameter;
import me.pesekjak.hippo.core.PreImport;
import me.pesekjak.hippo.core.annotations.Annotation;
import me.pesekjak.hippo.elements.classes.handles.Modifier;
import me.pesekjak.hippo.utils.EnumTypeParser;
import me.pesekjak.hippo.utils.SkriptUtil;
import me.pesekjak.hippo.utils.TypeLookup;
import me.pesekjak.hippo.utils.parser.AnnotationParser;
import me.pesekjak.hippo.utils.parser.NamedParameterParser;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.skriptlang.skript.lang.script.Script;

import java.util.logging.Logger;


public final class Types {

    private static boolean failed = false;

    private Types() {
        throw new UnsupportedOperationException();
    }

    static {
        register("modifier", Modifier.class, Types::registerModifierType);
        register("annotation", Annotation.class, Types::registerAnnotationType);
        register("parameter", NamedParameter.class, Types::registerParameterType);
        register("preimport", PreImport.class, Types::registerPreImportType);
        register("character", Character.class, Types::registerCharacterType);
        if (failed) {
            Logger logger = Hippo.getInstance().getLogger();
            logger.warning("Failed to register all types, the addon may not function properly");
            logger.warning("This is most likely caused by another addon incompatible with Hippo");
        }
    }

    private static void register(String codeName, Class<?> clazz, Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception exception) {
            ClassInfo<?> conflicting;
            String message = "Failed to register type '" + codeName + "'";

            if (Classes.getExactClassInfo(clazz) != null) {
                conflicting = Classes.getExactClassInfo(clazz);
                assert conflicting != null;
                message = message + " because the class '" + conflicting.getC() + "' is already used by a different type";
            }

            else if (Classes.getClassInfoNoError(codeName) != null) {
                conflicting = Classes.getClassInfoNoError(codeName);
                assert conflicting != null;
                message = message + " because the same codename is already used by type of class '" + conflicting.getC() + "'";
            }

            Hippo.getInstance().getLogger().severe(message);
            failed = true;
        }
    }

    private static void registerModifierType() {
        Classes.registerClass(new ClassInfo<>(Modifier.class, "modifier")
                .user("modifiers?")
                .parser(new EnumTypeParser<>("modifier", Modifier.class))
                .name("Modifier")
                .description("Represents a Java modifier.")
                .examples("public, static int hello")
                .since("1.0.0")
        );
    }

    private static void registerAnnotationType() {
        Classes.registerClass(new ClassInfo<>(Annotation.class, "annotation")
                .user("annotations?")
                .parser(new AnnotationParser())
                .name("Annotation")
                .description("Represents a Java annotation.")
                .examples("@Override")
                .since("1.0.0")
        );
    }

    private static void registerParameterType() {
        Classes.registerClass(new ClassInfo<>(NamedParameter.class, "parameter")
                .user("parameters?")
                .parser(new NamedParameterParser())
                .name("Named Parameter")
                .description("Represents a named parameter.")
                .examples("public void method(@Nullable String param)")
                .since("1.0.0")
        );
    }

    private static void registerPreImportType() {
        Classes.registerClass(new ClassInfo<>(PreImport.class, "preimport")
                .user("preimports?")
                .parser(new Parser<>() {
                    @Override
                    public PreImport parse(@NotNull String string, @NotNull ParseContext context) {
                        Script script = SkriptUtil.getCurrentScript(ParserInstance.get());
                        if (script == null) return null;
                        Type type = TypeLookup.lookup(script, string, true);
                        if (type == null) return null;
                        if (!ASMUtil.isComplex(type) || ASMUtil.isArray(type)) return null;
                        return new PreImport(type);
                    }

                    @Override
                    public boolean canParse(@NotNull ParseContext context) {
                        return context != ParseContext.DEFAULT;
                    }

                    @Override
                    public @NotNull String toString(PreImport preImport, int flags) {
                        return "preimport " + preImport.type().getClassName();
                    }

                    @Override
                    public @NotNull String toVariableNameString(PreImport preImport) {
                        return "preimport:" + preImport.type().getClassName();
                    }
                })
                .name("Pre-Import")
                .description("Represents a pre-imported class")
                .examples("public void run() throws PreImportedException")
                .since("1.0.0")
        );
    }

    private static void registerCharacterType() {
        Classes.registerClass(new ClassInfo<>(Character.class, "character")
                .user("characters?")
                .parser(new Parser<>() {
                    @Override
                    public Character parse(@NotNull String string, @NotNull ParseContext context) {
                        if (!string.startsWith("'") || !string.endsWith("'")) return null;
                        if (string.length() != 3) return null;
                        return string.charAt(1);
                    }

                    @Override
                    public boolean canParse(@NotNull ParseContext context) {
                        return true;
                    }

                    @Override
                    public @NotNull String toString(Character character, int flags) {
                        return character.toString();
                    }

                    @Override
                    public @NotNull String toVariableNameString(Character character) {
                        return "char:" + character.toString();
                    }
                })
                .name("Character")
                .description("Represents a single character")
                .examples("set {_c} to 'A'")
                .since("1.0.0")
        );
    }

}
