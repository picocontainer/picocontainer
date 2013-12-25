/*****************************************************************************
 * Copyright (C) 2003-2013 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Serban Iordache                                          *
 *****************************************************************************/
package com.picocontainer.gems.containers;

import com.picocontainer.PicoException;

public class PicoJsonException extends PicoException {
	private static final long serialVersionUID = 1L;

	public PicoJsonException() {
		super();
	}

	public PicoJsonException(String message, Throwable cause) {
		super(message, cause);
	}

	public PicoJsonException(String message) {
		super(message);
	}

	public PicoJsonException(Throwable cause) {
		super(cause);
	}
}