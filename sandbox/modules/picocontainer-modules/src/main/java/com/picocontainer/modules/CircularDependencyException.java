package com.picocontainer.modules;

/**
 * Thrown when you get a circular dependency among module imports:
 *  Module A imports Module B which imports Module A.
 * @author Michael Rimov, Centerline Computers, Inc.
 */
@SuppressWarnings("serial")
public class CircularDependencyException extends RuntimeException {

	public CircularDependencyException(String message, Throwable cause) {
		super(message, cause);
	}

	public CircularDependencyException(String message) {
		super(message);
	}

}
