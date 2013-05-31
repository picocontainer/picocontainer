package org.picocontainer.injectors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.NameBinding;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.injectors.AbstractInjector.UnsatisfiableDependenciesException;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
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

    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor, LifecycleStrategy lifecycle, Properties componentProps, Object key, Class<T> impl, ConstructorParameters constructorParams, FieldParameters[] fieldParams, MethodParameters[] methodParams) throws PicoCompositionException {
        return wrapLifeCycle(monitor.newInjector(new NamedMethodInjector(key, impl, monitor, prefix, optional, methodParams)), lifecycle);
    }

    @SuppressWarnings("serial")
    public static class NamedMethodInjector<T> extends SetterInjection.SetterInjector<T> {

        private final boolean optional;

        public NamedMethodInjector(Object key, Class<T> impl, ComponentMonitor monitor, boolean optional, MethodParameters... parameters) {
            this(key, impl, monitor, "set", optional, parameters);
        }

        public NamedMethodInjector(Object key, Class<T> impl, ComponentMonitor monitor, MethodParameters... parameters) {
            this(key, impl, monitor, "set", true, parameters);
        }

        public NamedMethodInjector(Object key, Class<T> impl, ComponentMonitor monitor, String prefix, MethodParameters... parameters) {
            this(key, impl, monitor, prefix, true, parameters);
        }

        public NamedMethodInjector(Object key, Class<T> impl, ComponentMonitor monitor, String prefix, boolean optional, MethodParameters... parameters) {
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
        protected void unsatisfiedDependencies(PicoContainer container, Set<Type> unsatisfiableDependencyTypes, List<AccessibleObject> unsatisfiableDependencyMembers) {
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
