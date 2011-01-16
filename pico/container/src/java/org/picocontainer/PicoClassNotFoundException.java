/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer;

/**
 * Runtime Exception wrapper for a ClassNotFoundException.
 */
@SuppressWarnings("serial")
public class PicoClassNotFoundException extends PicoException {

    public PicoClassNotFoundException(final String className, final ClassNotFoundException cnfe) {
        super("Class '" + className + "' not found", cnfe);  
    }
}
