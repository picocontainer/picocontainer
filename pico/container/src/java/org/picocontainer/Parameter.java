/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Jon Tirsen                        *
 *****************************************************************************/

package org.picocontainer;

import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.parameters.DefaultConstructorParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * This class provides control over the arguments that will be passed to a constructor. It can be used for finer control over
 * what arguments are passed to a particular constructor.
 *
 * @author Jon Tirs&eacute;n
 * @author Aslak Helles&oslash;y
 * @author Thomas Heller
 * @see MutablePicoContainer#addComponent(Object,Object,Parameter[]) a method on the
 *      {@link MutablePicoContainer} interface which allows passing in of an array of {@linkplain Parameter Parameters}.
 * @see org.picocontainer.parameters.ComponentParameter an implementation of this interface that allows you to specify the key
 *      used for resolving the parameter.
 * @see org.picocontainer.parameters.ConstantParameter an implementation of this interface that allows you to specify a constant
 *      that will be used for resolving the parameter.
 */
public interface Parameter {

	/**
	 * Zero parameter is used when you wish a component to be instantiated with its default constructor.  Ex:
	 * <div class="source">
	 * 	<pre>
	 * 		MutablePicoContainer mpc = new PicoBuilder().build();
	 * 		mpc.addComponent(Map.class, HashMap.class, Parameter.ZERO);
	 * 		mpc.addComponent(List.class, ArrayList.class, Parameter.ZERO);
	 * 	</pre>
	 * </div>
	 * <p>By specifying the default constructor in this example code, you allow PicoContainer to recognize
	 * that HashMap(Collection) should <em>not</em> be used and avoid a CircularDependencyException.</p>
	 */
    Parameter[] ZERO =  new Parameter[] {DefaultConstructorParameter.INSTANCE};
    
    Parameter[] DEFAULT = new Parameter[]{ ComponentParameter.DEFAULT };
    
    
    /**
     * Check if the Parameter can satisfy the expected type using the container.
     *
     * @param container             the container from which dependencies are resolved.
     * @param forAdapter            the {@link org.picocontainer.ComponentAdapter} that is asking for the instance
     * @param injecteeAdapter       the adapter to be injected into (null for N/A)
     * @param expectedType          the required type
     * @param expectedNameBinding Expected parameter name
     * @param useNames              should use parameter names for disambiguation
     * @param binding @return <code>true</code> if the component parameter can be resolved.
     * @since 2.8.1
     *
     */
    Resolver resolve(PicoContainer container, ComponentAdapter<?> forAdapter,
                     ComponentAdapter<?> injecteeAdapter, Type expectedType, NameBinding expectedNameBinding,
                     boolean useNames, Annotation binding);

    /**
     * Verify that the Parameter can satisfy the expected type using the container
     *
     * @param container             the container from which dependencies are resolved.
     * @param adapter               the {@link ComponentAdapter} that is asking for the verification
     * @param expectedType          the required type
     * @param expectedNameBinding Expected parameter name
     *
     * @param useNames
     * @param binding
     * @throws PicoCompositionException if parameter and its dependencies cannot be resolved
     */
    void verify(PicoContainer container, ComponentAdapter<?> adapter,
                Type expectedType, NameBinding expectedNameBinding,
                boolean useNames, Annotation binding);

    /**
     * Accepts a visitor for this Parameter. The method is normally called by visiting a {@link ComponentAdapter}, that
     * cascades the {@linkplain PicoVisitor visitor} also down to all its {@linkplain Parameter Parameters}.
     *
     * @param visitor the visitor.
     *
     */
    void accept(PicoVisitor visitor);

    /**
     * Resolver is used transitarily during resolving of Parameters.
     * isResolvable() and resolveInstance() in series do not cause resolveAdapter() twice
     */
    public static interface Resolver {

        /**
         * @return can the parameter be resolved
         */
        public boolean isResolved();

        /**
         * @return the instance to be used to inject as a parameter
         * @param into
         */
        public Object resolveInstance(Type into);

        /**
         * @return the ComponentAdapter for the parameter in question
         */
        public ComponentAdapter<?> getComponentAdapter();

    }

    /**
     * The Parameter cannot (ever) be resolved
     */
    public static class NotResolved implements Resolver {
        public boolean isResolved() {
            return false;
        }

        public Object resolveInstance(Type into) {
            return null;
        }

        public ComponentAdapter<?> getComponentAdapter() {
            return null;
        }
    }

    /**
     * Delegate to another reolver
     */
    public abstract static class DelegateResolver implements Resolver {
        private final Resolver delegate;

        public DelegateResolver(Resolver delegate) {
            this.delegate = delegate;
        }

        public boolean isResolved() {
            return delegate.isResolved();
        }

        public Object resolveInstance(Type into) {
            return delegate.resolveInstance(into);
        }

        public ComponentAdapter<?> getComponentAdapter() {
            return delegate.getComponentAdapter();
        }
    }

    /**
     * A fixed value wrapped as a Resolver
     */
    public static class ValueResolver implements Resolver {

        private final boolean resolvable;
        private final Object value;
        private final ComponentAdapter<?> adapter;

        public ValueResolver(boolean resolvable, Object value, ComponentAdapter<?> adapter) {
            this.resolvable = resolvable;
            this.value = value;
            this.adapter = adapter;
        }

        public boolean isResolved() {
            return resolvable;
        }

        public Object resolveInstance(Type into) {
            return value;
        }

        public ComponentAdapter<?> getComponentAdapter() {
            return adapter;
        }
    }

}
