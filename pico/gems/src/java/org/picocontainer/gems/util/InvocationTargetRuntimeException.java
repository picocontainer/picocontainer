/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Centerline Computers, Inc.                               *
 *****************************************************************************/
package org.picocontainer.gems.util;

import org.picocontainer.PicoException;

/**
 * Runtime Exception version of
 * {@link java.lang.reflect.InvocationTargetException}.
 * 
 * @author Michael Rimov
 */
@SuppressWarnings("serial")
public class InvocationTargetRuntimeException extends PicoException {


	/**
	 * 
	 */
	public InvocationTargetRuntimeException() {
		super();
	}

	/**
	 * @param message
	 */
	public InvocationTargetRuntimeException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvocationTargetRuntimeException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvocationTargetRuntimeException(final String message,
			final Throwable cause) {
		super(message, cause);
	}

}
