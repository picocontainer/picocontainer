package org.picocontainer.defaults.issues;

import org.junit.Test;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.Characteristics;

import org.picocontainer.injectors.SetterInjection;
import static junit.framework.Assert.assertEquals;

public class Issue0316TestCase {

    //@Test
    public void testGood() {
        doTest(GoodTarget.class);
    }
    
    @Test
    public void testBad() {
        doTest(BadTarget.class);
    }
    
    
    private void doTest(Class targetClass) {
        MutablePicoContainer pico = new PicoBuilder(new SetterInjection("inject")).build();
        pico.addComponent("fruit1", Apple.class);
        pico.addComponent("fruit2", Pear.class);
        pico.addComponent(new Integer(42));
        pico.as(Characteristics.USE_NAMES).addComponent(targetClass);
        
        Target target = (Target)pico.getComponent(targetClass);
        assertEquals("apple", target.eat1());
        assertEquals("pear", target.eat2());
        assertEquals(42, target.getNumber());
    }
    
    
    public static interface Fruit {
        public String eat();
    }
    public static class Apple implements Fruit {
        public String eat() { return "apple"; }
    }
    public static class Pear implements Fruit {
        public String eat() { return "pear"; }
    }
    
    public static interface Target {
        String eat1();
        String eat2();
        int getNumber();
    }
    
    public static class GoodTarget implements Target {
        private Fruit fruit1;
        private Fruit fruit2;
        private Integer number;

        public void injectNumber(Integer number)  {
            this.number = number;
        }

        public void injectFruit1(Fruit fruit1) {
            this.fruit1 = fruit1;
        }

        public void injectFruit2(Fruit fruit2) {
            this.fruit2 = fruit2;
        }
        public int getNumber() {
            return number.intValue();
        }
        
        public String eat1() { return fruit1.eat(); }
        public String eat2() { return fruit2.eat(); }
    }
    
    public static class BadTarget implements Target {
        private Fruit fruit1;
        private Fruit fruit2;
        private Integer number;

        public void injectFruit1(Fruit fruit1) {
            this.fruit1 = fruit1;
        }

        public void injectFruit2(Fruit fruit2) {
            this.fruit2 = fruit2;
        }
        
        public void injectNumber(Integer number)  {
            this.number = number;
        }

        public int getNumber() {
            return number.intValue();
        }
        
        public String eat1() { return fruit1.eat(); }
        public String eat2() { return fruit2.eat(); }
    }
    
}