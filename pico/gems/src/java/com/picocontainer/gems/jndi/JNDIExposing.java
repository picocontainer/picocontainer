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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.behaviors.AbstractBehavior;
import com.picocontainer.behaviors.Storing;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;

/**
 * produce JNDI exposing behaviour
 *
 * @author Konstantin Pribluda
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public class JNDIExposing extends AbstractBehavior {

	@Override
	public <T> ComponentAdapter<T> addComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle,
			final Properties componentProps, final ComponentAdapter<T> adapter) {
		try {
			return new JNDIExposed<T>(super.addComponentAdapter(
					monitor, lifecycle, componentProps,
					adapter));
		} catch (NamingException e) {
			throw new PicoCompositionException(
					"unable to create JNDI behaviour", e);
		}
	}

	@Override
	public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle,
			final Properties componentProps, final Object key, final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {
		// TODO Auto-generated method stub
		ComponentAdapter<T> componentAdapter = super.createComponentAdapter(monitor, lifecycle, componentProps, key, impl, constructorParams, fieldParams, methodParams);
		try {
			return new JNDIExposed<T>(componentAdapter);
		} catch (NamingException e) {
			throw new PicoCompositionException(
					"unable to create JNDI behaviour", e);
		}
	}

    /**
     * exposes component to JNDI basically does same thing as cached, but uses JNDI
     * reference instead. Maybe Cached shall be refactored? as there is little new
     * functionality.
     *
     * @author Konstantin Pribluda
     *
     */
    public static class JNDIExposed<T> extends Storing.Stored<T> {


        /**
         * construct reference itself using vanilla initial context.
         * JNDI name is stringified component key
         * @param delegate
         *            delegate adapter

         * @throws javax.naming.NamingException
         */
        public JNDIExposed(final ComponentAdapter<T> delegate) throws NamingException {
            super(delegate, new JNDIObjectReference<Instance<T>>(delegate.getComponentKey()
                    .toString(), new InitialContext()));
        }

        /**
         * create with provided reference
         *
         * @param delegate
         * @param instanceReference
         */
        public JNDIExposed(final ComponentAdapter<T> delegate,
                final JNDIObjectReference<Instance<T>> instanceReference) {
            super(delegate, instanceReference);
        }

        /**
         * create adapter with desired name
         * @param delegate
         * @param name
         * @throws javax.naming.NamingException
         */
        public JNDIExposed(final ComponentAdapter<T> delegate, final String name) throws NamingException {
            super(delegate, new JNDIObjectReference<Instance<T>>(name, new InitialContext()));
        }

        @Override
        public String toString() {
            return "JNDI" + getDelegate().toString();
        }
    }
}
