package me.pesekjak.hippo.utils.parser;

import me.pesekjak.hippo.core.PreImport;
import me.pesekjak.hippo.utils.TypeLookup;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;
import org.skriptlang.skript.lang.script.Script;

import java.util.Collections;
import java.util.List;

public class SuperSignatureParserTest {

    @Test
    public void test() throws ParserException {
        Script script = getEmptyScript();
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));
        TypeLookup.registerPreImport(script, "Blub", new PreImport(Type.getType(Object.class)));

        List<Type> types = SuperSignatureParser.parse("Blob, Blub[], int[][], char, Something, Blob", script);
        assert types.size() == 6;
        assert types.get(0).getDescriptor().equals(Type.getDescriptor(Object.class));
        assert types.get(1).getDescriptor().equals(Type.getDescriptor(Object[].class));
        assert types.get(2).getDescriptor().equals(Type.getDescriptor(int[][].class));
        assert types.get(3).getDescriptor().equals(Type.getDescriptor(char.class));
        assert types.get(4) == null;
        assert types.get(5).getDescriptor().equals(Type.getDescriptor(Object.class));
    }

    @Test
    public void single() throws ParserException {
        Script script = getEmptyScript();
        TypeLookup.registerPreImport(script, "Blob", new PreImport(Type.getType(Object.class)));

        List<Type> types = SuperSignatureParser.parse("Blob", script);
        assert types.size() == 1;
        assert types.get(0).getDescriptor().equals(Type.getDescriptor(Object.class));
    }

    @SuppressWarnings("ALL")
    public static Script getEmptyScript() {
        return new Script(null, Collections.emptyList());
    }

}
