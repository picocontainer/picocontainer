package com.picocontainer.injectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.util.Properties;

import org.junit.Test;
import com.picocontainer.tck.AbstractComponentAdapterTest;
import com.picocontainer.testmodel.SimpleTouchable;
import com.picocontainer.testmodel.Touchable;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.ComponentMonitorStrategy;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.Parameter;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.Startable;
import com.picocontainer.injectors.AbstractInjectionType;
import com.picocontainer.injectors.ConstructorInjection;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.monitors.WriterComponentMonitor;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;

public class LifecycleAdapterTestCase {

    private final ConstructorInjection.ConstructorInjector INJECTOR = new ConstructorInjection.ConstructorInjector(
            Foo.class, Foo.class, new ConstructorParameters(new Parameter[0])
   );

    private final AbstractComponentAdapterTest.RecordingLifecycleStrategy strategy = new AbstractComponentAdapterTest.RecordingLifecycleStrategy(new StringBuffer());

    AbstractInjectionType ais = new AbstractInjectionType() {
        public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle, final Properties componentProps, final Object key, final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {
            return wrapLifeCycle(INJECTOR, lifecycle);
        }
    };

    @Test
    public void passesOnLifecycleOperations() {

        LifecycleStrategy adapter = (LifecycleStrategy) ais.createComponentAdapter(new NullComponentMonitor(), strategy, new Properties(), null, null, null, null, null);
        assertEquals("com.picocontainer.injectors.AbstractInjectionType$LifecycleAdapter", adapter.getClass().getName());
        Touchable touchable = new SimpleTouchable();
        adapter.start(touchable);
        adapter.stop(touchable);
        adapter.dispose(touchable);
        assertEquals("<start<stop<dispose", strategy.recording());
    }

    @Test
    public void canHaveMonitorChanged() {
        ComponentMonitorStrategy adapter = (ComponentMonitorStrategy) ais.createComponentAdapter(new NullComponentMonitor(), strategy, new Properties(), Foo.class, Foo.class, null, null, null);
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
