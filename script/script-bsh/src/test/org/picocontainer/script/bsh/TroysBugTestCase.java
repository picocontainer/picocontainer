package org.picocontainer.script.bsh;

import org.picocontainer.script.AbstractScriptedContainerBuilderTestCase;
import org.picocontainer.script.LifecycleMode;
import org.picocontainer.script.ScriptedContainerBuilder;
import org.picocontainer.PicoContainer;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.ComponentFactory;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.lifecycle.StartableLifecycleStrategy;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.monitors.ConsoleComponentMonitor;
import org.picocontainer.injectors.ConstructorInjection;
import org.junit.Test;import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.Reader;
import java.io.StringReader;

public class TroysBugTestCase extends AbstractScriptedContainerBuilderTestCase {

    @Test
    public void testTroysBug() {

        Reader script = new StringReader(
                "//pico = new org.picocontainer.DefaultPicoContainer(new org.picocontainer.behaviors.Caching(), parent);\n" +
                "pico = parent.makeChildContainer();\n" +
                "pico.setName(\"child\");\n" +
                "// add dependencies...\n" +
                "pico.addComponent("+Thing.class.getName()+".class, "+ThingImpl.class.getName()+".class, null);");

        ScriptedContainerBuilder builder = new BeanShellContainerBuilder(script, AbstractScriptedContainerBuilderTestCase.class.getClassLoader(), LifecycleMode.AUTO_LIFECYCLE);

        ComponentFactory componentFactory = new Caching().wrap(new ConstructorInjection());
        ComponentMonitor componentMonitor = new ConsoleComponentMonitor();
        LifecycleStrategy lifecycleStrategy = new CustomLifecycleStrategy(componentMonitor); // starts/stops CustomStartable

        DefaultPicoContainer parent = new DefaultPicoContainer(componentFactory, lifecycleStrategy, null, componentMonitor);
        parent.setName("parent");
        PicoContainer container = builder.buildContainer(parent, "defaultScope", true);

        ThingImpl thing = (ThingImpl) container.getComponent(Thing.class); // not started

        assertTrue(thing.started);

    }

    public static interface CustomStartable {
        void sstart();
        void sstop(); 
    }

    public static class CustomLifecycleStrategy extends StartableLifecycleStrategy {
        public CustomLifecycleStrategy(ComponentMonitor componentMonitor) {
            super(componentMonitor);
        }

        protected Class getStartableInterface() {
            return CustomStartable.class;
        }

        protected String getStopMethodName() {
            return "sstop";
        }

        protected String getStartMethodName() {
            return "sstart";
        }
    }


    public static interface Thing extends CustomStartable {
    }

    public static class ThingImpl implements TroysBugTestCase.Thing {
        public boolean started = false;
        public boolean stopped = false;
        public void sstart() {
            started = true;
        }

        public void sstop() {
            stopped = true;
        }
    }


}
