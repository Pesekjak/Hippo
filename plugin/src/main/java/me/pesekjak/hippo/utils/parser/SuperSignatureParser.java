package me.pesekjak.hippo.utils.parser;

import me.pesekjak.hippo.utils.TypeLookup;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Type;
import org.skriptlang.skript.lang.script.Script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parser for super constructor signatures.
 */
public final class SuperSignatureParser {

    private SuperSignatureParser() {
        throw new UnsupportedOperationException();
    }

    /**
     * Parses signature for given script.
     *
     * @param string text to parse
     * @param script script
     * @return parsed signature as list of types
     */
    public static @Unmodifiable List<@Nullable Type> parse(String string, Script script) throws ParserException {
        List<Type> types = new ArrayList<>();
        String toParse = string.trim();
        if (toParse.length() == 0) return Collections.unmodifiableList(types);

        while (toParse.length() != 0) {
            TypeEntry next = next(toParse, script);
            types.add(next.type);
            toParse = next.rest.trim();
        }

        return Collections.unmodifiableList(types);
    }

    /**
     * Parses next type entry in the signature.
     *
     * @param string signature
     * @param script script
     * @return next type entry
     */
    private static TypeEntry next(String string, Script script) throws ParserException {
        if (string.length() == 0) throw new ParserException();

        String toParse = string.trim();

        Analyzer analyzer = new Analyzer(toParse);
        while (analyzer.canMove() && analyzer.peek() == ' ')
            analyzer.eat();

        StringBuilder typeBuilder = new StringBuilder();
        while (analyzer.canMove() && analyzer.peek() != ' ' && analyzer.peek() != ',')
            typeBuilder.append(analyzer.move());

        Type type = TypeLookup.lookup(script, typeBuilder.toString());

        while (analyzer.canMove() && (analyzer.peek() == ' ' || analyzer.peek() == ','))
            analyzer.eat();

        return new TypeEntry(type, analyzer.rest());
    }

    /**
     * Parsed signature type entry.
     *
     * @param type next parsed type
     * @param rest rest of the text to parse
     */
    private record TypeEntry(@Nullable Type type, String rest) {
    }

}
