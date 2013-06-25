package com.picocontainer.injectors;

import static com.picocontainer.injectors.Injector.constructor;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentFactory;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.Parameter;
import com.picocontainer.PicoContainer;
import com.picocontainer.PicoVisitor;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.parameters.ConstantParameter;
import com.picocontainer.visitors.AbstractPicoVisitor;

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


        public boolean visitContainer(final PicoContainer pico) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void visitComponentAdapter(final ComponentAdapter<?> componentAdapter) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void visitComponentFactory(final ComponentFactory componentFactory) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void visitParameter(final Parameter parameter) {
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
