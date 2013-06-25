/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.references;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.picocontainer.ObjectReference;

/**
 * Gets and sets references on Thread Local
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class ThreadLocalReference<T> extends ThreadLocal<T> implements ObjectReference<T>, Serializable {

    private void writeObject(final ObjectOutputStream out) {
        if(out != null)
		 {
			; // eliminate warning because of unused parameter
		}
    }

    private void readObject(final ObjectInputStream in) {
        if(in != null)
		 {
			; // eliminate warning because of unused parameter
		}
    }

}
