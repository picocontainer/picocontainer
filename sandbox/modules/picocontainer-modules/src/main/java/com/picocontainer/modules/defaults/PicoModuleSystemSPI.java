/*******************************************************************************
 * Copyright (C)  PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.modules.defaults;

import com.picocontainer.modules.PicoModuleSystem;

/**
 * Labels interfaces used internally in the deployment process, not fit for normal
 * public consumption :)
 * @author Michael Rimov, Centerline Computers, Inc.
 *
 */
public interface PicoModuleSystemSPI extends PicoModuleSystem {
	public PicoModuleSystem deployModuleByName(String moduleName);

}
