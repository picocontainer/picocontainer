/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.references;

import java.io.Serializable;

import org.picocontainer.ObjectReference;

/**
 * Simple instance implementation of ObjectReference. 
 * 
 * @author Aslak Helles&oslash;y
 * @author Konstantin Pribluda
 */
@SuppressWarnings("serial")
public class SimpleReference<T> implements ObjectReference<T>,
		Serializable {
	private T instance;

	public SimpleReference() {
	    // no-op
	}

	public T get() {
		return instance;
	}

	public void set(T item) {
		this.instance = item;
	}
}
