/**
 *
 */
package org.picocontainer.script;

import org.picocontainer.PicoContainer;

/**
 * No-op post container build action.  Doesn't do anything
 * automatic to the container.
 * @author Paul Hammant
 * @author Michael Rimov, Centerline Computers, Inc.
 */
public class NoOpPostBuildContainerAction implements PostBuildContainerAction {

	/**
	 * No-op Implementation
	 * @param mpc Constructed container.
	 */
	public PicoContainer onNewContainer(final PicoContainer mpc) {
		//Does Nothing
		return mpc;
	}

}
