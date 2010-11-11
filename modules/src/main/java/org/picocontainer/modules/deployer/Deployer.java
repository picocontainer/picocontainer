package org.picocontainer.modules.deployer;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.modules.ModuleMonitor;

/**
 * A deployer provides a method of loading some sort of &quot;archive&quot; with
 * a soft-configuration system. The archive can be compressed zips, remote urls,
 * or standard file folders.
 * <p>
 * It uses Apache Commons VFS for a unified resource model, but the actual
 * format of the 'archive' will depend on the implementation of the deployer.
 * See
 * {@link org.picocontainer.modules.deployer.PicoContainerDeployer.deployer.NanoContainerDeployer}
 * for the default file format used.
 * </p>
 * <p>
 * Typically, the archive is deployed in its own unique VFS-based classloader to
 * provide independence of these archives. For those following development of
 * the PicoContainer world, a deployer can be considered a bit of a
 * mini-microcontainer.
 * </p>
 * 
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 */
public interface Deployer {

	/**
	 * Deploys some sort of application folder. As far as NanoContainer
	 * deployment goes, there is a null assembly scope associated with this
	 * method, and
	 * 
	 * @param applicationFolder
	 *            FileObject the base class of the 'archive'. By archive, the
	 *            format depends on the deployer instance, and it may even apply
	 *            to things such remote URLs. Must use Apache VFS
	 * @param parentClassLoader
	 *            The parent classloader to attach this container to.
	 * @param parentContainerRef
	 *            ObjectReference the parent container object reference.
	 * @param assemblyScope
	 *            the assembly scope to use. This can be any object desired,
	 *            (null is allowed) and when coupled with things like NanoWAR,
	 *            it allows conditional assembly of different components in the
	 *            script.
	 * @return ObjectReference a new object reference that container the new
	 *         container.
	 * @throws FileSystemException
	 *             upon VFS-based errors.
	 */
	MutablePicoContainer deploy(FileObject applicationFolder,
			ClassLoader parentClassLoader,
			MutablePicoContainer parentContainer, Object assemblyScope)
			throws FileSystemException;

	/**
	 * Retrieve the module layout (usually defined in the constructor).
	 * 
	 * @return
	 */
	ModuleLayout getModuleLayout();

	/**
	 * Retrieve the monitor used for the deployment process
	 * 
	 * @return
	 */
	ModuleMonitor getMonitor();

}
