/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Mauro Talevi                                             *
 *****************************************************************************/

package org.picocontainer.gems.monitors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.picocontainer.ChangedBehavior;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.Injector;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.monitors.ComponentMonitorHelper;
import org.picocontainer.monitors.NullComponentMonitor;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static org.picocontainer.monitors.ComponentMonitorHelper.ctorToString;
import static org.picocontainer.monitors.ComponentMonitorHelper.memberToString;
import static org.picocontainer.monitors.ComponentMonitorHelper.methodToString;
import static org.picocontainer.monitors.ComponentMonitorHelper.parmsToString;


/**
 * A {@link ComponentMonitor} which writes to a Commons Logging {@link Log Log} instance.
 * The Log instance can either be injected or, if not set, the {@link LogFactory LogFactory}
 * will be used to retrieve it at every invocation of the monitor.
 * <h4>Note on Serialization</h4>
 * <p>Commons Logging does <em>not</em> guarantee Serialization.  It is supported when using Log4j
 * as a back end, but you should write a test case to determine if your particular logger implementation
 * is supported if you plan on serializing this ComponentMonitor.</p>
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class CommonsLoggingComponentMonitor implements ComponentMonitor, Serializable {


	/**
	 * Commons Logger.
	 */
	private Log log;
	
   
	/**
	 * Delegate for component monitor chains.
	 */
    private final ComponentMonitor delegate;

    /**
     * Creates a CommonsLoggingComponentMonitor with no Log instance set.
     * The {@link LogFactory LogFactory} will be used to retrieve the Log instance
     * at every invocation of the monitor.
     */
    public CommonsLoggingComponentMonitor() {
        delegate = new NullComponentMonitor();
    }

    /**
     * Creates a CommonsLoggingComponentMonitor with a given Log instance class.
     * The class name is used to retrieve the Log instance.
     * 
     * @param logClass the class of the Log
     */
    public CommonsLoggingComponentMonitor(final Class<?> logClass) {
        this(logClass.getName());
    }

    /**
     * Creates a CommonsLoggingComponentMonitor with a given Log instance name. It uses the
     * {@link LogFactory LogFactory} to create the Log instance.
     * 
     * @param logName the name of the Log
     */
    public CommonsLoggingComponentMonitor(final String logName) {
        this(LogFactory.getLog(logName));
    }

    /**
     * Creates a CommonsLoggingComponentMonitor with a given Log instance
     * @param log the Log to write to
     */
    public CommonsLoggingComponentMonitor(final Log log) {
        this();
        this.log = log;        
    }

    /**
     * Creates a CommonsLoggingComponentMonitor with a given Log instance class.
     * The class name is used to retrieve the Log instance.
     *
     * @param logClass the class of the Log
     * @param delegate the delegate
     */
    public CommonsLoggingComponentMonitor(final Class<?> logClass, final ComponentMonitor delegate) {
        this(logClass.getName(), delegate);
    }

    /**
     * Creates a CommonsLoggingComponentMonitor with a given Log instance name. It uses the
     * {@link LogFactory LogFactory} to create the Log instance.
     *
     * @param logName the name of the Log
     * @param delegate the delegate
     */
    public CommonsLoggingComponentMonitor(final String logName, final ComponentMonitor delegate) {
        this(LogFactory.getLog(logName), delegate);
    }

    /**
     * Creates a CommonsLoggingComponentMonitor with a given Log instance.
     * @param log the Log with which to write events.
     * @param delegate the delegate
     */
    public CommonsLoggingComponentMonitor(final Log log, final ComponentMonitor delegate) {
        this.log = log;
        this.delegate = delegate;
    }


    /** {@inheritDoc} **/
   public <T> Constructor<T> instantiating(final PicoContainer container, final ComponentAdapter<T> componentAdapter,
                                     final Constructor<T> constructor) {
        Log log = getLog(constructor);
        if (log.isDebugEnabled()) {
            log.debug(ComponentMonitorHelper.format(ComponentMonitorHelper.INSTANTIATING, ctorToString(constructor)));
        }
        return delegate.instantiating(container, componentAdapter, constructor);
    }

   /** {@inheritDoc} **/
    public <T> void instantiated(final PicoContainer container, final ComponentAdapter<T> componentAdapter,
                             final Constructor<T> constructor,
                             final Object instantiated,
                             final Object[] parameters,
                             final long duration) {
        Log log = getLog(constructor);
        if (log.isDebugEnabled()) {
            log.debug(ComponentMonitorHelper.format(ComponentMonitorHelper.INSTANTIATED, ctorToString(constructor), duration, instantiated.getClass().getName(), parmsToString(parameters)));
        }
        delegate.instantiated(container, componentAdapter, constructor, instantiated, parameters, duration);
    }

    /** {@inheritDoc} **/
    public <T> void instantiationFailed(final PicoContainer container,
                                    final ComponentAdapter<T>  componentAdapter,
                                    final Constructor<T>  constructor,
                                    final Exception cause) {
        Log log = getLog(constructor);
        if (log.isWarnEnabled()) {
            log.warn(ComponentMonitorHelper.format(ComponentMonitorHelper.INSTANTIATION_FAILED, ctorToString(constructor), cause.getMessage()), cause);
        }
        delegate.instantiationFailed(container, componentAdapter, constructor, cause);
    }

    /** {@inheritDoc} **/
    public Object invoking(final PicoContainer container,
                         final ComponentAdapter<?> componentAdapter,
                         final Member member,
                         final Object instance,
                         final Object... args) {
        Log log = getLog(member);
        if (log.isDebugEnabled()) {
            log.debug(ComponentMonitorHelper.format(ComponentMonitorHelper.INVOKING, memberToString(member), instance));
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
        Log log = getLog(member);
        if (log.isDebugEnabled()) {
            log.debug(ComponentMonitorHelper.format(ComponentMonitorHelper.INVOKED, memberToString(member), instance, duration));
        }
        delegate.invoked(container, componentAdapter, member, instance,  duration, retVal, args);
    }

    /** {@inheritDoc} **/
    public void invocationFailed(final Member member, final Object instance, final Exception cause) {
        Log log = getLog(member);
        if (log.isWarnEnabled()) {
            log.warn(ComponentMonitorHelper.format(ComponentMonitorHelper.INVOCATION_FAILED, memberToString(member), instance, cause.getMessage()), cause);
        }
        delegate.invocationFailed(member, instance, cause);
    }

    /** {@inheritDoc} **/
    public void lifecycleInvocationFailed(final MutablePicoContainer container,
                                          final ComponentAdapter<?> componentAdapter, final Method method,
                                          final Object instance,
                                          final RuntimeException cause) {
        Log log = getLog(method);
        if (log.isWarnEnabled()) {
            log.warn(ComponentMonitorHelper.format(ComponentMonitorHelper.LIFECYCLE_INVOCATION_FAILED, methodToString(method), instance, cause.getMessage()), cause);
        }
        delegate.lifecycleInvocationFailed(container, componentAdapter, method, instance, cause);
    }

    /** {@inheritDoc} **/
    public Object noComponentFound(final MutablePicoContainer container, final Object key) {
        Log log = this.log != null ? this.log : LogFactory.getLog(ComponentMonitor.class);
        if (log.isWarnEnabled()) {
            log.warn(ComponentMonitorHelper.format(ComponentMonitorHelper.NO_COMPONENT, key));
        }
        return delegate.noComponentFound(container, key);
    }

    /** {@inheritDoc} **/
    public Injector newInjector(final Injector injector) {
        return delegate.newInjector(injector);
    }

    /** {@inheritDoc} **/
    public ChangedBehavior changedBehavior(ChangedBehavior changedBehavior) {
        return delegate.changedBehavior(changedBehavior);
    }

    /**
     * Retrieves the logger appropriate for the calling member's class.
     * @param member constructor/method/field who's callback is required.
     * @return the Commons logging instance.
     */
    protected synchronized Log getLog(final Member member) {
        if (log != null) {
            return log;
        } 
        return LogFactory.getLog(member.getDeclaringClass());
    }

    
}
