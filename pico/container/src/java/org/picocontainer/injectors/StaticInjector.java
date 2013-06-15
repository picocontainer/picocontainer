/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import java.lang.reflect.Type;

import org.picocontainer.Injector;
import org.picocontainer.PicoContainer;

/**
 * Performs injection into static members of a class and does not return an instance.
 * @author Michael Rimov
 *
 */
public interface StaticInjector<T> extends Injector<T> {

	void injectStatics(final PicoContainer container, final Type into, StaticsInitializedReferenceSet initializedReferenceSet);

}
