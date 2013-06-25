/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaible                                           *
 *****************************************************************************/

package com.picocontainer.gems.jmx;

/**
 * An abstract ObjectNameFactory that offers functionality to handle the domain part of the object name.
 * @author J&ouml;rg Schaible
 */
public abstract class AbstractObjectNameFactory implements ObjectNameFactory {

    private final String domain;

    /**
     * Construct an AbstractObjectNameFactory.
     * @param domain The name of the domain, use <code>null</code> for the default domain.
     */
    protected AbstractObjectNameFactory(final String domain) {
        this.domain = domain;
    }

    /**
     * @return Return the domain part of the {@link javax.management.ObjectName}.
     */
    protected String getDomain() {
        return domain != null ? domain : "";
    }
}
