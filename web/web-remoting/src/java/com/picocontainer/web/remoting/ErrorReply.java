/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.web.remoting;

public class ErrorReply {
	
    @SuppressWarnings("unused")
	private boolean ERROR = true;
	private String message;

    public ErrorReply(String message) {
        this.message = message;
    }

	public String getMessage() {
		return message;
	}

}
