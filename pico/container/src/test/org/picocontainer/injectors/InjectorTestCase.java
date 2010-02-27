package org.picocontainer.injectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;
import org.picocontainer.*;
import static org.picocontainer.injectors.Injector.constructor;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.parameters.ConstantParameter;
import org.picocontainer.tck.AbstractComponentAdapterTest;
import org.picocontainer.testmodel.NullLifecycle;
import org.picocontainer.testmodel.RecordingLifecycle;
import org.picocontainer.visitors.AbstractPicoVisitor;

/**
 * test capabilities of injector factory. as this is mostly convenience wrapper around
 * constructors, we just test that everything was passed through
 *
 * @author ko5tik
 */
public class InjectorTestCase {

    final Object key = new Object();
    final Parameter checked = new ConstantParameter(null);
    final Parameter[] checkedArray = new Parameter[]{checked};
    final ComponentMonitor monitor = new NullComponentMonitor();


    final PicoVisitor parameterChecker = new AbstractPicoVisitor() {


        public boolean visitContainer(PicoContainer pico) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void visitComponentAdapter(ComponentAdapter<?> componentAdapter) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void visitComponentFactory(ComponentFactory componentFactory) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void visitParameter(Parameter parameter) {
            assertSame(checked, parameter);
        }
    };

    /**
     * test that all parameters were passed to respective constructor
     */
    @Test
    public void testSimpleConstructor() {
        ComponentAdapter adapter = constructor(key, getClass(), checked);
        assertSame(key, adapter.getComponentKey());
        assertSame(getClass(), adapter.getComponentImplementation());
        adapter.accept(parameterChecker);
    }
}
