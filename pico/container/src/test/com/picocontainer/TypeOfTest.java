package com.picocontainer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertNotSame;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.picocontainer.TypeOf;

public class TypeOfTest {

    @Test
    public void typeErasureSidesteppedWithGenericSubclass() {
        TypeOf<Set<Color>> blort = new TypeOf<Set<Color>>(){};
        assertEquals("java.util.Set<java.awt.Color>", blort.getType().toString());
    }

    @Test
    public void canUseRealClassesToo() {
        TypeOf<Color> blort = new TypeOf<Color>(){};
        assertEquals(Color.class, blort.getType());
    }

    @Test
    public void factoryMethodMakesTypeOfForRegularNonGenericClasses() {
        TypeOf<Color> blort = TypeOf.fromClass(Color.class);
        assertSame(Color.class, blort.getType());
    }

    @Test
    public void typeOflooksGoodInAMethodDeclaration() {
        StringBuffer sb = new StringBuffer();
        Set<List<Color>> setOfListOfColors = getComponent(new TypeOf<List<Color>>(){}, sb);
        assertEquals("java.util.List<java.awt.Color>", sb.toString());
        List<Color> reds = new ArrayList<Color>();
        reds.add(Color.red);
        reds.add(Color.orange);
        setOfListOfColors.add(reds);
    }

    /* Symbolic of Pico's future/shortly getComponent(..) */
    public static <T> Set<T> getComponent(final TypeOf<T> foos, final StringBuffer sb) {
        sb.append(foos.getType().toString());
        return new HashSet<T>();
    }

    @Test
    public void oldWaylooksGoodInAMethodDeclaration() {
        StringBuffer sb = new StringBuffer();
        Set<Color> setOfColors = getComponent(Color.class, sb);
        assertEquals("class java.awt.Color", sb.toString());

        setOfColors.add(Color.red);
        setOfColors.add(Color.orange);
    }

    /* Symbolic of Pico's existing getComponent(..) */
    public static <T> Set<T> getComponent(final Class<T> foos, final StringBuffer sb) {
        // and can delegate
        return getComponent(TypeOf.fromClass(foos), sb);
    }

    @Test
    public void hashCodeWorks() {
        assertEquals(TypeOf.fromClass(Set.class).hashCode(), TypeOf.fromClass(Set.class).hashCode());
    }

    @Test
    public void equalsWorks() {
        assertNotSame(TypeOf.fromClass(Set.class), TypeOf.fromClass(Set.class));
        assertEquals(TypeOf.fromClass(Set.class), TypeOf.fromClass(Set.class));
    }

}
