/**
 * 
 */
package com.picocontainer.modules.deployer;

import com.picocontainer.modules.DeploymentException;

/**
 * Exception that indicates that the deployment folder/archive didn't have the
 * expected folders or files.
 * 
 * @author Michael Rimov, Centerline Computers, Inc.
 * 
 */
@SuppressWarnings("serial")
public class MalformedArchiveException extends DeploymentException {

	/**
	 * @param message
	 */
	public MalformedArchiveException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param t
	 */
	public MalformedArchiveException(final String message, final Throwable t) {
		super(message, t);
	}

}
