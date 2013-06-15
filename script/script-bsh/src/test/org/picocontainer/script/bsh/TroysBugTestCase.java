package org.picocontainer.script.bsh;

import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;
import org.picocontainer.ComponentFactory;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.lifecycle.StartableLifecycleStrategy;
import org.picocontainer.monitors.ConsoleComponentMonitor;
import org.picocontainer.script.AbstractScriptedContainerBuilderTestCase;
import org.picocontainer.script.ContainerBuilder;

public class TroysBugTestCase extends AbstractScriptedContainerBuilderTestCase {

    @Test
    public void testTroysBug() {

        Reader script = new StringReader(
                "//pico = new org.picocontainer.DefaultPicoContainer(new org.picocontainer.behaviors.Caching(), parent);\n" +
                "pico = parent.makeChildContainer();\n" +
                "pico.setName(\"child\");\n" +
                "// add dependencies...\n" +
                "pico.addComponent("+Thing.class.getName()+".class, "+ThingImpl.class.getName()+".class, null);");

        ContainerBuilder builder = new BeanShellContainerBuilder(script, AbstractScriptedContainerBuilderTestCase.class.getClassLoader());

        ComponentFactory componentFactory = new Caching().wrap(new ConstructorInjection());
        ComponentMonitor monitor = new ConsoleComponentMonitor();
        LifecycleStrategy lifecycle = new CustomLifecycleStrategy(monitor); // starts/stops CustomStartable

        DefaultPicoContainer parent = new DefaultPicoContainer(null, lifecycle, monitor, componentFactory);
        parent.setName("parent");
        PicoContainer container = builder.buildContainer(parent, "defaultScope", true);

        ThingImpl thing = (ThingImpl) container.getComponent(Thing.class); // not started

        assertTrue(thing.started);

    }

    public static interface CustomStartable {
        void sstart();
        void sstop();
    }

    @SuppressWarnings("serial")
	public static class CustomLifecycleStrategy extends StartableLifecycleStrategy {
        public CustomLifecycleStrategy(final ComponentMonitor monitor) {
            super(monitor);
        }

        @Override
		protected Class<?> getStartableInterface() {
            return CustomStartable.class;
        }

        @Override
		protected String getStopMethodName() {
            return "sstop";
        }

        @Override
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
