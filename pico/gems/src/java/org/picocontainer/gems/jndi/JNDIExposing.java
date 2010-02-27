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

import javax.naming.NamingException;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.behaviors.AbstractBehaviorFactory;

/**
 * produce JNDI exposing behaviour
 * 
 * @author Konstantin Pribluda
 * 
 * @param <T>
 */
@SuppressWarnings("serial")
public class JNDIExposing extends AbstractBehaviorFactory {

	@Override
	public <T> ComponentAdapter<T> addComponentAdapter(
			final ComponentMonitor componentMonitor,
			final LifecycleStrategy lifecycleStrategy,
			final Properties componentProperties, final ComponentAdapter<T> adapter) {
		try {
			return new JNDIExposed<T>(super.addComponentAdapter(
					componentMonitor, lifecycleStrategy, componentProperties,
					adapter));
		} catch (NamingException e) {
			throw new PicoCompositionException(
					"unable to create JNDI behaviour", e);
		}
	}

	@Override
	public <T> ComponentAdapter<T> createComponentAdapter(
			final ComponentMonitor componentMonitor,
			final LifecycleStrategy lifecycleStrategy,
			final Properties componentProperties, final Object componentKey,
			final Class<T> componentImplementation, final Parameter... parameters)
			throws PicoCompositionException {
		// TODO Auto-generated method stub
		ComponentAdapter<T> componentAdapter = super.createComponentAdapter(
				componentMonitor, lifecycleStrategy, componentProperties,
				componentKey, componentImplementation, parameters);

		try {
			return new JNDIExposed<T>(componentAdapter);
		} catch (NamingException e) {
			throw new PicoCompositionException(
					"unable to create JNDI behaviour", e);
		}
	}

}
