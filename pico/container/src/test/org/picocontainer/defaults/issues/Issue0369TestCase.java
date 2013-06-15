package org.picocontainer.defaults.issues;

import static junit.framework.Assert.assertSame;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;

public class Issue0369TestCase {

    @Test
    public void simpleProofOfChangeInGetComponent() {
        MyAdapter mya = new MyAdapter();
        Class<? extends List> impl = mya.getComponentImplementation();
        assertSame(ArrayList.class, impl);
    }

    public class MyAdapter implements ComponentAdapter<List> {

        private final Class<? extends List> implementationclass = ArrayList.class;

        public Class<? extends List> getComponentImplementation() {
            return implementationclass;
        }

        public Object getComponentKey() {
            return null;
        }

        public List getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
            return null;
        }

        public void verify(final PicoContainer container) throws PicoCompositionException {

        }

        public void accept(final PicoVisitor visitor) {

        }

        public ComponentAdapter<List> getDelegate() {
            return null;
        }

        public <U extends ComponentAdapter> U findAdapterOfType(final Class<U> adapterType) {
            return null;
        }

        public String getDescriptor() {
            return null;
        }
    }

}