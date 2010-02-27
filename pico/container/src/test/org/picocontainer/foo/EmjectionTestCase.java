package org.picocontainer.foo;

import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.Emjection;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.Caching;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertNotSame;
import static org.picocontainer.Characteristics.EMJECTION_ENABLED;

public class EmjectionTestCase {

    @Test
    public void basicEmjection() {
        StringBuilder sb = new StringBuilder();
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(Antelope.class);
        pico.as(EMJECTION_ENABLED).addComponent(ZooKeeper.class);
        pico.addComponent(sb);
        ZooKeeper zooKeeper = pico.getComponent(ZooKeeper.class);
        zooKeeper.doHeadCount();
        assertEquals("giraffe=true, antelope=true", sb.toString());
    }


    @Test
    public void testThatTransientNatureOfAdhocDependencies() {
        MutablePicoContainer pico = new DefaultPicoContainer(new Caching());
        pico.addComponent(Antelope.class);
        pico.as(EMJECTION_ENABLED).addComponent(ZooKeeper.class);
        pico.addComponent(new StringBuilder());
        ZooKeeper component = pico.getComponent(ZooKeeper.class);
        component.doHeadCount();
        Zoo zoo1 = component.zoo;
        component = pico.getComponent(ZooKeeper.class);
        component.doHeadCount();
        Zoo zoo2 = component.zoo;
        zoo2.headCount();
        assertNotSame(zoo1, zoo2); // made one the fly
        assertNotSame(zoo1.giraffe, zoo2.giraffe); // made one the fly
        assertSame(zoo1.antelope, zoo2.antelope); // in the parent-most picocontainer
        assertSame(zoo1.sb, zoo2.sb); // in the parent-most picocontainer
    }


    public static class Zoo {
        private final Emjection emjection = new Emjection();

        private Giraffe giraffe;
        private Antelope antelope;
        private StringBuilder sb;

        public Zoo(Giraffe giraffe, Antelope antelope, StringBuilder sb) {
            this.giraffe = giraffe;
            this.antelope = antelope;
            this.sb = sb;
        }

        public void headCount() {
            sb.append("giraffe=").append(giraffe != null);
            sb.append(", antelope=").append(antelope != null);
        }
    }


    public static class ZooKeeper {

        private final Emjection emjection = new Emjection();
        private Zoo zoo;

        public void doHeadCount() {
            zoo = neu(Zoo.class, new Giraffe());
            zoo.headCount();
        }

        <T> T neu(Class<T> type, Object... args) {
            return Emjection.neu(type, emjection, args);
        }

    }

    public static class Giraffe {
    }
    public static class Antelope {
    }


}
