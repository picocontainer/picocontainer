package com.picocontainer.modules.deployer;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import com.picocontainer.script.ContainerBuilder;

/**
 * Adapts file names to Container builder.
 * 
 * @author Michael Rimov, Centerline Computers, Inc.
 */
public interface FileExtensionMapper {

	/**
	 * Given the file extension, returns true if the implementation's scripting
	 * system is supported.
	 * 
	 * @param fileExtension
	 *            file's extension.
	 * @return true if the file is supported.
	 */
	public abstract boolean isExtensionAKnownScript(String fileExtension);

	/**
	 * Retrieve a pipe delimited list of all file extensions supported by the
	 * system's ScriptEngineManager
	 * 
	 * @return pipe delimited list of supported extensions (without the period).
	 */
	public abstract String getAllSupportedExtensions();

	/**
	 * Instantiates the container builder for the specified FileObject.
	 * 
	 * @param cl
	 *            the classloader to use for the script execution.
	 * @param script
	 *            the FileObject that points to the script.
	 * @return ContainerBuilder instance
	 * @throws FileSystemException
	 */
	public ContainerBuilder instantiateContainerBuilder(ClassLoader cl,
			FileObject script) throws FileSystemException;

}