/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.parameters;

import org.picocontainer.Parameter;

public abstract class AbstractParameter implements Parameter {
    
    /**
     * Target field/property/whatever name.  Used with named accessible objects such as fields and setters
     */
    private final String targetName;


	public String getTargetName() {
		return targetName;
	}    
    
	
	public AbstractParameter() {
		this(null);
	}
	
	public AbstractParameter(String targetName) {
		this.targetName = targetName;
	}
}
