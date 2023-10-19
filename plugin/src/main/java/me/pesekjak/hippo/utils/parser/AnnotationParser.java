package me.pesekjak.hippo.utils.parser;

import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.parser.ParserInstance;
import me.pesekjak.hippo.core.annotations.*;
import me.pesekjak.hippo.utils.SkriptUtil;
import me.pesekjak.hippo.utils.TypeLookup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.io.File;
import java.util.*;

/**
 * Parser for annotations.
 */
public final class AnnotationParser extends Parser<Annotation> {

    @Override
    public Annotation parse(@NotNull String string, @NotNull ParseContext context) {
        File script = SkriptUtil.getCurrentScript(ParserInstance.get());
        if (script == null) return null;
        try {
            return parse(string, script);
        } catch (Exception exception) {
            return null;
        }
    }

    @Override
    public boolean canParse(@NotNull ParseContext context) {
        return true;
    }

    @Override
    public @NotNull String toString(Annotation annotation, int flags) {
        return "annotation " + annotation.getType().getClassName();
    }

    @Override
    public @NotNull String toVariableNameString(Annotation annotation) {
        return "annotation:" + annotation.getType().getClassName();
    }

    /**
     * Parses annotation for given script.
     *
     * @param string text to parse
     * @param script script
     * @return parsed annotation
     */
    public static Annotation parse(String string, File script) throws ParserException {
        if (string.length() == 0) throw new ParserException();
        if (string.charAt(0) != '@') throw new ParserException();

        String toParse = string.substring(1);

        int leftBracket = toParse.indexOf('(');
        int rightBracket = toParse.lastIndexOf(')');

        if (leftBracket != -1 && rightBracket != toParse.length() - 1) throw new ParserException();
        if (leftBracket > rightBracket) throw new ParserException();

        String typeIdentifier = toParse;
        if (leftBracket != -1) typeIdentifier = typeIdentifier.substring(0, leftBracket);

        Type type = TypeLookup.lookup(script, typeIdentifier);
        if (type == null) throw new ParserException();

        if (leftBracket != -1 && rightBracket - leftBracket > 1)
            return new Annotation(type, parseValues(toParse.substring(leftBracket + 1, rightBracket), script));
        return new Annotation(type, Collections.emptyMap());
    }

    /**
     * Parses annotation parameters.
     *
     * @param string text to parse
     * @param script script
     * @return annotation parameters
     */
    private static Map<String, AnnotationVisitable> parseValues(String string, File script) throws ParserException {
        String toParse = string.trim();
        Map<String, AnnotationVisitable> values = new LinkedHashMap<>();

        do {
            Entry next = nextEntry(toParse, script);
            if (next == null) break;
            values.put(Objects.requireNonNull(next.name), next.value);
            toParse = next.rest.trim();
            if (toParse.length() == 0) break;
        } while (true);

        return values;
    }

    /**
     * Parses next annotation parameter entry.
     *
     * @param string text to parse
     * @param script script
     * @return next entry
     */
    private static @Nullable Entry nextEntry(String string, File script) throws ParserException {
        Analyzer analyzer = new Analyzer(string);
        visitEnd(analyzer);
        analyzer.trim();
        if (!analyzer.canMove()) return null;

        while (analyzer.canMove() && analyzer.peek() != ' ' && analyzer.peek() != '=')
            analyzer.eat();

        String name = analyzer.analyzed().trim();
        if (name.length() == 0) throw new ParserException();

        while (analyzer.canMove() && (analyzer.peek() == ' ' || analyzer.peek() == '='))
            analyzer.eat();

        return nextNamedEntry(name, analyzer.rest(), script);
    }

