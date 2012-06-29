/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.containers;

import java.util.Properties;

import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

/**
 * immutable pico container constructed from properties.
 * intendet to be used with config parameter
 * 
 * @author Konstantin Pribluda
 *
 */
@SuppressWarnings("serial")
public class PropertiesPicoContainer extends AbstractDelegatingPicoContainer {

	/**
	 * create with parent container and populate from properties
	 * @param properties
	 * @param parent
	 */
	public PropertiesPicoContainer(Properties properties, PicoContainer parent) {
		super(new DefaultPicoContainer(parent));		
		// populate container from properties
		for(Object key: properties.keySet()) {
			((MutablePicoContainer)getDelegate()).addComponent(key,properties.get(key));
		}
	}

    /**
	 * construct without a parent
	 * @param properties
	 */
	public PropertiesPicoContainer(Properties properties) {
		this(properties,null);
	}

    public void setName(String s) {
        ((MutablePicoContainer)getDelegate()).setName(s);
    }
    
    @Override
    public String toString() {
        return "[Properties]:" + super.getDelegate().toString();
    }    

}
