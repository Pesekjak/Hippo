package me.pesekjak.hippo.utils.parser;

import me.pesekjak.hippo.core.PreImport;
import me.pesekjak.hippo.core.annotations.Annotation;
import me.pesekjak.hippo.core.annotations.Constant;
import me.pesekjak.hippo.utils.TypeLookup;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.io.File;
import java.util.List;

public class ClassAnnotationsParserTest {

    @Test
    public void simpleTest() throws ParserException {
        File script = new File("script.sk");
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));
        TypeLookup.registerPreImport(script, "Blub", new PreImport(Type.getType(Object.class)));

        List<Annotation> annotation = ClassAnnotationsParser.parse("@Blob(blob = 15.5d, blub = 10), @Blub, @Blub(), @Blob", script);
        assert annotation.size() == 4;
        assert annotation.get(0).getType().getDescriptor().equals(Type.getDescriptor(Object.class));
        assert (int) ((Constant) annotation.get(0).getValues().get("blub")).get() == 10;
        assert annotation.get(1).getType().getDescriptor().equals(Type.getDescriptor(Object.class));
        assert annotation.get(2).getType().getDescriptor().equals(Type.getDescriptor(Object.class));
        assert annotation.get(3).getType().getDescriptor().equals(Type.getDescriptor(Object.class));
    }

}
