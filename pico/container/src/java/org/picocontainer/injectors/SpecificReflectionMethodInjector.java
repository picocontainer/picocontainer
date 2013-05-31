package org.picocontainer.injectors;

import org.picocontainer.ComponentMonitor;
import org.picocontainer.Parameter;
import org.picocontainer.parameters.MethodParameters;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Injection will happen through a specific single reflection method for the component.
 *
 * @author Paul Hammant
 */
public class SpecificReflectionMethodInjector extends MethodInjection.MethodInjector {
    private final List<Method> injectionMethods;

    public SpecificReflectionMethodInjector(Object key, Class impl, ComponentMonitor monitor, Method injectionMethod, boolean useNames, boolean useAllParameters, MethodParameters... parameters) throws NotConcreteRegistrationException {
        super(key, impl, monitor, null, useNames, useAllParameters, parameters);
        ArrayList<Method> methods = new ArrayList<Method>();
        methods.add(injectionMethod);
        this.injectionMethods = methods;
    }

    @Override
    protected List<Method> getInjectorMethods() {
        return injectionMethods;
    }

    @Override
    public String getDescriptor() {
        StringBuilder mthds = new StringBuilder();
        for (Method method : injectionMethods) {
            mthds.append(",").append(method.getName());
        }
        return "SpecificReflectionMethodInjector[" + mthds.substring(1) + "]-";
    }

}
