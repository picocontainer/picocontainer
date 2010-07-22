package org.picocontainer.containers;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Converters;
import org.picocontainer.Converting;
import org.picocontainer.NameBinding;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.PicoVisitor;
import org.picocontainer.TypeOf;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

/**
 * Abstract base class for <i>immutable<i> delegation to a PicoContainer
 * 
 * @author Konstantin Pribluda
 * 
 */
@SuppressWarnings("serial")
public abstract class AbstractDelegatingPicoContainer implements PicoContainer, Converting, Serializable{

    private PicoContainer delegate;

    public AbstractDelegatingPicoContainer(PicoContainer delegate) {
		if (delegate == null) {
			throw new NullPointerException(
					"PicoContainer delegate must not be null");
		}
		this.delegate = delegate;
	}

	public final void accept(PicoVisitor visitor) {
        visitor.visitContainer(this);
        delegate.accept(visitor);
	}


	@Override
    public boolean equals(Object obj) {
		// required to make it pass on both jdk 1.3 and jdk 1.4. Btw, what about
		// overriding hashCode()? (AH)
		return delegate.equals(obj) || this == obj;
	}

	public <T> T getComponentInto(Class<T> componentType, Type into) {
		return componentType.cast(getComponentInto((Object) componentType, into));
	}

    public <T> T getComponentInto(TypeOf<T> componentType, Type into) {
        return (T) getComponentInto((Object) componentType, into);
    }

    public <T> T getComponent(Class<T> componentType, Class<? extends Annotation> binding, Type into) {
        return delegate.getComponent(componentType, binding, into);
    }

    public <T> T getComponent(Class<T> componentType, Class<? extends Annotation> binding) {
        return delegate.getComponent(componentType, binding);
    }

    public Object getComponent(Object keyOrType) {
        return getComponentInto(keyOrType, ComponentAdapter.NOTHING.class);
    }

    public Object getComponentInto(Object keyOrType, Type into) {
        return delegate.getComponentInto(keyOrType, into);
    }

    public <T> T getComponent(Class<T> componentType) {
        return getComponentInto(TypeOf.fromClass(componentType), ComponentAdapter.NOTHING.class);
    }

    public <T> T getComponent(TypeOf<T> componentType) {
        return delegate.getComponent(componentType);
    }

    public <T> ComponentAdapter<T> getComponentAdapter(Class<T> componentType, NameBinding componentNameBinding) {
        return delegate.getComponentAdapter(TypeOf.fromClass(componentType), componentNameBinding);
    }

    public <T> ComponentAdapter<T> getComponentAdapter(TypeOf<T> componentType,
			NameBinding componentNameBinding) {
		return delegate.getComponentAdapter(componentType, componentNameBinding);
	}

    public <T> ComponentAdapter<T> getComponentAdapter(Class<T> componentType, Class<? extends Annotation> binding) {
        return delegate.getComponentAdapter(TypeOf.fromClass(componentType), binding);
    }

    public <T> ComponentAdapter<T> getComponentAdapter(TypeOf<T> componentType, Class<? extends Annotation> binding) {
        return delegate.getComponentAdapter(componentType, binding);
    }

    public ComponentAdapter<?> getComponentAdapter(Object key) {
		return delegate.getComponentAdapter(key);
	}

	public Collection<ComponentAdapter<?>> getComponentAdapters() {
		return delegate.getComponentAdapters();
	}

    public <T> List<ComponentAdapter<T>> getComponentAdapters(Class<T> componentType) {
        return delegate.getComponentAdapters(TypeOf.fromClass(componentType));
    }

    public <T> List<ComponentAdapter<T>> getComponentAdapters(TypeOf<T> componentType) {
		return delegate.getComponentAdapters(componentType);
	}

    public <T> List<ComponentAdapter<T>> getComponentAdapters(Class<T> componentType, Class<? extends Annotation> binding) {
        return delegate.getComponentAdapters(TypeOf.fromClass(componentType), binding);
    }    

    public <T> List<ComponentAdapter<T>> getComponentAdapters(TypeOf<T> componentType, Class<? extends Annotation> binding) {
        return delegate.getComponentAdapters(componentType, binding);
    }

    public List<Object> getComponents() {
		return delegate.getComponents();
	}

	public <T> List<T> getComponents(Class<T> type) throws PicoException {
		return delegate.getComponents(type);
	}

	public PicoContainer getDelegate() {
		return delegate;
	}

	public PicoContainer getParent() {
		return delegate.getParent();
	}
    
    @Override
    public String toString() {
        return "D<" + delegate.toString();
    }

    public Converters getConverters() {
        if (delegate instanceof Converting) {
            return ((Converting) delegate).getConverters();
        } else {
            return null;
        }
    }
}
