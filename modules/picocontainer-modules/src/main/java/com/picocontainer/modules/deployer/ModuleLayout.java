package com.picocontainer.modules.deployer;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import com.picocontainer.modules.DeploymentException;

/**
 * There are many possible ways to define a module layout. Will it look more
 * like a jar file, a war file, or a ear file? This interface should take care
 * of the differences.
 * 
 * @author Michael Rimov
 * @todo it would be really nice to be able to have a module layout as a
 *       declarative definition rather than a class since a lot of the behavior
 *       gets copied between layout-to-layout
 */
public interface ModuleLayout {

	/**
	 * Retrieves the deployment script from the specified module.
	 * 
	 * @param applicationFolder
	 *            the root file object for the module being deployed
	 * @param mgr
	 * @return FileObject representing the script to manage.
	 * @throws FileSystemException
	 */
	FileObject getDeploymentScript(FileObject applicationFolder)
			throws FileSystemException, DeploymentException;

	/**
	 * Retrieve a classloader for the module
	 * 
	 * @param parentClassLoader
	 *            the parent classloader
	 * @param applicationFolder
	 *            the root file object for the module being deployed
	 * @return a constructed classloader.
	 * @throws FileSystemException
	 */
	ClassLoader constructModuleClassLoader(ClassLoader parentClassLoader,
			FileObject applicationFolder) throws FileSystemException;

	/**
	 * Retrieve the file extension mapper.
	 * 
	 * @return
	 */
	FileExtensionMapper getFileExtensionMapper();

	/**
	 * Retrieve the expected &quot;base name&quot; for scripts. For example, if
	 * the base name is &quot;picocontainer&quot;, then the deployment script
	 * will be (for example): &quot;picocontainer.js&quot;, or
	 * &quot;picocontainer.groovy&quot;
	 * 
	 * @return the layout's file base name.
	 */
	String getFileBasename();

}
