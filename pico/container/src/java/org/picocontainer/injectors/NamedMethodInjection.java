package org.picocontainer.injectors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.NameBinding;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Properties;
import java.util.Set;

public class NamedMethodInjection extends AbstractInjectionType {

    private final String prefix;
    private final boolean optional;

    public NamedMethodInjection(String prefix) {
        this(prefix, true);
    }

    public NamedMethodInjection() {
        this("set");
    }

    public NamedMethodInjection(boolean optional) {
        this("set", optional);
    }

    public NamedMethodInjection(String prefix, boolean optional) {
        this.prefix = prefix;
        this.optional = optional;
    }

    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor, LifecycleStrategy lifecycle, Properties componentProps, Object key, Class<T> impl, Parameter... parameters) throws PicoCompositionException {
        return wrapLifeCycle(monitor.newInjector(new NamedMethodInjector(key, impl, parameters, monitor, prefix, optional)), lifecycle);
    }

    @SuppressWarnings("serial")
    public static class NamedMethodInjector<T> extends SetterInjection.SetterInjector<T> {

        private final boolean optional;

        public NamedMethodInjector(Object key, Class<T> impl, Parameter[] parameters, ComponentMonitor monitor, boolean optional) {
            this(key, impl, parameters, monitor, "set", optional);
        }

        public NamedMethodInjector(Object key, Class<T> impl, Parameter[] parameters, ComponentMonitor monitor) {
            this(key, impl, parameters, monitor, "set", true);
        }

        public NamedMethodInjector(Object key, Class<T> impl, Parameter[] parameters, ComponentMonitor monitor, String prefix) {
            this(key, impl, parameters, monitor, prefix, true);
        }

        public NamedMethodInjector(Object key, Class<T> impl, Parameter[] parameters, ComponentMonitor monitor, String prefix, boolean optional) {
            super(key, impl, parameters, monitor, prefix, true);
            this.optional = optional;
        }

        @Override
        protected NameBinding makeParameterNameImpl(final AccessibleObject member) {
            return new NameBinding() {
                public String getName() {
                    String name = ((Method)member).getName().substring(prefix.length()); // string off 'set' or chosen prefix
                    return name.substring(0,1).toLowerCase() + name.substring(1);  // change "SomeThing" to "someThing"
                }
            };
        }

        @Override
        protected void unsatisfiedDependencies(PicoContainer container, Set<Type> unsatisfiableDependencyTypes) {
            if (!optional) {
                super.unsatisfiedDependencies(container, unsatisfiableDependencyTypes);
            }
        }

        @Override
        public String getDescriptor() {
            return "NamedMethodInjection";
        }

    }
}
