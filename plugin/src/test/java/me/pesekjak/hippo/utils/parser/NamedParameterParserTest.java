package me.pesekjak.hippo.utils.parser;

import me.pesekjak.hippo.core.NamedParameter;
import me.pesekjak.hippo.core.PreImport;
import me.pesekjak.hippo.core.annotations.Annotation;
import me.pesekjak.hippo.utils.TypeLookup;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;
import org.skriptlang.skript.lang.script.Script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NamedParameterParserTest {

    @Test
    public void simpleTest() throws ParserException {
        Script script = getEmptyScript();
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));

        NamedParameter parameter = NamedParameterParser.parse("Blob blob", script);
        assert parameter.name().equals("blob");
        assert parameter.parameter().getType().getClassName().equals(Object.class.getName());
    }

    @Test
    public void arrayTest() throws ParserException {
        Script script = getEmptyScript();
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));

        NamedParameter parameter = NamedParameterParser.parse("Blob[][] blob", script);
        assert parameter.parameter().getType().getDescriptor().equals("[[Ljava/lang/Object;");
    }

    @Test
    public void annotationsTest() throws ParserException {
        Script script = getEmptyScript();
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));

        NamedParameter parameter = NamedParameterParser.parse("@Blob(), @Blob(blob = 5.1d) Blob blob", script);
        List<Annotation> annotations = new ArrayList<>(parameter.parameter().getAnnotations());
        assert annotations.size() == 2;
        assert annotations.get(0).getType().getClassName().equals(Object.class.getName());
        assert annotations.get(1).getType().getClassName().equals(Object.class.getName());
    }

    @Test
    public void primitiveTest() throws ParserException {
        Script script = getEmptyScript();
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));

        NamedParameter parameter = NamedParameterParser.parse("@Blob(blob = boolean.class) int blob", script);
        assert parameter.name().equals("blob");
        assert parameter.parameter().getType().getClassName().equals("int");
    }

    @Test
    public void annotationsTest2() throws ParserException {
        Script script = getEmptyScript();
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));

        NamedParameter parameter = NamedParameterParser.parse("@Blob(blob = boolean.class), @Blob, @Blob() int blob", script);
        assert parameter.parameter().getAnnotations().size() == 3;
    }

    @SuppressWarnings("ALL")
    public static Script getEmptyScript() {
        return new Script(null, Collections.emptyList());
    }

}
