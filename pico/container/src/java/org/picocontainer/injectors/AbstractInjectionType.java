package org.picocontainer.injectors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.ComponentMonitorStrategy;
import org.picocontainer.InjectionType;
import org.picocontainer.Injector;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;
import org.picocontainer.lifecycle.NullLifecycleStrategy;

import java.io.Serializable;
import java.lang.reflect.Type;

@SuppressWarnings("serial")
public abstract class AbstractInjectionType implements InjectionType, Serializable {

    public void verify(PicoContainer container) {
    }

    public final void accept(PicoVisitor visitor) {
        visitor.visitComponentFactory(this);
    }

    protected <T> ComponentAdapter<T> wrapLifeCycle(final Injector<T> injector, LifecycleStrategy lifecycle) {
        if (lifecycle instanceof NullLifecycleStrategy) {
            return injector;
        } else {
            return new LifecycleAdapter<T>(injector, lifecycle);
        }
    }

	private static class LifecycleAdapter<T> implements Injector<T>, LifecycleStrategy, ComponentMonitorStrategy, Serializable {
        private final Injector<T> delegate;
        private final LifecycleStrategy lifecycle;

        public LifecycleAdapter(Injector<T> delegate, LifecycleStrategy lifecycle) {
            this.delegate = delegate;
            this.lifecycle = lifecycle;
        }

        public Object getComponentKey() {
            return delegate.getComponentKey();
        }

        public Class<? extends T> getComponentImplementation() {
            return delegate.getComponentImplementation();
        }

        public T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            return delegate.getComponentInstance(container, into);
        }

        public void verify(PicoContainer container) throws PicoCompositionException {
            delegate.verify(container);
        }

        public void accept(PicoVisitor visitor) {
            delegate.accept(visitor);
        }

        public ComponentAdapter<T> getDelegate() {
            return delegate;
        }

        @SuppressWarnings("rawtypes")
		public <U extends ComponentAdapter> U findAdapterOfType(Class<U> adapterType) {
            return delegate.findAdapterOfType(adapterType);
        }

        public String getDescriptor() {
            return "LifecycleAdapter";
        }

        public String toString() {
            return getDescriptor() + ":" + delegate.toString();
        }

        public void start(Object component) {
            lifecycle.start(component);
        }

        public void stop(Object component) {
            lifecycle.stop(component);
        }

        public void dispose(Object component) {
            lifecycle.dispose(component);
        }

        public boolean hasLifecycle(Class<?> type) {
            return lifecycle.hasLifecycle(type);
        }

        public boolean isLazy(ComponentAdapter<?> adapter) {
            return lifecycle.isLazy(adapter);
        }

        public void changeMonitor(ComponentMonitor monitor) {
            if (delegate instanceof ComponentMonitorStrategy) {
                ((ComponentMonitorStrategy) delegate).changeMonitor(monitor);
            }
        }

        public ComponentMonitor currentMonitor() {
            if (delegate instanceof ComponentMonitorStrategy) {
                return ((ComponentMonitorStrategy) delegate).currentMonitor();
            }
            return null;
        }

        public Object decorateComponentInstance(PicoContainer container, Type into, T instance) {
            return delegate.decorateComponentInstance(container, into, instance);
        }
    }
}
