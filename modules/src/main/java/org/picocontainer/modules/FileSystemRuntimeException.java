package org.picocontainer.modules;

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
