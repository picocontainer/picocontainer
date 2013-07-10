package com.picocontainer.web.providers;


public class ProviderSetupException extends RuntimeException {

	/**
	 * Serialization UUID.
	 */
	private static final long serialVersionUID = -1891332258261676356L;

	public ProviderSetupException(String message) {
		super(message);
	}

	public ProviderSetupException(Throwable cause) {
		super(cause);
	}

	public ProviderSetupException(String message, Throwable cause) {
		super(message, cause);
	}

}
