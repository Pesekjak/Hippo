package me.pesekjak.hippo.classes;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Modifiable extends Annotatable {

    @Getter
    private final List<Modifier> modifiers = new ArrayList<>();

    public void addModifiers(Modifier... modifiers) {
        this.modifiers.addAll(Arrays.asList(modifiers));
    }

}
