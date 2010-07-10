package org.picocontainer.injectors;

import org.junit.Test;
import org.picocontainer.*;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.monitors.WriterComponentMonitor;
import org.picocontainer.tck.AbstractComponentAdapterTest;
import org.picocontainer.testmodel.SimpleTouchable;
import org.picocontainer.testmodel.Touchable;

import java.io.PrintWriter;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LifecycleAdapterTestCase {

    private final ConstructorInjection.ConstructorInjector INJECTOR = new ConstructorInjection.ConstructorInjector(
            Foo.class, Foo.class, new NullComponentMonitor(), false, new Parameter[0]
   );

    private AbstractComponentAdapterTest.RecordingLifecycleStrategy strategy = new AbstractComponentAdapterTest.RecordingLifecycleStrategy(new StringBuffer());

    AbstractInjectionType ais = new AbstractInjectionType() {
        public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor, LifecycleStrategy lifecycle, Properties componentProps, Object key, Class<T> impl, Parameter... parameters) throws PicoCompositionException {
            return wrapLifeCycle(INJECTOR, lifecycle);
        }
    };

    @Test
    public void passesOnLifecycleOperations() {

        LifecycleStrategy adapter = (LifecycleStrategy) ais.createComponentAdapter(new NullComponentMonitor(), strategy, new Properties(), null, null, new Parameter[0]);
        assertEquals("org.picocontainer.injectors.AbstractInjectionType$LifecycleAdapter", adapter.getClass().getName());
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
