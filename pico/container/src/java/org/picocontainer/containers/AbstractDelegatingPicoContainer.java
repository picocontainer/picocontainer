package org.picocontainer.containers;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Converters;
import org.picocontainer.Converting;
import org.picocontainer.NameBinding;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.PicoVisitor;

import com.googlecode.jtype.Generic;

/**
 * Abstract base class for <i>immutable<i> delegation to a PicoContainer
 *
 * @author Konstantin Pribluda
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractDelegatingPicoContainer implements PicoContainer, Converting, Serializable {

    private PicoContainer delegate;

    public AbstractDelegatingPicoContainer(final PicoContainer delegate) {
		if (delegate == null) {
			throw new NullPointerException(
					"PicoContainer delegate must not be null");
		}
		this.delegate = delegate;
	}

	public final void accept(final PicoVisitor visitor) {
        visitor.visitContainer(this);
        delegate.accept(visitor);
	}


	@Override
    public boolean equals(final Object obj) {
		// required to make it pass on both jdk 1.3 and jdk 1.4. Btw, what about
		// overriding hashCode()? (AH)
		return delegate.equals(obj) || this == obj;
	}

	public <T> T getComponentInto(final Class<T> componentType, final Type into) {
		return componentType.cast(getComponentInto((Object) componentType, into));
	}

    public <T> T getComponentInto(final Generic<T> componentType, final Type into) {
        return (T) getComponentInto((Object) componentType, into);
    }

    public <T> T getComponent(final Class<T> componentType, final Class<? extends Annotation> binding, final Type into) {
        return delegate.getComponent(componentType, binding, into);
    }

    public <T> T getComponent(final Class<T> componentType, final Class<? extends Annotation> binding) {
        return delegate.getComponent(componentType, binding);
    }

    public Object getComponent(final Object keyOrType) {
        return getComponentInto(keyOrType, ComponentAdapter.NOTHING.class);
    }

    public Object getComponentInto(final Object keyOrType, final Type into) {
        return delegate.getComponentInto(keyOrType, into);
    }

    public <T> T getComponent(final Class<T> componentType) {
        return getComponentInto(Generic.get(componentType), ComponentAdapter.NOTHING.class);
    }

    public <T> T getComponent(final Generic<T> componentType) {
        return delegate.getComponent(componentType);
    }

    public <T> ComponentAdapter<T> getComponentAdapter(final Class<T> componentType, final NameBinding componentNameBinding) {
        return delegate.getComponentAdapter(Generic.get(componentType), componentNameBinding);
    }

    public <T> ComponentAdapter<T> getComponentAdapter(final Generic<T> componentType,
			final NameBinding componentNameBinding) {
		return delegate.getComponentAdapter(componentType, componentNameBinding);
	}

    public <T> ComponentAdapter<T> getComponentAdapter(final Class<T> componentType, final Class<? extends Annotation> binding) {
        return delegate.getComponentAdapter(Generic.get(componentType), binding);
    }

    public <T> ComponentAdapter<T> getComponentAdapter(final Generic<T> componentType, final Class<? extends Annotation> binding) {
        return delegate.getComponentAdapter(componentType, binding);
    }

    public ComponentAdapter<?> getComponentAdapter(final Object key) {
		return delegate.getComponentAdapter(key);
	}

	public Collection<ComponentAdapter<?>> getComponentAdapters() {
		return delegate.getComponentAdapters();
	}

    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Class<T> componentType) {
        return delegate.getComponentAdapters(Generic.get(componentType));
    }

    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Generic<T> componentType) {
		return delegate.getComponentAdapters(componentType);
	}

    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Class<T> componentType, final Class<? extends Annotation> binding) {
        return delegate.getComponentAdapters(Generic.get(componentType), binding);
    }

    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Generic<T> componentType, final Class<? extends Annotation> binding) {
        return delegate.getComponentAdapters(componentType, binding);
    }

    public List<Object> getComponents() {
		return delegate.getComponents();
	}

	public <T> List<T> getComponents(final Class<T> type) throws PicoException {
		return delegate.getComponents(type);
	}

	public PicoContainer getDelegate() {
		return delegate;
	}

	/**
	 * Allows for swapping of delegate object to allow for temp proxies.
	 * @param newDelegate
	 * @return the old delegate instance.
	 */
	protected PicoContainer swapDelegate(final PicoContainer newDelegate) {
		if (newDelegate == null) {
			throw new NullPointerException("newDelegate");
		}
		PicoContainer oldDelegate = delegate;
		this.delegate = newDelegate;
		return oldDelegate;
	}

	public PicoContainer getParent() {
		return delegate.getParent();
	}

    @Override
    public String toString() {
        return "[Delegate]:" + delegate.toString();
    }

    public Converters getConverters() {
        if (delegate instanceof Converting) {
            return ((Converting) delegate).getConverters();
        } else {
            return null;
        }
    }
}
