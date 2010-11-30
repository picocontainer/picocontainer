package org.picocontainer.modules;

import java.io.Serializable;

import org.apache.commons.vfs.FileObject;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.script.util.MultiException;

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
	 * @param componentFolder
	 * @param compositionClass
	 * @param moduleClassLoader
	 */
	void compositionClassNotCorrectType(FileObject componentFolder,
			Class<?> compositionClass, ClassLoader moduleClassLoader);

	/**
	 * Generic event when an exception is thrown during deployment. 
	 * @param componentFolder
	 * @param e
	 */
	void errorPerformingDeploy(FileObject componentFolder, Throwable e);

	/**
	 * Indicates a deployment was successful.
	 * @param componentFolder
	 * @param returnValue
	 * @param timeInMillis
	 */
	void deploySuccess(FileObject componentFolder,
			MutablePicoContainer returnValue, long timeInMillis);

	void skippingDeploymentBecauseNotDeployable(FileObject allChildren);

	void startingMultiModuleDeployment(FileObject moduleDirectory);

	void multiModuleDeploymentFailed(FileObject moduleDirectory,
			MultiException errors);

	void multiModuleDeploymentSuccess(FileObject moduleDirectory,
			MutablePicoContainer returnValue, long deploymentTime);
}
