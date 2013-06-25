package com.picocontainer.script;

import com.picocontainer.PicoContainer;

/**
 * Allows you to perform specific actions on a PicoContainer once it
 * has been constructed.
 * @author Paul Hammant
 * @author Michael Rimov, Centerline Computers, Inc.
 * @since PicoContainer 3.0
 */
public interface PostBuildContainerAction {

	/**
	 * Callback method called once a new container is started.
	 * @param container newly constructed and populated PicoContainer.
	 * @return usually the container passed in, but it allows for final decoration
	 * of the container
	 */
	PicoContainer onNewContainer(PicoContainer container);

}
