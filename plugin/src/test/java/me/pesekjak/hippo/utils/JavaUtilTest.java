package me.pesekjak.hippo.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class JavaUtilTest {

    @Test
    public void closestClass() {
        Class<?> clazz = JavaUtil.getClosestSuperClass(LinkedList.class, ArrayList.class, HashSet.class);
        assert clazz.equals(java.util.AbstractCollection.class);
        clazz = JavaUtil.getClosestSuperClass(LinkedList.class, ArrayList.class, HashSet.class, List.class);
        assert clazz.equals(Object.class);
        clazz = JavaUtil.getClosestSuperClass(ArrayList.class);
        assert clazz.equals(ArrayList.class);
    }

}
