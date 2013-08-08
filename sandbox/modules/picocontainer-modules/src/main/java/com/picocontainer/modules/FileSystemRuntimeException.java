/*******************************************************************************
 * Copyright (C)  PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.modules;

import org.apache.commons.vfs.FileSystemException;

/**
 * Runtime Exception wrapper around VFS's checked FileSystemException
 * @author Micael Rimov, Centerline Computers
 *
 */
@SuppressWarnings("serial")
public class FileSystemRuntimeException extends RuntimeException {

	public FileSystemRuntimeException(String message, FileSystemException vfsException) {
		super(message, vfsException);
	}
}
