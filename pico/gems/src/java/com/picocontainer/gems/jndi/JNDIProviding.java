/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package com.picocontainer.gems.jndi;

import java.util.Properties;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentFactory;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.PicoVisitor;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;

/**
 * TODO: decide where to get JNDI name as we do not have
 * implementation here. ? Property
 * @author Konstantin Pribluda
 *
 */
public class JNDIProviding implements ComponentFactory {

	public <T> ComponentAdapter<T> createComponentAdapter(
			final ComponentMonitor monitor,
			final LifecycleStrategy lifecycle,
			final Properties componentProps, final Object key,
			final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams)
			throws PicoCompositionException {
		return null;
	}

    public void verify(final PicoContainer container) {
    }

    public void accept(final PicoVisitor visitor) {
        visitor.visitComponentFactory(this);
    }

	public void dispose() {
		
	}
}
