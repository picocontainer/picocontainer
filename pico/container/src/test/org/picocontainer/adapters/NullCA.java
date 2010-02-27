package org.picocontainer.adapters;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;

import java.lang.reflect.Type;

public class NullCA implements ComponentAdapter {

    private Object key;

    public NullCA(Object key) {
        this.key = key;
    }

    public Object getComponentKey() {
        return key;
    }

    public Class getComponentImplementation() {
        return NOTHING.class;
    }

    public Object getComponentInstance(PicoContainer container)  {
        return null;
    }

    public Object getComponentInstance(PicoContainer container, Type into)  {
        return null;
    }

    public void verify(PicoContainer container)  {
    }

    public void accept(PicoVisitor visitor) {
    }

    public ComponentAdapter getDelegate() {
        return null;
    }

    public ComponentAdapter findAdapterOfType(Class adapterType) {
        return null;
    }

    public String getDescriptor() {
        return "Null-CA";
    }
}
