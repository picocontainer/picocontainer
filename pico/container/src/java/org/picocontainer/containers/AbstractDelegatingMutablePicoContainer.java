/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by the committers                                           *
 *****************************************************************************/
package org.picocontainer.containers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.lifecycle.LifecycleState;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

/**
 * abstract base class for delegating to mutable containers
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public abstract class AbstractDelegatingMutablePicoContainer extends AbstractDelegatingPicoContainer implements MutablePicoContainer {

    public AbstractDelegatingMutablePicoContainer(final MutablePicoContainer delegate) {
		super(delegate);
	}

    public <T> BindWithOrTo<T> bind(final Class<T> type) {
        return getDelegate().bind(type);
    }

	public MutablePicoContainer addComponent(final Object key,
                                             final Object implOrInstance,
                                             final Parameter... parameters) throws PicoCompositionException {
        getDelegate().addComponent(key, implOrInstance, parameters);
        return this;
    }

    public MutablePicoContainer addComponent(final Object implOrInstance) throws PicoCompositionException {
	     getDelegate().addComponent(implOrInstance);
	     return this;
    }



	public MutablePicoContainer addComponent(final Object key, final Object implOrInstance,
			final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) {
		getDelegate().addComponent(key, implOrInstance, constructorParams, fieldParams, methodParams);
		return this;
	}


    public MutablePicoContainer addConfig(final String name, final Object val) {
        getDelegate().addConfig(name, val);
        return this;
    }

    public MutablePicoContainer addAdapter(final ComponentAdapter<?> componentAdapter) throws PicoCompositionException {
        getDelegate().addAdapter(componentAdapter);
        return this;
    }

    public MutablePicoContainer addProvider(final javax.inject.Provider<?> provider) {
        getDelegate().addProvider(provider);
        return this;
    }

    public MutablePicoContainer addProvider(final Object key, final javax.inject.Provider<?> provider) {
    	getDelegate().addProvider(key, provider);
    	return this;
    }


    public <T> ComponentAdapter<T> removeComponent(final Object key) {
        return getDelegate().removeComponent(key);
    }

    public <T> ComponentAdapter<T> removeComponentByInstance(final T componentInstance) {
        return getDelegate().removeComponentByInstance(componentInstance);
    }

    public MutablePicoContainer addChildContainer(final PicoContainer child) {
        getDelegate().addChildContainer(child);
        return this;
    }

    public boolean removeChildContainer(final PicoContainer child) {
        return getDelegate().removeChildContainer(child);
    }

	public MutablePicoContainer change(final Properties... properties) {
	    getDelegate().change(properties);
	    return this;
	}

	public MutablePicoContainer as(final Properties... properties) {
		//
		//DefaultMutablePicoContainer.as() returns a different container instance
		//For as() to work, we need to swap to the new container.
		//
	    MutablePicoContainer resultingDelegate = getDelegate().as(properties);

	    OneRegistrationSwappingInvocationHandler tempInvocationHandler = new OneRegistrationSwappingInvocationHandler(this, resultingDelegate);
	    MutablePicoContainer proxiedDelegate =   (MutablePicoContainer) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {MutablePicoContainer.class}, tempInvocationHandler);
	    this.swapDelegate(proxiedDelegate);
	    return proxiedDelegate;
	}

	public void dispose() {
		getDelegate().dispose();
	}

	abstract public MutablePicoContainer makeChildContainer();

	public void start() {
		getDelegate().start();
	}

	public void stop() {
		getDelegate().stop();
	}

	@Override
	public MutablePicoContainer getDelegate() {
		return (MutablePicoContainer) super.getDelegate();
	}

    public void setName(final String name) {
        getDelegate().setName(name);
    }

    public void setLifecycleState(final LifecycleState lifecycleState) {
        getDelegate().setLifecycleState(lifecycleState);
    }

    /** {@inheritDoc} **/
    public LifecycleState getLifecycleState() {
        return getDelegate().getLifecycleState();
    }

    /** {@inheritDoc} **/
    public String getName() {
        return getDelegate().getName();
    }

    public void changeMonitor(final ComponentMonitor monitor) {
        getDelegate().changeMonitor(monitor);
    }


	@Override
	protected MutablePicoContainer swapDelegate(final PicoContainer newDelegate) {
		return (MutablePicoContainer)super.swapDelegate(newDelegate);
	}

	/**
	 * Allows invocation of addComponent(*) once and then reverts the delegate back to the old instance.
	 * @author Michael Rimov
	 *
	 */
	public static class OneRegistrationSwappingInvocationHandler implements InvocationHandler {

		private final AbstractDelegatingMutablePicoContainer owner;

		private final MutablePicoContainer oldDelegate;

		private final MutablePicoContainer oneShotPico;

		public OneRegistrationSwappingInvocationHandler(final AbstractDelegatingMutablePicoContainer owner, final MutablePicoContainer oneShotPico) {
			this.owner = owner;
			this.oneShotPico = oneShotPico;
			oldDelegate = owner.getDelegate();
		}

		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			//Invoke delegate no matter what.  No problem there.
			try {
				Object result =  method.invoke(oneShotPico, args);
				String methodName = method.getName();

				//If the method is one of the addComponent() methods, then swap the delegate back to the
				//old value.
				if (methodName.startsWith("addComponent") || methodName.startsWith("addAdapter") || methodName.startsWith("addProvider")) {
					owner.swapDelegate(oldDelegate);
				}

				//Swap back to the original owner now
				return owner;
			} catch(InvocationTargetException e) {
				Throwable nestedException = e.getTargetException();
				if (nestedException instanceof RuntimeException) {
					throw nestedException;
				}

				//Otherwise
				throw new PicoCompositionException("Error in proxy", e);
			} catch (Throwable e) {
				//Gotta catch to avoid endless loops :(
				if (e instanceof RuntimeException) {
					throw e;
				}
				//Make sure we don't have checked exceptions propagating up for some reason
				throw new PicoCompositionException("Error in proxy", e);
			}
		}

	}
}
