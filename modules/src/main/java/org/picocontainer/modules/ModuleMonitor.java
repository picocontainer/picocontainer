package org.picocontainer.modules;

import java.io.Serializable;

import org.apache.commons.vfs.FileObject;
import org.picocontainer.MutablePicoContainer;

/**
 * Monitor interface that receives events during deployment
 * @author Michael Rimov, Centerline Computers, Inc.
 *
 */
public interface ModuleMonitor extends Serializable {

	/**
	 * Indicates that no java class was found in the specific module.
	 * @param applicationFolder
	 * @param className
	 * @param moduleClassLoader
	 * @param e
	 */
	void noCompositionClassFound(FileObject applicationFolder,
			String className, ClassLoader moduleClassLoader,
			ClassNotFoundException e);

	/**
	 * Indicates that a composition class was found in the specified module, 
	 * but it is not the correct type.
	 * @param applicationFolder
	 * @param compositionClass
	 * @param moduleClassLoader
	 */
	void compositionClassNotCorrectType(FileObject applicationFolder,
			Class<?> compositionClass, ClassLoader moduleClassLoader);

	/**
	 * Generic event when an exception is thrown during deployment. 
	 * @param applicationFolder
	 * @param e
	 */
	void errorPerformingDeploy(FileObject applicationFolder, Throwable e);

	/**
	 * Indicates a deployment was successful.
	 * @param applicationFolder
	 * @param returnValue
	 * @param timeInMillis
	 */
	void deploySuccess(FileObject applicationFolder,
			MutablePicoContainer returnValue, long timeInMillis);
}
