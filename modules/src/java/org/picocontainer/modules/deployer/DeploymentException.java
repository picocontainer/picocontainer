package org.picocontainer.modules.deployer;

import org.picocontainer.PicoException;

/**
 *
 * Runtime Wrapper Exception for errors in deployment.
 *
 * @author Aslak Helles&oslash;y
 */
@SuppressWarnings("serial")
public class DeploymentException extends PicoException {

	public DeploymentException(String message) {
		super(message);
	}
	
	
    public DeploymentException(String message, Throwable t) {
        super(message,t);
    }

    public DeploymentException(Throwable t) {
        super(t);
    }
}
