package org.picocontainer.adapters;

import java.lang.reflect.Type;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;

public class NullCA implements ComponentAdapter {

    private final Object key;

    public NullCA(final Object key) {
        this.key = key;
    }

    public Object getComponentKey() {
        return key;
    }

    public Class getComponentImplementation() {
        return NOTHING.class;
    }

    public Object getComponentInstance(final PicoContainer container, final Type into)  {
        return null;
    }

    public void verify(final PicoContainer container)  {
    }

    public void accept(final PicoVisitor visitor) {
    }

    public ComponentAdapter getDelegate() {
        return null;
    }

    public ComponentAdapter findAdapterOfType(final Class adapterType) {
        return null;
    }

    public String getDescriptor() {
        return "Null-CA";
    }
}
