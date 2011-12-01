/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.visitors;

import org.picocontainer.PicoContainer;
import org.picocontainer.PicoCompositionException;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A PicoVisitor implementation, that calls methods on the components of a specific type.
 * 
 * @author Aslak Helles&oslash;y
 * @author J&ouml;rg Schaible
 */
@SuppressWarnings("serial")
public class MethodCallingVisitor extends TraversalCheckingVisitor implements Serializable {

    // TODO: we must serialize method with read/writeObject ... and are our parent serializable ???
    private transient Method method;
    private final Object[] arguments;
    private final Class<?> type;
    private final boolean visitInInstantiationOrder;
    private final List componentInstances;

    /**
     * Construct a MethodCallingVisitor for a method with arguments.
     * 
     * @param method the {@link Method} to invoke
     * @param ofType the type of the components, that will be invoked
     * @param visitInInstantiationOrder <code>true</code> if components are visited in instantiation order
     * @param arguments the arguments for the method invocation (may be <code>null</code>)
     * @throws NullPointerException if <tt>method</tt>, or <tt>ofType</tt> is <code>null</code>
     */
    public MethodCallingVisitor(Method method, Class<?> ofType, Object[] arguments, boolean visitInInstantiationOrder) {
        if (method == null) {
            throw new NullPointerException();
        }
        this.method = method;
        this.arguments = arguments;
        this.type = ofType;
        this.visitInInstantiationOrder = visitInInstantiationOrder;
        this.componentInstances = new ArrayList();
    }

    /**
     * Construct a MethodCallingVisitor for standard methods visiting the component in instantiation order.
     * 
     * @param method the method to invoke
     * @param ofType the type of the components, that will be invoked
     * @param arguments the arguments for the method invocation (may be <code>null</code>)
     * @throws NullPointerException if <tt>method</tt>, or <tt>ofType</tt> is <code>null</code>
     */
    public MethodCallingVisitor(Method method, Class ofType, Object[] arguments) {
        this(method, ofType, arguments, true);
    }

    public Object traverse(Object node) {
        componentInstances.clear();
        try {
            super.traverse(node);
            if (!visitInInstantiationOrder) {
                Collections.reverse(componentInstances);
            }
            for (Object componentInstance : componentInstances) {
                invoke(componentInstance);
            }
        } finally {
            componentInstances.clear();
        }
        return Void.TYPE;
    }

    public boolean visitContainer(PicoContainer pico) {
        super.visitContainer(pico);
        componentInstances.addAll(pico.getComponents(type));
        return CONTINUE_TRAVERSAL;
    }

    protected Method getMethod() {
        return method;
    }

    protected Object[] getArguments() {
        return arguments;
    }

    protected void invoke(final Object[] targets) {
        for (Object target : targets) {
            invoke(target);
        }
    }

    protected Class<Void> invoke(final Object target) {
        final Method method = getMethod();
        try {
            method.invoke(target, getArguments());
        } catch (IllegalArgumentException e) {
            throw new PicoCompositionException("Can't call " + method.getName() + " on " + target, e);
        } catch (IllegalAccessException e) {
            throw new PicoCompositionException("Can't call " + method.getName() + " on " + target, e);
        } catch (InvocationTargetException e) {
            throw new PicoCompositionException("Failed when calling " + method.getName() + " on " + target, e
                    .getTargetException());
        }
        return Void.TYPE;
    }
}
