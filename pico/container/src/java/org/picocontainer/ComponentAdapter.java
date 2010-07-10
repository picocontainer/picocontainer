/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer;

import java.lang.reflect.Type;

/**
 * A component adapter is responsible for providing a specific component
 * instance of type &lt;T&gt;. An instance of an implementation of this interface is
 * used inside a {@link PicoContainer} for every registered component or
 * instance. Each <code>ComponentAdapter</code> instance has to have a key
 * which is unique within that container. The key itself is either a class type
 * (normally an interface) or an identifier.
 * <p>In a overly simplistic sense, the ComponentAdapter can be thought of us a type of
 * an object factory.  If you need to modify how your object is constructed, use and appropriate
 * ComponentAdapter or roll your own since the API is purposely kept rather simple.  See
 * <a href="http://www.picocontainer.org/adapters.html">http://www.picocontainer.org/adapters.html</a>
 * for more information.</p>
 *
 * @author Jon Tirs&eacute;n
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 */
public interface ComponentAdapter<T> {

    public class NOTHING {
        private NOTHING() {
        }
    };

    /**
     * Retrieve the key associated with the component.
     *
     * @return the component's key. Should either be a class type (normally an interface) or an identifier that is
     *         unique (within the scope of the current PicoContainer).
     */
    Object getComponentKey();

    /**
     * Retrieve the class of the component.
     *
     * @return the component's implementation class. Should normally be a concrete class (ie, a class that can be
     *         instantiated).
     */
    Class<? extends T> getComponentImplementation();

    /**
     * Retrieve the component instance. This method will usually create a new instance each time it is called, but that
     * is not required. For example, {@link org.picocontainer.behaviors.Caching.Cached} will always return the
     * same instance.
     *
     * @param container the {@link org.picocontainer.PicoContainer}, that is used to resolve any possible dependencies of the instance.
     * @param into the class that is about to be injected into. Use ComponentAdapter.NOTHING.class if this is not important to you.
     * @return the component instance.
     * @throws PicoCompositionException  if the component has dependencies which could not be resolved, or
     *                                     instantiation of the component lead to an ambiguous situation within the
     *                                     container.
     */
    T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException;

    /**
     * Verify that all dependencies for this adapter can be satisfied. Normally, the adapter should verify this by
     * checking that the associated PicoContainer contains all the needed dependencies.
     *
     * @param container the {@link PicoContainer}, that is used to resolve any possible dependencies of the instance.
     * @throws PicoCompositionException if one or more dependencies cannot be resolved.
     */
    void verify(PicoContainer container) throws PicoCompositionException;

    /**
     * Accepts a visitor for this ComponentAdapter. The method is normally called by visiting a {@link PicoContainer}, that
     * cascades the visitor also down to all its ComponentAdapter instances.
     *
     * @param visitor the visitor.
     */
    void accept(PicoVisitor visitor);

    /**
     * Component adapters may be nested in a chain, and this method is used to grab the next ComponentAdapter in the chain.
     * @return the next component adapter in line or null if there is no delegate ComponentAdapter.
     */
    ComponentAdapter<T> getDelegate();

    /**
     * Locates a component adapter of type <em>componentAdapterType</em> in the ComponentAdapter chain.  Will return null
     * if there is no adapter of the given type.
     * @param <U> the type of ComponentAdapter being located.
     * @param adapterType the class of the adapter type being located.  Never null.
     * @return the appropriate component adapter of type <em>U</em>.  May return null if the component adapter type is not
     * returned.
     */
    <U extends ComponentAdapter> U findAdapterOfType(Class<U> adapterType);

    /**
     * Get a string key descriptor of the component adapter for use in toString()
     * @return the descriptor
     */
    String getDescriptor();



}
