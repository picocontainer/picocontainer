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

	public AbstractParameter() {

	}

	/**
	 * @todo REMOVE ME
	 *
	 * @return
	 */
	public String getTargetName() {
		return null;
	}

}