    /**
     * Parses value of the next annotation parameter entry.
     * @param name name of the parameter
     * @param string text to parse
     * @param script script
     * @return next entry
     */
    // Takes only the value part to parse, e.g. '10d' or '"Hello World"'
    private static Entry nextNamedEntry(@Nullable String name, String string, File script) throws ParserException {
        Analyzer analyzer = new Analyzer(string);
        if (analyzer.peek() == '"') {
            analyzer.eat();
            if (analyzer.canMove() && analyzer.peek() == '"') {
                visitEnd(analyzer);
                return new Entry(name, new Constant(""), analyzer.rest());
            }
            StringBuilder text = new StringBuilder();
            while (analyzer.canMove()) {
                char next = analyzer.move();
                if (next == '"') {
                    if (analyzer.canMove() && analyzer.peek() == '"') {
                        analyzer.eat();
                        text.append('"');
                        continue;
                    }
                    visitEnd(analyzer);
                    return new Entry(name, new Constant(text.toString()), analyzer.rest());
                }
                text.append(next);
            }
            throw new ParserException();
        }

        if (analyzer.peek() == '\'') {
            analyzer.eat();
            if (!analyzer.canMove()) throw new ParserException();
            char c = analyzer.move();
            if (!analyzer.canMove() || analyzer.peek() != '\'') throw new ParserException();
            analyzer.eat();
            visitEnd(analyzer);
            return new Entry(name, new Constant(c), analyzer.rest());
        }

        if (analyzer.peek() >= 48 && analyzer.peek() <= 57) {
            StringBuilder number = new StringBuilder();
            while (analyzer.canMove()) {
                char next = analyzer.move();
                if (next >= 48 && next <= 57 || next == '.') {
                    number.append(next);
                    if (analyzer.canMove()) continue;
                }
                Number value;
                try {
                    value = Double.valueOf(number.toString());
                } catch (Exception exception) {
                    value = 0d;
                }
                if (!analyzer.canMove() || next == ' ' || next == ',') {
                    visitEnd(analyzer);
                    return new Entry(name, new Constant(value.intValue()), analyzer.rest());
                }
                Constant constant;
                switch (Character.toLowerCase(next)) {
                    case 'b' -> constant = new Constant(value.byteValue());
                    case 's' -> constant = new Constant(value.shortValue());
                    case 'i' -> constant = new Constant(value.intValue());
                    case 'l' -> constant = new Constant(value.longValue());
                    case 'f' -> constant = new Constant(value.floatValue());
                    case 'd' -> constant = new Constant(value.doubleValue());
                    default -> throw new ParserException();
                }
                visitEnd(analyzer);
                return new Entry(name, constant, analyzer.rest());
            }
        }

        if (analyzer.peek() == '@') {
            analyzer.mark();
            while (analyzer.canMove() && analyzer.peek() != '(' && analyzer.peek() != ',' && analyzer.peek() != ' ')
                analyzer.eat();
            if (analyzer.canMove() && analyzer.peek() == '(') {
                analyzer.eat();
                int level = 1;
                boolean inString = false;
                while (analyzer.canMove()) {
                    char next = analyzer.move();
                    if (next == '"') {
                        if (analyzer.canMove() && analyzer.peek() == '"') continue;
                        inString = !inString;
                        continue;
                    }
                    if (inString) continue;
                    if (next == '(') level++;
                    if (next == ')') level--;
                    if (level == 0) break;
                }
                if (level != 0) throw new ParserException();
            }
            StringBuilder toParse = new StringBuilder();
            int length = analyzer.cursor() - analyzer.getMark();
            analyzer.moveToMark();
            for (int i = 0; i < length; i++) toParse.append(analyzer.move());
            Annotation annotation = AnnotationParser.parse(toParse.toString(), script);
            visitEnd(analyzer);
            return new Entry(name, annotation, analyzer.rest());
        }

        if (analyzer.peek() == '[') {
            analyzer.eat();
            analyzer.mark();
            int level = 1;
            boolean inString = false;
            while (analyzer.canMove()) {
                char next = analyzer.move();
                if (next == '"') {
                    if (analyzer.canMove() && analyzer.peek() == '"') continue;
                    inString = !inString;
                    continue;
                }
                if (inString) continue;
                if (next == '[') level++;
                if (next == ']') level--;
                if (level == 0) break;
            }
            if (level != 0) throw new ParserException();
            StringBuilder toParse = new StringBuilder();
            int length = analyzer.cursor() - analyzer.getMark() - 1; // -1 because of closing bracket
            analyzer.moveToMark();
            for (int i = 0; i < length; i++) toParse.append(analyzer.move());
            analyzer.eat(); // eating the closing bracket
            List<AnnotationVisitable> literals = new ArrayList<>();
            Analyzer insideAnalyzer = new Analyzer(toParse.toString());
            while (insideAnalyzer.canMove()) {
                if (insideAnalyzer.peek() == ' ') {
                    insideAnalyzer.eat();
                    continue;
                }
                Entry next = nextNamedEntry(null, insideAnalyzer.rest(), script);
                literals.add(next.value);
                insideAnalyzer = new Analyzer(next.rest);
                visitEnd(insideAnalyzer);
            }
            visitEnd(analyzer);
            return new Entry(name, new LiteralsArray(literals.toArray(new AnnotationVisitable[0])), analyzer.rest());
        }

        analyzer.mark();
        while (analyzer.canMove() && analyzer.peek() != ' ' && analyzer.peek() != ',')
            analyzer.eat();
        StringBuilder toParse = new StringBuilder();
        int length = analyzer.cursor() - analyzer.getMark();
        analyzer.moveToMark();
        for (int i = 0; i < length; i++) toParse.append(analyzer.move());
        visitEnd(analyzer);
        String reference = toParse.toString();

        if (reference.equals("true"))
            return new Entry(name, new Constant(true), analyzer.rest());
        if (reference.equals("false"))
            return new Entry(name, new Constant(false), analyzer.rest());

        int lastDot = reference.lastIndexOf('.');
        if (lastDot == -1) throw new ParserException();
        if (lastDot == reference.length() - 1) throw new ParserException();
        Type type = TypeLookup.lookup(script, reference.substring(0, lastDot));
        String value = reference.substring(lastDot + 1);
        if (type == null) throw new ParserException();
        if (value.equals("class"))
            return new Entry(name, new ClassLiteral(type), analyzer.rest());
        if (type.getDescriptor().charAt(0) != 'L') throw new ParserException();
        return new Entry(name, new EnumConstant(type, value), analyzer.rest());
    }

    /**
     * Eats text at the end of the entry for parsing the next one.
     * @param analyzer analyzer
     */
    private static void visitEnd(Analyzer analyzer) {
        while (analyzer.canMove() && (analyzer.peek() == ' ' || analyzer.peek() == ','))
            analyzer.eat();
    }

    /**
     * Annotation parameter entry.
     * @param name name of the parameter
     * @param value value of the parameter
     * @param rest rest of the text to parse
     */
    private record Entry(@Nullable String name, AnnotationVisitable value, String rest) {
    }

}
