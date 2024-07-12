package me.pesekjak.hippo.utils;

import ch.njol.skript.config.Config;
import ch.njol.skript.registrations.Classes;
import com.btk5h.skriptmirror.JavaType;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import me.pesekjak.hippo.core.ASMUtil;
import me.pesekjak.hippo.core.PreImport;
import me.pesekjak.hippo.utils.parser.Analyzer;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.skriptlang.reflect.java.elements.structures.StructImport;
import org.skriptlang.skript.lang.script.Script;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Util class used to parse and manage custom types.
 * <p>
 * Is used to store the pre-imported classes across all scripts and
 * to parse the types, {@link me.pesekjak.hippo.elements.expressions.ExprPreImport}.
 */
public final class TypeLookup {

    private static final Table<Script, String, PreImport> PRE_IMPORTS = Tables.newCustomTable(new HashMap<>(), HashMap::new);

    private TypeLookup() {
        throw new UnsupportedOperationException();
    }

    /**
     * Parses type identifier in given script.
     *
     * @param script script
     * @param identifier type identifier, accepts primitive and array types as well.
     * @param lookupClassInfo whether registered class infos should be accepted as types
     * @return parsed type or null if it can not be found
     */
    public static @Nullable Type lookup(Script script, String identifier, boolean lookupClassInfo) {
        String alias = identifier;
        int arrayLevel = 0;

        if (alias.lastIndexOf(']') == alias.length() - 1) {
            Analyzer analyzer = new Analyzer(alias);
            StringBuilder aliasBuilder = new StringBuilder();
            while (analyzer.canMove() && analyzer.peek() != '[') aliasBuilder.append(analyzer.move());
            alias = aliasBuilder.toString();
            while (analyzer.canMove() && analyzer.move() == '[' && analyzer.canMove() && analyzer.move() == ']')
                arrayLevel++;
        }

        PreImport preImport = PRE_IMPORTS.get(script, alias);
        if (preImport != null) return ASMUtil.getArrayType(preImport.type(), arrayLevel);

        if (alias.toLowerCase().equals(alias)) {
            Type primitive;
            try {
                primitive = switch (alias) {
                    case "boolean" -> Type.BOOLEAN_TYPE;
                    case "char" -> Type.CHAR_TYPE;
                    case "byte" -> Type.BYTE_TYPE;
                    case "short" -> Type.SHORT_TYPE;
                    case "int" -> Type.INT_TYPE;
                    case "long" -> Type.LONG_TYPE;
                    case "float" -> Type.FLOAT_TYPE;
                    case "double" -> Type.DOUBLE_TYPE;
                    case "void" -> Type.VOID_TYPE;
                    default -> throw new IllegalStateException("Unexpected value: " + alias);
                };
                if (primitive == Type.VOID_TYPE && arrayLevel != 0)
                    return null;
            } catch (Throwable exception) {
                primitive = null;
            }
            if (primitive != null) return ASMUtil.getArrayType(primitive, arrayLevel);
        }

        JavaType javaType;
        try { // try block because of tests
            javaType = StructImport.lookup(script, alias);
        } catch (Throwable exception) {
            javaType = null;
        }
        if (javaType != null) return ASMUtil.getArrayType(javaType.getJavaClass(), arrayLevel);

        if (!lookupClassInfo) return null;

        try {
            Class<?> classInfo = Classes.getClass(alias.toLowerCase());
            return ASMUtil.getArrayType(classInfo, arrayLevel);
        } catch (Throwable throwable) {
            return null;
        }
    }

    /**
     * Registers new pre-import.
     *
     * @param script script to register the pre-import for
     * @param identifier alias
     * @param preImport pre-import
     * @return whether the import has been registered under given alias
     */
    public static boolean registerPreImport(Script script, String identifier, PreImport preImport) {
        if (PRE_IMPORTS.contains(script, identifier)) return false;
        PRE_IMPORTS.put(script, identifier, preImport);
        return true;
    }

    /**
     * Unregisters pre-import.
     *
     * @param script script to unregister the pre-import for
     * @param identifier alias
     * @return whether the import has been unregistered
     */
    public static boolean unregisterPreImport(Script script, String identifier) {
        if (!PRE_IMPORTS.contains(script, identifier)) return false;
        PRE_IMPORTS.remove(script, identifier);
        return true;
    }

    /**
     * Unregisters all pre-imports for given script.
     *
     * @param script script to unregister all pre-imports for
     */
    public static void unregisterPreImports(Script script) {
        PRE_IMPORTS.row(script).clear();
        PRE_IMPORTS.rowMap().remove(script);
    }

    /**
     * Unregisters all pre-imports for given scripts.
     *
     * @param configs scripts to unregister all pre-imports for
     */
    public static void unregisterPreImports(Collection<Config> configs) {
        List<Script> toUnregister = PRE_IMPORTS.rowKeySet().stream()
                .filter(script -> {
                    for (Config config : configs)
                        if (config.getFileName().equals(script.getConfig().getFileName())) return true;
                    return false;
                })
                .toList();
        toUnregister.forEach(TypeLookup::unregisterPreImports);
    }

}
