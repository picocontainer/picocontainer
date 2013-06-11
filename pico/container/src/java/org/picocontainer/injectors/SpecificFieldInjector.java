/**
 * 
 */
package org.picocontainer.injectors;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.picocontainer.ComponentMonitor;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.parameters.FieldParameters;

/**
 * Takes specific Fields obtained through reflection and injects them.  Private fields are ok (as per JSR-330), as
 * this class attempts setAccessible(true) before assigning the field.
 * <p>Statics Note:  Either make everything static and use inject statics or make everything non static and use
 * getComponentInstance().</p>  
 * @author Michael Rimov
 */
@SuppressWarnings("serial")
public class SpecificFieldInjector<T> extends AbstractFieldInjector<T> implements StaticInjector<T>{
	
    private Field[] fieldsToInject;
    
    private boolean isStaticInjection = false;
    
    private transient ThreadLocalCyclicDependencyGuard<T> instantiationGuard;

    /**
     * Simple testing constructor.
     * @param key
     * @param impl
     * @param fieldsToInject
     */
    public SpecificFieldInjector(Object key, Class<T> impl, Field... fieldsToInject) {
    	this(key, impl, null, new NullComponentMonitor(), false, true, fieldsToInject);
    }
    
    /**
     * Usual constructor invoked during runtime.
     * @param key
     * @param impl
     * @param parameters
     * @param monitor
     * @param useNames
     * @param requireConsumptionOfAllParameters
     * @param fieldsToInject
     */
	public SpecificFieldInjector(Object key, Class<T> impl, FieldParameters[] parameters, ComponentMonitor monitor,
            boolean useNames, boolean requireConsumptionOfAllParameters, Field... fieldsToInject) {
		
		super(key, impl, monitor, useNames, requireConsumptionOfAllParameters, parameters);
		this.fieldsToInject = fieldsToInject;


		this.isStaticInjection = isStaticInjection(fieldsToInject);
    	
	}

	
	
	public void injectStatics(final PicoContainer container, final Type into) {
		if (!isStaticInjection) {
			throw new PicoCompositionException(Arrays.deepToString(fieldsToInject) + " are non static fields, injectStatics should not be called.");
		}
		
        final Constructor<?> constructor = getConstructor();
        boolean iInstantiated = false;
        T result;
        try {
	        if (instantiationGuard == null) {
	        	iInstantiated = true;
	            instantiationGuard = new ThreadLocalCyclicDependencyGuard<T>() {
	                public T run(Object instance) {
	                    final ParameterToAccessibleObjectPair[] matchingParameters = getMatchingParameterListForMembers(guardedContainer);
	                    return  decorateComponentInstance(matchingParameters, currentMonitor(), null, container, guardedContainer, into, null);
	                }
	            };
	        }
	        instantiationGuard.setGuardedContainer(container);
	        result = instantiationGuard.observe(getComponentImplementation(), null);
        } finally {
	        if (iInstantiated) {
	        	instantiationGuard.remove();
	        	instantiationGuard = null;
	        }
        }
	}

	
	@Override
	protected void initializeInjectionMembersAndTypeLists() {
		injectionMembers = new ArrayList<AccessibleObject>();
        List<Annotation> bindingIds = new ArrayList<Annotation>();
        final List<Type> typeList = new ArrayList<Type>();
        for (Field eachFieldToInject : fieldsToInject) {
        	injectionMembers.add(eachFieldToInject);
        }

        //Sort for injection.
        Collections.sort(injectionMembers, new JSRAccessibleObjectOrderComparator());
        
        for (AccessibleObject eachMember : injectionMembers) {
        	Field field = (Field)eachMember;
            typeList.add(box(field.getGenericType()));
            bindingIds.add(AnnotatedFieldInjection.AnnotatedFieldInjector.getBinding(field));

        }
        
        injectionTypes = typeList.toArray(new Type[0]);
        bindings = bindingIds.toArray(new Annotation[0]);

	}
	
	
	@Override
	protected Object memberInvocationReturn(Object lastReturn, AccessibleObject member, Object instance) {
		return instance;
	}

	@Override
	protected Object injectIntoMember(AccessibleObject member, Object componentInstance, Object toInject)
			throws IllegalAccessException, InvocationTargetException {
        final Field field = (Field) member;
        
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
			public Void run() {
	            field.setAccessible(true);
	            return null;
			}
        });
        
        field.set(componentInstance, toInject);
        return null;
     }

	@Override
	public T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
		if (isStaticInjection) {
			throw new PicoCompositionException(Arrays.deepToString(fieldsToInject) + " are static fields, getComponentInstance() on this adapter should not be called.");
		}
		
		return super.getComponentInstance(container, into);
	}

}
