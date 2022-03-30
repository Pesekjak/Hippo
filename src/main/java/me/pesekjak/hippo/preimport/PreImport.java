package me.pesekjak.hippo.preimport;

import me.pesekjak.hippo.classes.Type;

public record PreImport(Type preImportType) {

    public Type getType() {
        return preImportType;
    }

}
