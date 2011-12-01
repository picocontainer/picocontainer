/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaible                                           *
 *****************************************************************************/

package org.picocontainer.gems.jmx.testmodel;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;


/**
 * PersonMBean that is already a DynamicMBean.
 * @author J&ouml;rg Schaible
 */
public class DynamicMBeanPerson extends StandardMBean implements PersonMBean {

    private String name;

    /**
     * Construct a DynamicMBeanPerson.
     * @throws NotCompliantMBeanException
     */
    public DynamicMBeanPerson() throws NotCompliantMBeanException {
        super(PersonMBean.class);
    }

    /**
     * {@inheritDoc}
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }

}
