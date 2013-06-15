package org.picocontainer.injectors;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.NameBinding;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

public class NamedMethodInjection extends AbstractInjectionType {

    private final String prefix;
    private final boolean optional;

    public NamedMethodInjection(final String prefix) {
        this(prefix, true);
    }

    public NamedMethodInjection() {
        this("set");
    }

    public NamedMethodInjection(final boolean optional) {
        this("set", optional);
    }

    public NamedMethodInjection(final String prefix, final boolean optional) {
        this.prefix = prefix;
        this.optional = optional;
    }

    public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle, final Properties componentProps, final Object key, final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {
        return wrapLifeCycle(monitor.newInjector(new NamedMethodInjector(key, impl, monitor, prefix, optional, methodParams)), lifecycle);
    }

    @SuppressWarnings("serial")
    public static class NamedMethodInjector<T> extends SetterInjection.SetterInjector<T> {

        private final boolean optional;

        public NamedMethodInjector(final Object key, final Class<T> impl, final ComponentMonitor monitor, final boolean optional, final MethodParameters... parameters) {
            this(key, impl, monitor, "set", optional, parameters);
        }

        public NamedMethodInjector(final Object key, final Class<T> impl, final ComponentMonitor monitor, final MethodParameters... parameters) {
            this(key, impl, monitor, "set", true, parameters);
        }

        public NamedMethodInjector(final Object key, final Class<T> impl, final ComponentMonitor monitor, final String prefix, final MethodParameters... parameters) {
            this(key, impl, monitor, prefix, true, parameters);
        }

        public NamedMethodInjector(final Object key, final Class<T> impl, final ComponentMonitor monitor, final String prefix, final boolean optional, final MethodParameters... parameters) {
            super(key, impl, monitor, prefix, true, "", false, parameters);
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
        protected void unsatisfiedDependencies(final PicoContainer container, final Set<Type> unsatisfiableDependencyTypes, final List<AccessibleObject> unsatisfiableDependencyMembers) {
            if (!optional) {
                throw new UnsatisfiableDependenciesException(this.getComponentImplementation().getName() + " has unsatisfied dependencies " + unsatisfiableDependencyTypes
                        + " for members " + unsatisfiableDependencyMembers + " from " + container);
            }
        }

        @Override
        public String getDescriptor() {
            return "NamedMethodInjection";
        }

    }
}
