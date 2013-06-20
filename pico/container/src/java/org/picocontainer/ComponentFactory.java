/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer;

import java.util.Properties;

import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

/**
 * <p/>
 * A component factory is responsible for creating
 * {@link ComponentAdapter} component adapters. The main use of the component factory is
 * inside {@link DefaultPicoContainer#DefaultPicoContainer(ComponentFactory)}, where it can
 * be used to customize the default component adapter that is used when none is specified
 * explicitly.
 * </p>
 *
 * @author Paul Hammant
 * @author Mauro Talevi
 * @author Jon Tirs&eacute;n
 */
public interface ComponentFactory {

    /**
     * Create a new component adapter based on the specified arguments.
     *
     * @param monitor the component monitor
     * @param lifecycle te lifecycle strategy
     * @param componentProps the component properties
     * @param key the key to be associated with this adapter. This
     *            value should be returned from a call to
     *            {@link ComponentAdapter#getComponentKey()} on the created
     *            adapter.
     * @param impl the implementation class to be associated
     *            with this adapter. This value should be returned from a call
     *            to {@link ComponentAdapter#getComponentImplementation()} on
     *            the created adapter. Should not be null.
     * @param constructorParams TODO
     * @param fieldParams TODO
     * @param methodParams TODO
     * @return a new component adapter based on the specified arguments. Should
     *         not return null.
     * @throws PicoCompositionException if the creation of the component adapter
     *             results in a {@link PicoCompositionException}.
     * @return The component adapter
     */
    <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor,
                                            LifecycleStrategy lifecycle,
                                            Properties componentProps,
                                            Object key,
                                            Class<T> impl,
                                            ConstructorParameters constructorParams, FieldParameters[] fieldParams, MethodParameters[] methodParams) throws PicoCompositionException;

    /**
     * Verification for the ComponentFactory - subject to implementation.
     *
     * @param container the {@link PicoContainer}, that is used for verification.
     * @throws PicoCompositionException if one or more dependencies cannot be resolved.
     */
    void verify(PicoContainer container);

    /**
     * Accepts a visitor for this ComponentFactory. The method is normally called by visiting a {@link PicoContainer}, that
     * cascades the visitor also down to all its ComponentFactory instances.
     *
     * @param visitor the visitor.
     */
    void accept(PicoVisitor visitor);
    
    void dispose();
}
