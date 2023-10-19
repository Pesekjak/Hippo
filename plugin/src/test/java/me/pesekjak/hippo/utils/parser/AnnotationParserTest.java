package me.pesekjak.hippo.utils.parser;

import me.pesekjak.hippo.core.PreImport;
import me.pesekjak.hippo.core.annotations.*;
import me.pesekjak.hippo.utils.TypeLookup;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AnnotationParserTest {

    @Test
    public void test() throws ParserException {
        File script = new File("script.sk");
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));

        Annotation annotation = AnnotationParser.parse("@Blob", script);
        assert annotation.getType().getClassName().equals(Object.class.getName());
    }

    @Test
    public void stringTest() throws ParserException {
        File script = new File("script.sk");
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));

        Annotation annotation = AnnotationParser.parse("@Blob(blob = \"blob\", blub = \"blub \"\" <- quote\")", script);
        assert ((Constant) annotation.getValues().get("blob")).get().equals("blob");
        assert ((Constant) annotation.getValues().get("blub")).get().equals("blub \" <- quote");
    }

    @Test
    public void numberTest() throws ParserException {
        File script = new File("script.sk");
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));

        Annotation annotation = AnnotationParser.parse("@Blob(blob = 15.5d, blub = 10)", script);
        assert ((Number) ((Constant) annotation.getValues().get("blob")).get()).doubleValue() == 15.5d;
        assert ((Number) ((Constant) annotation.getValues().get("blub")).get()).intValue() == 10;
    }

    @Test
    public void nestedTest() throws ParserException {
        File script = new File("script.sk");
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));

        Annotation annotation = AnnotationParser.parse("@Blob(blob = @Blob(blob = 5), blub = @Blob)", script);
        Annotation blobAnnotation = (Annotation) annotation.getValues().get("blob");
        assert blobAnnotation.getType().getClassName().equals(Object.class.getName());
        assert ((Number) ((Constant) blobAnnotation.getValues().get("blob")).get()).intValue() == 5;
        assert ((Annotation) annotation.getValues().get("blub")).getType().getClassName().equals(Object.class.getName());
    }

    @Test
    public void arrayTest() throws ParserException {
        File script = new File("script.sk");
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));

        Annotation annotation = AnnotationParser.parse("@Blob(blob = [1, 2, 3, \"blob\"], blub = [])", script);
        List<Object> first = List.of(((LiteralsArray) annotation.getValues().get("blob")).content().toArray(new Object[0]));
        assert ((Number) ((Constant) first.get(0)).get()).intValue() == 1;
        assert ((Number) ((Constant) first.get(1)).get()).intValue() == 2;
        assert ((Number) ((Constant) first.get(2)).get()).intValue() == 3;
        assert ((Constant) first.get(3)).get().equals("blob");
        assert ((LiteralsArray) annotation.getValues().get("blub")).content().isEmpty();
    }

    @Test
    public void enumTest() throws ParserException {
        File script = new File("script.sk");
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));

        Annotation annotation = AnnotationParser.parse("@Blob(blob = Blob.BLUB)", script);
        assert ((EnumConstant) annotation.getValues().get("blob")).name().equals("BLUB");
    }

    @Test
    public void classTest() throws ParserException {
        File script = new File("script.sk");
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));

        Annotation annotation = AnnotationParser.parse("@Blob(blob = Blob.class)", script);
        assert ((ClassLiteral) annotation.getValues().get("blob")).type().getClassName().equals(Object.class.getName());
    }

    @Test
    public void moreNested() throws ParserException {
        File script = new File("script.sk");
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));
        TypeLookup.registerPreImport(script, "Blub", new PreImport(Type.getType(Object.class)));

        Annotation annotation = AnnotationParser.parse("@Blob( blob = Blob.class, blub = @Blub(blob = [ \"hi\",  Blob.class]))", script);
        List<Object> blob = new ArrayList<>(((LiteralsArray) ((Annotation) annotation.getValues().get("blub")).getValues().get("blob")).content());
        assert ((ClassLiteral) blob.get(1)).type().getClassName().equals(Object.class.getName());
    }

    @Test
    public void cursedStrings() throws ParserException {
        File script = new File("script.sk");
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));

        Annotation annotation = AnnotationParser.parse("@Blob(blub = @Blob(blob = \"(()\"))", script);
        assert ((Constant) ((Annotation) annotation.getValues().get("blub")).getValues().get("blob")).get().equals("(()");
    }

    @Test
    public void primitiveClassLiteral() throws ParserException {
        File script = new File("script.sk");
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));

        Annotation annotation = AnnotationParser.parse("@Blob(blub = int.class, blob = boolean.class)", script);
        assert ((ClassLiteral) annotation.getValues().get("blub")).type().getClassName().equals("int");
        assert ((ClassLiteral) annotation.getValues().get("blob")).type().getClassName().equals("boolean");
    }

    @Test
    public void arrayClassLiteral() throws ParserException {
        File script = new File("script.sk");
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));

        Annotation annotation = AnnotationParser.parse("@Blob(blub = int[].class, blob = Blob[][].class)", script);
        assert ((ClassLiteral) annotation.getValues().get("blub")).type().getDescriptor().equals("[I");
        assert ((ClassLiteral) annotation.getValues().get("blob")).type().getDescriptor().equals("[[Ljava/lang/Object;");
    }

    @Test
    public void characterTest() throws ParserException {
        File script = new File("script.sk");
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));

        Annotation annotation = AnnotationParser.parse("@Blob(blub = 'a', blob = 'b')", script);
        assert ((Constant) annotation.getValues().get("blub")).<Character>get() == 'a';
        assert ((Constant) annotation.getValues().get("blob")).<Character>get() == 'b';
    }

    @Test
    public void booleanTest() throws ParserException {
        File script = new File("script.sk");
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));

        Annotation annotation = AnnotationParser.parse("@Blob(blub = true, blob = false)", script);
        assert ((Constant) annotation.getValues().get("blub")).<Boolean>get();
        assert !((Constant) annotation.getValues().get("blob")).<Boolean>get();
    }

}
