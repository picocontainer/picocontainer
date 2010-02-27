/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file. 
 ******************************************************************************/
package org.picocontainer.script;

/**
 * Enumeration for lifecycle behaviors with the container builders.
 * @author Michael Rimov
 *
 */
public enum LifecycleMode {
	/**
	 * Uses standard lifecycle methods -- start is called when the
	 * container is started, and stop is called when the container
	 * is stopped and disposed.
	 */
	AUTO_LIFECYCLE,
	
	/**
	 * No start/stop methods are called when the containers are built/killed.
	 * Dispose, is called.  //TODO:  Dispose called? Proper or improper.
	 */
	NO_LIFECYCLE;
	
	/**
	 * Returns true if lifecycle methods should be called.
	 * @return true/false
	 */
	public boolean isInvokeLifecycle() {
		return (this.equals(AUTO_LIFECYCLE));
	}
}
