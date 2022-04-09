package me.pesekjak.hippo.classes;

import java.util.ArrayList;
import java.util.List;

public class ConstantArray {

    public List<Constant> constants = new ArrayList<>();

    public List<Constant> getConstants() {
        return constants;
    }

    public void addConstant(Constant constant) {
        constants.add(constant);
    }

    public void removeConstant(Constant constant) {
        constants.remove(constant);
    }

}
