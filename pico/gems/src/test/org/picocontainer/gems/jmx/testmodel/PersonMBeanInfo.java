/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaible                                           *
 *****************************************************************************/

package org.picocontainer.gems.jmx.testmodel;

import javax.management.MBeanInfo;


/**
 * @author J&ouml;rg Schaible
 */
@SuppressWarnings("serial")
public class PersonMBeanInfo extends MBeanInfo {

	final static MBeanInfo INFO = Person.createMBeanInfo();

    public PersonMBeanInfo() {
        super(INFO.getClassName(), INFO.getDescription(), INFO.getAttributes(), INFO.getConstructors(), INFO
                .getOperations(), INFO.getNotifications());
    }

}
