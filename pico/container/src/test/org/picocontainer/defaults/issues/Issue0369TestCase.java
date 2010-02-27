package org.picocontainer.defaults.issues;

import org.junit.Test;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertSame;

public class Issue0369TestCase {

    @Test
    public void simpleProofOfChangeInGetComponent() {
        MyAdapter mya = new MyAdapter();
        Class<? extends List> componentImplementation = mya.getComponentImplementation();
        assertSame(ArrayList.class, componentImplementation);
    }

    public class MyAdapter implements ComponentAdapter<List> {

        private Class<? extends List> implementationclass = ArrayList.class;

        public Class<? extends List> getComponentImplementation() {
            return implementationclass;
        }

        public Object getComponentKey() {
            return null;
        }

        public List getComponentInstance(PicoContainer container) throws PicoCompositionException {
            return null;
        }

        public List getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            return null;
        }

        public void verify(PicoContainer container) throws PicoCompositionException {

        }

        public void accept(PicoVisitor visitor) {

        }

        public ComponentAdapter<List> getDelegate() {
            return null;
        }

        public <U extends ComponentAdapter> U findAdapterOfType(Class<U> adapterType) {
            return null;
        }

        public String getDescriptor() {
            return null;
        }
    }

}