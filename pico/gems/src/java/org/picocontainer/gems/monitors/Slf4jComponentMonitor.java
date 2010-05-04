/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Paul Hammaant                                            *
 *****************************************************************************/
package org.picocontainer.gems.monitors;

import org.picocontainer.ChangedBehavior;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.Injector;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.monitors.ComponentMonitorHelper;
import org.picocontainer.monitors.NullComponentMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static org.picocontainer.monitors.ComponentMonitorHelper.ctorToString;
import static org.picocontainer.monitors.ComponentMonitorHelper.format;
import static org.picocontainer.monitors.ComponentMonitorHelper.memberToString;
import static org.picocontainer.monitors.ComponentMonitorHelper.methodToString;
import static org.picocontainer.monitors.ComponentMonitorHelper.parmsToString;

/**
 * A {@link org.picocontainer.ComponentMonitor} which writes to a Slf4j
 * {@link org.slf4j.Logger} instance. The Logger instance can either be injected
 * or, if not set, the {@link org.slf4j.LoggerFactory} will be used to retrieve
 * it at every invocation of the monitor.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 * @author Michael Rimov
 */
@SuppressWarnings("serial")
public class Slf4jComponentMonitor implements ComponentMonitor, Serializable {


	/**
	 * Slf4j Logger.
	 */
	private transient Logger logger;

	/**
	 * Delegate Monitor.
	 */
	private final ComponentMonitor delegate;

	/**
	 * Creates a Slf4jComponentMonitor with no Logger instance set. The
	 * {@link org.slf4j.LoggerFactory} will be used to retrieve the Logger
	 * instance at every invocation of the monitor.
	 */
	public Slf4jComponentMonitor() {
		delegate = new NullComponentMonitor();

	}

	/**
	 * Creates a Slf4jComponentMonitor with a given Logger instance class. The
	 * class name is used to retrieve the Logger instance.
	 * 
	 * @param loggerClass
	 *            the class of the Logger
	 */
	public Slf4jComponentMonitor(final Class<?> loggerClass) {
		this(loggerClass.getName());
	}

	/**
	 * Creates a Slf4jComponentMonitor with a given Logger instance name. It
	 * uses the {@link org.slf4j.LoggerFactory} to create the Logger instance.
	 * 
	 * @param loggerName
	 *            the name of the Log
	 */
	public Slf4jComponentMonitor(final String loggerName) {
		this(LoggerFactory.getLogger(loggerName));
	}

	/**
	 * Creates a Slf4jComponentMonitor with a given Logger instance
	 * 
	 * @param logger
	 *            the Logger to write to
	 */
	public Slf4jComponentMonitor(final Logger logger) {
		this();
		this.logger = logger;
	}

	/**
	 * Creates a Slf4jComponentMonitor with a given Logger instance class. The
	 * class name is used to retrieve the Logger instance.
	 * 
	 * @param loggerClass
	 *            the class of the Logger
	 * @param delegate
	 *            the delegate
	 */
	public Slf4jComponentMonitor(final Class<?> loggerClass,
			final ComponentMonitor delegate) {
		this(loggerClass.getName(), delegate);
	}

	/**
	 * Creates a Slf4jComponentMonitor with a given Logger instance name. It
	 * uses the {@link org.slf4j.LoggerFactory} to create the Logger instance.
	 * 
	 * @param loggerName
	 *            the name of the Log
	 * @param delegate
	 *            the delegate
	 */
	public Slf4jComponentMonitor(final String loggerName,
			final ComponentMonitor delegate) {
		this(LoggerFactory.getLogger(loggerName), delegate);
	}

	/**
	 * Creates a Slf4jComponentMonitor with a given Slf4j Logger instance
	 * 
	 * @param logger
	 *            the Logger to write to
	 * @param delegate
	 *            the delegate
	 */
	public Slf4jComponentMonitor(final Logger logger,
			final ComponentMonitor delegate) {
		this(delegate);
		this.logger = logger;
	}

	/**
	 * Similar to default constructor behavior, but this version wraps a
	 * delegate ComponentMonitor.
	 * 
	 * @param delegate
	 *            The next component monitor in the chain.
	 */
	public Slf4jComponentMonitor(final ComponentMonitor delegate) {
		this.delegate = delegate;
	}

	/** {@inheritDoc} * */
	public <T> Constructor<T> instantiating(final PicoContainer container,
			final ComponentAdapter<T> componentAdapter,
			final Constructor<T> constructor) {
		Logger logger = getLogger(constructor);
		if (logger.isDebugEnabled()) {
			logger.debug(format(ComponentMonitorHelper.INSTANTIATING,
					ctorToString(constructor)));
		}
		return delegate.instantiating(container, componentAdapter, constructor);
	}

