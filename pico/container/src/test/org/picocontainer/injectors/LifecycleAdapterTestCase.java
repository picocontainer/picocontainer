package org.picocontainer.injectors;

import org.junit.Test;
import org.picocontainer.*;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.monitors.WriterComponentMonitor;
import org.picocontainer.tck.AbstractComponentAdapterTest;
import org.picocontainer.testmodel.NullLifecycle;
import org.picocontainer.testmodel.SimpleTouchable;
import org.picocontainer.testmodel.Touchable;

import java.io.PrintWriter;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LifecycleAdapterTestCase {

    private final ConstructorInjector INJECTOR = new ConstructorInjector(
            Foo.class, Foo.class, new Parameter[0],
            new NullComponentMonitor(), false);

    private AbstractComponentAdapterTest.RecordingLifecycleStrategy strategy = new AbstractComponentAdapterTest.RecordingLifecycleStrategy(new StringBuffer());

    AbstractInjectionFactory ais = new AbstractInjectionFactory() {
        public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor componentMonitor, LifecycleStrategy lifecycleStrategy, Properties componentProperties, Object componentKey, Class<T> componentImplementation, Parameter... parameters) throws PicoCompositionException {
            return wrapLifeCycle(INJECTOR, lifecycleStrategy);
        }
    };

    @Test
    public void passesOnLifecycleOperations() {

        LifecycleStrategy adapter = (LifecycleStrategy) ais.createComponentAdapter(new NullComponentMonitor(), strategy, new Properties(), null, null, new Parameter[0]);
        assertEquals("org.picocontainer.injectors.AbstractInjectionFactory$LifecycleAdapter", adapter.getClass().getName());
        Touchable touchable = new SimpleTouchable();
        adapter.start(touchable);
        adapter.stop(touchable);
        adapter.dispose(touchable);
        assertEquals("<start<stop<dispose", strategy.recording());
    }

    @Test
    public void canHaveMonitorChanged() {
        ComponentMonitorStrategy adapter = (ComponentMonitorStrategy) ais.createComponentAdapter(new NullComponentMonitor(), strategy, new Properties(), Foo.class, Foo.class, new Parameter[0]);
        assertTrue(adapter.currentMonitor() instanceof NullComponentMonitor);
        adapter.changeMonitor(new WriterComponentMonitor(new PrintWriter(System.out)));
        assertTrue(adapter.currentMonitor() instanceof WriterComponentMonitor);

    }

    public static class Foo implements Startable {
        public void start() {
        }
        public void stop() {
        }
    }

}
