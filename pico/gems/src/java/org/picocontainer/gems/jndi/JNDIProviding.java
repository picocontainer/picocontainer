/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.gems.jndi;

import java.util.Properties;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentFactory;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;

/**
 * TODO: decide where to get JNDI name as we do not have 
 * implementation here. ? Property
 * @author Konstantin Pribluda
 *
 */
public class JNDIProviding implements ComponentFactory {

	public <T> ComponentAdapter<T> createComponentAdapter(
			final ComponentMonitor componentMonitor,
			final LifecycleStrategy lifecycleStrategy,
			final Properties componentProperties, final Object key,
			final Class<T> impl, final Parameter... parameters)
			throws PicoCompositionException {
		return null;
	}

    public void verify(final PicoContainer container) {
    }

    public void accept(final PicoVisitor visitor) {
        visitor.visitComponentFactory(this);
    }
}