	/** {@inheritDoc} * */
	public <T> void instantiated(final PicoContainer container,
			final ComponentAdapter<T> componentAdapter,
			final Constructor<T> constructor, final Object instantiated,
			final Object[] parameters, final long duration) {
		Logger logger = getLogger(constructor);
		if (logger.isDebugEnabled()) {
			logger.debug(format(ComponentMonitorHelper.INSTANTIATED,
					ctorToString(constructor), duration, instantiated
							.getClass().getName(), parmsToString(parameters)));
		}
		delegate.instantiated(container, componentAdapter, constructor,
				instantiated, parameters, duration);
	}

	/** {@inheritDoc} * */
	public <T> void instantiationFailed(final PicoContainer container,
			final ComponentAdapter<T> componentAdapter,
			final Constructor<T> constructor, final Exception cause) {
		Logger logger = getLogger(constructor);
		if (logger.isWarnEnabled()) {
			logger.warn(format(ComponentMonitorHelper.INSTANTIATION_FAILED,
					ctorToString(constructor), cause.getMessage()), cause);
		}
		delegate.instantiationFailed(container, componentAdapter, constructor,
				cause);
	}

	/** {@inheritDoc} * */
	public Object invoking(final PicoContainer container,
			final ComponentAdapter<?> componentAdapter, final Member member,
			final Object instance, Object... args) {
		Logger logger = getLogger(member);
		if (logger.isDebugEnabled()) {
			logger.debug(format(ComponentMonitorHelper.INVOKING,
					memberToString(member), instance));
		}
		return delegate.invoking(container, componentAdapter, member, instance, args);
	}

	/** {@inheritDoc} * */
	public void invoked(final PicoContainer container,
                        final ComponentAdapter<?> componentAdapter, final Member member,
                        final Object instance, final long duration, Object retVal, Object[] args) {
		Logger logger = getLogger(member);
		if (logger.isDebugEnabled()) {
			logger.debug(format(ComponentMonitorHelper.INVOKED,
					methodToString(member), instance, duration));
		}
		delegate.invoked(container, componentAdapter, member, instance,
				duration, retVal, args);
	}

	/** {@inheritDoc} * */
	public void invocationFailed(final Member member, final Object instance,
			final Exception cause) {
		Logger logger = getLogger(member);
		if (logger.isWarnEnabled()) {
			logger.warn(format(ComponentMonitorHelper.INVOCATION_FAILED,
					memberToString(member), instance, cause.getMessage()),
					cause);
		}
		delegate.invocationFailed(member, instance, cause);
	}

	/** {@inheritDoc} * */
	public void lifecycleInvocationFailed(final MutablePicoContainer container,
			final ComponentAdapter<?> componentAdapter, final Method method,
			final Object instance, final RuntimeException cause) {
		Logger logger = getLogger(method);
		if (logger.isWarnEnabled()) {
			logger.warn(format(
					ComponentMonitorHelper.LIFECYCLE_INVOCATION_FAILED,
					methodToString(method), instance, cause.getMessage()),
					cause);
		}
		delegate.lifecycleInvocationFailed(container, componentAdapter, method,
				instance, cause);
	}

	/** {@inheritDoc} * */
	public Object noComponentFound(final MutablePicoContainer container,
			final Object key) {
		Logger logger = this.logger != null ? this.logger : LoggerFactory
				.getLogger(ComponentMonitor.class);
		if (logger.isWarnEnabled()) {
			logger.warn(format(ComponentMonitorHelper.NO_COMPONENT,
					key));
		}
		return delegate.noComponentFound(container, key);

	}

	/** {@inheritDoc} */
	public Injector newInjector(
			final Injector injector) {
		return delegate.newInjector(injector);
	}

    /** {@inheritDoc} **/
    public ChangedBehavior changedBehavior(ChangedBehavior changedBehavior) {
        return delegate.changedBehavior(changedBehavior);
    }


    /**
	 * Retrieves the logger factory based class being instantiated.
	 * 
	 * @param member
	 *            Source method/constructor, etc being instantiated.
	 * @return an appropriate logger instance for this callback.
	 */
	protected synchronized Logger getLogger(final Member member) {
		if (logger != null) {
			return logger;
		}
		return LoggerFactory.getLogger(member.getDeclaringClass());
	}

	/**
	 * Serializes the monitor.
	 * 
	 * @param oos
	 *            object output stream.
	 * @throws IOException
	 */
	private void writeObject(final ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		if (logger != null) {
			oos.writeBoolean(true);
			oos.writeUTF(logger.getName());
		} else {
			oos.writeBoolean(false);
		}
	}

	/**
	 * Manually creates a new logger instance if it was defined earlier.
	 * 
	 * @param ois
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(final ObjectInputStream ois) throws IOException,
			ClassNotFoundException {
		ois.defaultReadObject();
		boolean hasDefaultLogger = ois.readBoolean();
		if (hasDefaultLogger) {
			String defaultLoggerCategory = ois.readUTF();
			assert defaultLoggerCategory != null : "Serialization indicated default logger, "
					+ "but no logger category found in input stream.";
			logger = LoggerFactory.getLogger(defaultLoggerCategory);
		}
	}
}
