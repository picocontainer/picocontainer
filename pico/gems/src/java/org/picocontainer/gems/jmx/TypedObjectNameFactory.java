/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaible                                           *
 *****************************************************************************/

package org.picocontainer.gems.jmx;

import javax.management.DynamicMBean;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


/**
 * An ObjectNameFactory, that uses the type of the {@link DynamicMBean} implementation to register. The value of the
 * type is the name of the implementation class without the package name.
 * @author J&ouml;rg Schaible
 */
public class TypedObjectNameFactory extends AbstractObjectNameFactory {

    /**
     * Construct a TypedObjectNameFactory using the default domain. Using <code>TypedObjectNameFactory(null)</code> is
     * equivalent.
     */
    public TypedObjectNameFactory() {
        this(null);
    }

    /**
     * Construct a TypedObjectNameFactory with a predefined domain.
     * @param domain The domain.
     */
    public TypedObjectNameFactory(final String domain) {
        super(domain);
    }

    /**
     * Create an {@link ObjectName} with the class name of the MBean implementation as key <em>type</em>.
     * @see org.picocontainer.gems.jmx.ObjectNameFactory#create(java.lang.Object, javax.management.DynamicMBean)
     */
    public ObjectName create(final Object key, final DynamicMBean mBean) throws MalformedObjectNameException {
        final String className = mBean.getMBeanInfo().getClassName();
        return new ObjectName(getDomain(), "type", className.substring(className.lastIndexOf('.') + 1));
    }

}
