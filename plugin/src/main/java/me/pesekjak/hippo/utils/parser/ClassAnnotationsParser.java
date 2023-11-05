package me.pesekjak.hippo.utils.parser;

import me.pesekjak.hippo.core.annotations.Annotation;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.util.ArrayList;
import java.util.List;

public final class ClassAnnotationsParser {

    private ClassAnnotationsParser() {
        throw new UnsupportedOperationException();
    }

    /**
     * Parses next annotations.
     * @param string text to parse
     * @param script script
     * @return parsed annotations
     */
    public static List<Annotation> parse(String string, Script script) throws ParserException {
        if (string.isEmpty()) throw new ParserException();

        String toParse = string.trim();

        List<Annotation> annotations = new ArrayList<>();
        while (true) {
            if (toParse.isEmpty()) break;
            AnnotationEntry annotationEntry = nextAnnotation(toParse, script);
            toParse = annotationEntry.rest.trim();
            if (annotationEntry.annotation == null) break;
            annotations.add(annotationEntry.annotation);
        }

        return annotations;
    }

    /**
     * Parses next annotation entry.
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
