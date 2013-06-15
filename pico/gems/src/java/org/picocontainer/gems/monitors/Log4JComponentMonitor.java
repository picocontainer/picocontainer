/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Paul Hammant                                             *
 *****************************************************************************/

package org.picocontainer.gems.monitors;

import static org.picocontainer.monitors.ComponentMonitorHelper.ctorToString;
import static org.picocontainer.monitors.ComponentMonitorHelper.format;
import static org.picocontainer.monitors.ComponentMonitorHelper.memberToString;
import static org.picocontainer.monitors.ComponentMonitorHelper.parmsToString;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.picocontainer.ChangedBehavior;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.Injector;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.monitors.ComponentMonitorHelper;
import org.picocontainer.monitors.NullComponentMonitor;


/**
 * A {@link org.picocontainer.ComponentMonitor} which writes to a Log4J {@link org.apache.log4j.Logger} instance.
 * The Logger instance can either be injected or, if not set, the {@link LogManager LogManager}
 * will be used to retrieve it at every invocation of the monitor.
 *
 * @author Paul Hammant
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class Log4JComponentMonitor implements ComponentMonitor, Serializable {


	/**
	 * Log4j Logger.
	 */
    private transient Logger logger;

    /**
     * Delegate Monitor.
     */
    private final ComponentMonitor delegate;

    /**
     * Creates a Log4JComponentMonitor with no Logger instance set.
     * The {@link LogManager LogManager} will be used to retrieve the Logger instance
     * at every invocation of the monitor.
     */
    public Log4JComponentMonitor() {
        delegate = new NullComponentMonitor();

    }

    /**
     * Creates a Log4JComponentMonitor with a given Logger instance class.
     * The class name is used to retrieve the Logger instance.
     *
     * @param loggerClass the class of the Logger
     */
    public Log4JComponentMonitor(final Class<?> loggerClass) {
        this(loggerClass.getName());
    }

    /**
     * Creates a Log4JComponentMonitor with a given Logger instance name. It uses the
     * {@link org.apache.log4j.LogManager LogManager} to create the Logger instance.
     *
     * @param loggerName the name of the Log
     */
    public Log4JComponentMonitor(final String loggerName) {
        this(LogManager.getLogger(loggerName));
    }

    /**
     * Creates a Log4JComponentMonitor with a given Logger instance
     *
     * @param logger the Logger to write to
     */
    public Log4JComponentMonitor(final Logger logger) {
        this();
        this.logger = logger;
    }

    /**
     * Creates a Log4JComponentMonitor with a given Logger instance class.
     * The class name is used to retrieve the Logger instance.
     *
     * @param loggerClass the class of the Logger
     * @param delegate the delegate
     */
    public Log4JComponentMonitor(final Class<?> loggerClass, final ComponentMonitor delegate) {
        this(loggerClass.getName(), delegate);
    }

    /**
     * Creates a Log4JComponentMonitor with a given Logger instance name. It uses the
     * {@link org.apache.log4j.LogManager LogManager} to create the Logger instance.
     *
     * @param loggerName the name of the Log
     * @param delegate the delegate
     */
    public Log4JComponentMonitor(final String loggerName, final ComponentMonitor delegate) {
        this(LogManager.getLogger(loggerName), delegate);
    }

    /**
     * Creates a Log4JComponentMonitor with a given Logger instance
     *
     * @param logger the Logger to write to
     * @param delegate the delegate
     */
    public Log4JComponentMonitor(final Logger logger, final ComponentMonitor delegate) {
        this(delegate);
        this.logger = logger;
    }

    public Log4JComponentMonitor(final ComponentMonitor delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} **/
    public <T> Constructor<T> instantiating(final PicoContainer container, final ComponentAdapter<T> componentAdapter,
                                     final Constructor<T> constructor) {
        Logger logger = getLogger(constructor);
        if (logger.isDebugEnabled()) {
            logger.debug(format(ComponentMonitorHelper.INSTANTIATING, ctorToString(constructor)));
        }
        return delegate.instantiating(container, componentAdapter, constructor);
    }

    /** {@inheritDoc} **/
    public <T> void instantiated(final PicoContainer container, final ComponentAdapter<T> componentAdapter,
                             final Constructor<T> constructor,
                             final Object instantiated,
                             final Object[] parameters,
                             final long duration) {
        Logger logger = getLogger(constructor);
        if (logger.isDebugEnabled()) {
            logger.debug(format(ComponentMonitorHelper.INSTANTIATED, ctorToString(constructor), duration, instantiated.getClass().getName(), parmsToString(parameters)));
        }
        delegate.instantiated(container, componentAdapter, constructor, instantiated, parameters, duration);
    }

    /** {@inheritDoc} **/
    public <T> void instantiationFailed(final PicoContainer container,
                                    final ComponentAdapter<T> componentAdapter,
                                    final Constructor<T> constructor,
                                    final Exception cause) {
        Logger logger = getLogger(constructor);
        if (logger.isEnabledFor(Priority.WARN)) {
            logger.warn(format(ComponentMonitorHelper.INSTANTIATION_FAILED, ctorToString(constructor), cause.getMessage()), cause);
        }
        delegate.instantiationFailed(container, componentAdapter, constructor, cause);
    }

    /** {@inheritDoc} **/
    public Object invoking(final PicoContainer container,
                         final ComponentAdapter<?> componentAdapter,
                         final Member member,
                         final Object instance, final Object... args) {
        Logger logger = getLogger(member);
        if (logger.isDebugEnabled()) {
            logger.debug(format(ComponentMonitorHelper.INVOKING, memberToString(member), instance));
        }
        return delegate.invoking(container, componentAdapter, member, instance, args);
    }

    /** {@inheritDoc} **/
    public void invoked(final PicoContainer container,
                        final ComponentAdapter<?> componentAdapter,
                        final Member member,
                        final Object instance,
                        final long duration,
                        final Object retVal, final Object[] args) {
        Logger logger = getLogger(member);
        if (logger.isDebugEnabled()) {
            logger.debug(format(ComponentMonitorHelper.INVOKED, memberToString(member), instance, duration));
        }
        delegate.invoked(container, componentAdapter, member, instance, duration, retVal, args);
    }

    /** {@inheritDoc} **/
    public void invocationFailed(final Member member, final Object instance, final Exception cause) {
        Logger logger = getLogger(member);
        if (logger.isEnabledFor(Priority.WARN)) {
            logger.warn(format(ComponentMonitorHelper.INVOCATION_FAILED, memberToString(member), instance, cause.getMessage()), cause);
        }
        delegate.invocationFailed(member, instance, cause);
    }

    /** {@inheritDoc} **/
    public void lifecycleInvocationFailed(final MutablePicoContainer container,
                                          final ComponentAdapter<?> componentAdapter, final Method method,
                                          final Object instance,
                                          final RuntimeException cause) {
        Logger logger = getLogger(method);
        if (logger.isEnabledFor(Priority.WARN)) {
            logger.warn(format(ComponentMonitorHelper.LIFECYCLE_INVOCATION_FAILED, memberToString(method), instance, cause.getMessage()), cause);
        }
        delegate.lifecycleInvocationFailed(container, componentAdapter, method, instance, cause);
    }

    /** {@inheritDoc} **/
    public Object noComponentFound(final MutablePicoContainer container, final Object key) {
        Logger logger = this.logger != null ? this.logger : LogManager.getLogger(ComponentMonitor.class);
        if (logger.isEnabledFor(Priority.WARN)) {
            logger.warn(format(ComponentMonitorHelper.NO_COMPONENT, key));
        }
        return delegate.noComponentFound(container, key);

    }

    /** {@inheritDoc} */
    public Injector newInjector(final Injector injector) {
        return delegate.newInjector(injector);
    }

    /** {@inheritDoc} **/
    public ChangedBehavior changedBehavior(final ChangedBehavior changedBehavior) {
        return delegate.changedBehavior(changedBehavior);
    }

    protected synchronized Logger getLogger(final Member member) {
        if (logger != null) {
            return logger;
        }
        return LogManager.getLogger(member.getDeclaringClass());
    }


    /**
     * Serializes the monitor.
     * @param oos object output stream.
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
     * @param ois
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(final ObjectInputStream ois) throws IOException, ClassNotFoundException {
    	ois.defaultReadObject();
    	boolean hasDefaultLogger = ois.readBoolean();
    	if (hasDefaultLogger) {
	    	String defaultLoggerCategory = ois.readUTF();
	    	assert defaultLoggerCategory != null : "Serialization indicated default logger, "
	    		+"but no logger category found in input stream.";
    		logger = LogManager.getLogger(defaultLoggerCategory);
    	}
    }

}
