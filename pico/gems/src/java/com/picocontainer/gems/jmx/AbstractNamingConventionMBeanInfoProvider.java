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

import javax.management.MBeanInfo;

import com.picocontainer.PicoContainer;


/**
 * Abstract base class for MBeanInfoProvider that search MBeanInfo in the PicoContainer registered with a key that
 * follows naming conventions.
 * @author J&ouml;rg Schaible
 */
public abstract class AbstractNamingConventionMBeanInfoProvider implements MBeanInfoProvider {

    /**
     * Locate a MBeanInfo as component in a PicoContainer. If no component is registered using the name of the MBeanInfo
     * as key, the method turns the name into a type and searches again.
     * @param mBeanInfoName The name of the {@link MBeanInfo} used as key.
     * @param picoContainer The {@link PicoContainer} used for the lookup.
     * @param classLoader The {@link ClassLoader} used to load the type of the key.
     * @return Returns the MBeanInfo instance or <code>null</code>.
     */
    protected MBeanInfo instantiateMBeanInfo(
            final String mBeanInfoName, final PicoContainer picoContainer, ClassLoader classLoader) {
        MBeanInfo mBeanInfo = null;
        try {
            mBeanInfo = (MBeanInfo)picoContainer.getComponent(mBeanInfoName);
        } catch (final ClassCastException e) {
            // wrong type, search goes on
        }
        if (mBeanInfo == null) {
            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            try {
                final Class mBeanInfoType = classLoader.loadClass(mBeanInfoName);
                if (MBeanInfo.class.isAssignableFrom(mBeanInfoType)) {
                    mBeanInfo = (MBeanInfo)picoContainer.getComponent(mBeanInfoType);
                }
            } catch (final ClassNotFoundException e) {
                // no such class
            }
        }
        return mBeanInfo;
    }

}
