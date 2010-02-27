/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Michael Ward                                    		 *
 *****************************************************************************/

package org.picocontainer.gems.jmx;

import java.util.HashSet;
import java.util.Set;

import javax.management.DynamicMBean;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.visitors.TraversalCheckingVisitor;


/**
 * A {@link org.picocontainer.PicoVisitor} to register JMX components for components of a {@link PicoContainer} tree in
 * a {@link MBeanServer}.
 * @author Michael Ward
 * @author J&ouml;rg Schaible
 */
public class JMXVisitor extends TraversalCheckingVisitor {
    private final DynamicMBeanProvider[] mBeanProviders;
    private final MBeanServer mBeanServer;
    private final Set visited;
    private final Set registeredInfo;
    private PicoContainer picoContainer;

    /**
     * Construct a JMXVisitor. This instance will register by default any component in the {@link MBeanServer}, that is
     * already a {@link DynamicMBean}. The {@link ObjectName} will use the default domain of the MBeanServer and has a
     * <em>type</em> key with the class name (without package name) as value.
     * @param server The {@link MBeanServer}to use for registering the MBeans.
     */
    public JMXVisitor(final MBeanServer server) {
        this(server, new DynamicMBeanProvider[]{new DynamicMBeanComponentProvider()});
    }

    /**
     * Construct a JMXVisitor.
     * @param server The {@link MBeanServer} to use for registering the MBeans.
     * @param providers The providers to deliver the DynamicMBeans.
     */
    public JMXVisitor(final MBeanServer server, final DynamicMBeanProvider[] providers) {
        if (server == null) {
            throw new NullPointerException("MBeanServer may not be null");
        }
        if (providers == null) {
            throw new NullPointerException("DynamicMBeanProvider[] may not be null");
        }
        if (providers.length == 0) {
            throw new IllegalArgumentException("DynamicMBeanProvider[] may not be empty");
        }
        mBeanServer = server;
        mBeanProviders = providers;
        visited = new HashSet();
        registeredInfo = new HashSet();
    }

    /**
     * Entry point for the visitor traversal.
     * @return Returns a {@link Set} with all ObjectInstance instances retrieved from the {@link MBeanServer} for the
     *         registered MBeans.
     * @see org.picocontainer.visitors.AbstractPicoVisitor#traverse(java.lang.Object)
     */
    @Override
	public Object traverse(final Object node) {
        super.traverse(node);
        picoContainer = null;
        final Set set = new HashSet(registeredInfo);
        registeredInfo.clear();
        return set;
    }

    /**
     * Provides the PicoContainer, that can resolve the components to register as MBean.
     * @see org.picocontainer.PicoVisitor#visitContainer(org.picocontainer.PicoContainer)
     */
    @Override
	public boolean visitContainer(final PicoContainer pico) {
        super.visitContainer(pico);
        picoContainer = pico;
        visited.clear();
        return CONTINUE_TRAVERSAL;
    }

    /**
     * Register the component as MBean. The implementation uses the known DynamicMBeanProvider instances to get the
     * MBean from the component.
     * @see org.picocontainer.PicoVisitor#visitComponentAdapter(org.picocontainer.ComponentAdapter)
     */
    @Override
	public void visitComponentAdapter(final ComponentAdapter componentAdapter) {
        super.visitComponentAdapter(componentAdapter);
        if (picoContainer == null) {
            throw new JMXRegistrationException("Cannot start JMXVisitor traversal with a ComponentAdapter");
        }
        if (!visited.contains(componentAdapter.getComponentKey())) {
            visited.add(componentAdapter.getComponentKey());
            for (final DynamicMBeanProvider provider : mBeanProviders) {
                final JMXRegistrationInfo info = provider.provide(picoContainer, componentAdapter);
                if (info != null) {
                    registeredInfo.add(register(info.getMBean(), info.getObjectName()));
                    break;
                }
            }
        }

    }

    /**
     * Register a MBean in the MBeanServer.
     * @param dynamicMBean the {@link DynamicMBean} to register.
     * @param objectName the {@link ObjectName} of the MBean registered the {@link MBeanServer}.
     * @return Returns the {@link ObjectInstance} returned from the MBeanServer after registration.
     * @throws JMXRegistrationException Thrown if MBean cannot be registered.
     */
    protected ObjectInstance register(final DynamicMBean dynamicMBean, final ObjectName objectName)
            throws JMXRegistrationException {
        try {
            return mBeanServer.registerMBean(dynamicMBean, objectName);
        } catch (final JMException e) {
            throw new JMXRegistrationException("Unable to register MBean to MBean Server", e);
        }
    }
}
