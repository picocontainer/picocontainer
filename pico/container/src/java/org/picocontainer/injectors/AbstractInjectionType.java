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

public abstract class AbstractInjectionType implements InjectionType, Serializable {

    public void verify(PicoContainer container) {
    }

    public final void accept(PicoVisitor visitor) {
        visitor.visitComponentFactory(this);
    }

    protected ComponentAdapter wrapLifeCycle(final Injector injector, LifecycleStrategy lifecycle) {
        if (lifecycle instanceof NullLifecycleStrategy) {
            return injector;
        } else {
            return new LifecycleAdapter(injector, lifecycle);
        }
    }

    private static class LifecycleAdapter implements ComponentAdapter, LifecycleStrategy, ComponentMonitorStrategy, Serializable {
        private final Injector injector;
        private final LifecycleStrategy lifecycle;

        public LifecycleAdapter(Injector injector, LifecycleStrategy lifecycle) {
            this.injector = injector;
            this.lifecycle = lifecycle;
        }

        public Object getComponentKey() {
            return injector.getComponentKey();
        }

        public Class getComponentImplementation() {
            return injector.getComponentImplementation();
        }

        public Object getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            return injector.getComponentInstance(container, into);
        }

        public void verify(PicoContainer container) throws PicoCompositionException {
            injector.verify(container);
        }

        public void accept(PicoVisitor visitor) {
            injector.accept(visitor);
        }

        public ComponentAdapter getDelegate() {
            return injector;
        }

        public ComponentAdapter findAdapterOfType(Class adapterType) {
            return injector.findAdapterOfType(adapterType);
        }

        public String getDescriptor() {
            return "LifecycleAdapter";
        }

        public String toString() {
            return getDescriptor() + ":" + injector.toString();
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
            if (injector instanceof ComponentMonitorStrategy) {
                ((ComponentMonitorStrategy) injector).changeMonitor(monitor);
            }
        }

        public ComponentMonitor currentMonitor() {
            if (injector instanceof ComponentMonitorStrategy) {
                return ((ComponentMonitorStrategy) injector).currentMonitor();
            }
            return null;
        }
    }
}
