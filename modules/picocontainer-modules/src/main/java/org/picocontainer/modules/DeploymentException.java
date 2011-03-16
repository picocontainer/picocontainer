package org.picocontainer.modules;

import org.picocontainer.PicoException;

/**
 * 
 * Runtime Wrapper Exception for errors in deployment.
 * 
 * @author Aslak Helles&oslash;y
 */
@SuppressWarnings("serial")
public class DeploymentException extends PicoException {

	public DeploymentException(final String message) {
		super(message);
	}

	public DeploymentException(final String message, final Throwable t) {
		super(message, t);
	}
}
