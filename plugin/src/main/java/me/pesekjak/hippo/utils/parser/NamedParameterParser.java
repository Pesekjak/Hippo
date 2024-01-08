package me.pesekjak.hippo.utils.parser;

import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.parser.ParserInstance;
import me.pesekjak.hippo.core.NamedParameter;
import me.pesekjak.hippo.core.Parameter;
import me.pesekjak.hippo.core.annotations.Annotation;
import me.pesekjak.hippo.elements.SyntaxCommons;
import me.pesekjak.hippo.utils.SkriptUtil;
import me.pesekjak.hippo.utils.TypeLookup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.skriptlang.skript.lang.script.Script;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Parser for named parameters.
 */
public final class NamedParameterParser extends Parser<NamedParameter> {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile(SyntaxCommons.VARIABLE_NAME);

    @Override
    public NamedParameter parse(@NotNull String string, @NotNull ParseContext context) {
        Script script = SkriptUtil.getCurrentScript(ParserInstance.get());
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
    public @NotNull String toString(NamedParameter namedParameter, int flags) {
        return "parameter " + namedParameter.name() + " " + namedParameter.parameter().getType().getClassName();
    }

    @Override
    public @NotNull String toVariableNameString(NamedParameter namedParameter) {
        return "parameter(" + namedParameter.name() + "):" + namedParameter.parameter().getType().getClassName();
    }

    /**
     * Parses next named parameter for given script.
     * @param string text to parse
     * @param script script
     * @return next named parameter
     */
    public static NamedParameter parse(String string, Script script) throws ParserException {
        if (string.isEmpty()) throw new ParserException();

        String toParse = string.trim();

        List<Annotation> annotations = new ArrayList<>();
        while (true) {
            AnnotationEntry annotationEntry = nextAnnotation(toParse, script);
            toParse = annotationEntry.rest;
            if (annotationEntry.annotation == null) break;
            annotations.add(annotationEntry.annotation);
        }

        Analyzer analyzer = new Analyzer(toParse);
        while (analyzer.canMove() && analyzer.peek() == ' ')
            analyzer.eat();

        StringBuilder typeBuilder = new StringBuilder();
        while (analyzer.canMove() && analyzer.peek() != ' ')
            typeBuilder.append(analyzer.move());

        Type type = TypeLookup.lookup(script, typeBuilder.toString(), true);
        if (type == null) throw new ParserException();

        while (analyzer.canMove() && analyzer.peek() == ' ')
            analyzer.eat();

        if (analyzer.peek() >= 48 && analyzer.peek() <= 57)
            throw new ParserException();

        StringBuilder nameBuilder = new StringBuilder();
        while (analyzer.canMove() && analyzer.peek() != ' ' && analyzer.peek() != ',')
            nameBuilder.append(analyzer.move());

        if (!VARIABLE_PATTERN.matcher(nameBuilder.toString()).matches()) throw new ParserException();

        if (analyzer.canMove() && !analyzer.rest().isBlank()) throw new ParserException();

        return new NamedParameter(nameBuilder.toString(), new Parameter(type, annotations));
    }

    /**
     * Parses next annotation of the parameter.
     * @param string text to parse
     * @param script script
     * @return next annotation entry
     */
    private static AnnotationEntry nextAnnotation(String string, Script script) throws ParserException {
        Analyzer analyzer = new Analyzer(string);
        while (analyzer.canMove() && analyzer.peek() == ' ')
            analyzer.eat();
        if (!analyzer.canMove()) throw new ParserException();
        if (analyzer.peek() != '@') return new AnnotationEntry(null, analyzer.rest());
        analyzer.mark();
        int level = 0;
        while (analyzer.canMove()) {
            if (level == 0 && (analyzer.peek() == ' ' || analyzer.peek() == ',')) break;
            char next = analyzer.move();
            if (next == '(') level++;
            if (next == ')') level--;
        }
        if (level != 0) throw new ParserException();
        StringBuilder toParse = new StringBuilder();
        int length = analyzer.cursor() - analyzer.getMark();
        analyzer.moveToMark();
        for (int i = 0; i < length; i++) toParse.append(analyzer.move());
        visitEnd(analyzer);
        return new AnnotationEntry(AnnotationParser.parse(toParse.substring(0, toParse.length()), script), analyzer.rest());
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
     * Annotation entry.
     * @param annotation annotation
     * @param rest rest of the text to parse
     */
    private record AnnotationEntry(@Nullable Annotation annotation, String rest) {
    }

}
