package org.picocontainer.injectors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.injectors.AbstractInjector.ThreadLocalCyclicDependencyGuard;
import org.picocontainer.injectors.MethodInjection.MethodInjector;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.parameters.MethodParameters;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Injection will happen through a specific single reflection method for the component.
 *
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class SpecificMethodInjector<T> extends MethodInjection.MethodInjector<T> implements StaticInjector<T> {
    private final List<Method> injectionMethods;
	private boolean isStaticInjection;
    private transient ThreadLocalCyclicDependencyGuard<Object> instantiationGuard;

    
    /**
     * Simple testable constructor
     * @param key
     * @param impl
     */
    public SpecificMethodInjector(Object key, Class<T> impl, Method... injectionMethods) {
    	this(key, impl, new NullComponentMonitor(), true, true, null, injectionMethods);
    }

    /**
     * Typical constructor used in deployments
     * @param key
     * @param impl
     * @param monitor
     * @param useNames
     * @param useAllParameters
     * @param parameters
     * @param injectionMethods
     * @throws NotConcreteRegistrationException
     */
    public SpecificMethodInjector(Object key, Class<T> impl, ComponentMonitor monitor, boolean useNames, boolean useAllParameters, MethodParameters[] parameters, Method... injectionMethods) throws NotConcreteRegistrationException {
        super(key, impl, monitor, null, useNames, useAllParameters, parameters);

        
        this.injectionMethods = Arrays.asList(injectionMethods);
		this.isStaticInjection = isStaticInjection(injectionMethods);

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



	public void injectStatics(final PicoContainer container, final Type into) {
		if (!isStaticInjection) {
			throw new PicoCompositionException(Arrays.deepToString(injectionMethods.toArray()) + " are non static fields, injectStatics should not be called.");
		}

    	boolean i_Instantiated = false;
    	try {
            if (instantiationGuard == null) {
            	i_Instantiated = true;
                instantiationGuard = new ThreadLocalCyclicDependencyGuard<Object>() {
                    @Override
                    @SuppressWarnings("synthetic-access")
                    public Object run(Object instance) {
                        List<Method> methods = getInjectorMethods();
                        Object[] methodParameters = null;
                        for (Method method : methods) {
                            methodParameters = getMemberArguments(guardedContainer, method, into);
                            invokeMethod(method, methodParameters, null, container);
                        }
                        return null;
                    }
                };
            }
            instantiationGuard.setGuardedContainer(container);
            instantiationGuard.observe(getComponentImplementation(), null);
    	} finally {
            if (i_Instantiated) {
            	instantiationGuard.remove();
            	instantiationGuard = null;
            }
    	}
		
	}

	@Override
	public T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
		if (isStaticInjection) {
			throw new PicoCompositionException(Arrays.deepToString(injectionMethods.toArray()) + " are static methods, getComponentInstance() on this adapter should not be called.");
		}
		
		return super.getComponentInstance(container, into);
	}	
	
}
