package org.picocontainer.modules.deployer;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.modules.ModuleMonitor;
import org.picocontainer.modules.monitor.commonslogging.CommonsLoggingModuleMonitor;
import org.picocontainer.script.ContainerBuilder;
import org.picocontainer.script.ScriptedBuilderNameResolver;

/**
 * This class is capable of deploying an application from any kind of file
 * system supported by <a
 * href="http://jakarta.apache.org/commons/sandbox/vfs/">Jakarta VFS</a>. (Like
 * local files, zip files etc.) - following the ScriptedContainerBuilderFactory
 * scripting model.
 * 
 * IMPORTANT NOTE: The scripting engine (rhino, jython, groovy etc.) should be
 * loaded by the same classLoader as the appliacation classes, i.e. the
 * VFSClassLoader pointing to the app directory.
 * 
 * <pre>
 *    +-------------------+
 *    | xxx               |  <-- parent app loader (must not contain classes from app builder classloader)
 *    +-------------------+
 *              |
 *    +-------------------+
 *    | someapp           | <-- app classloader (must not contain classes from app builder classloader)
 *    +-------------------+
 *              |
 *    +-------------------+
 *    | picocontainer     |
 *    | nanocontainer     |  <-- app builder classloader
 *    | rhino             |
 *    | jython            |
 *    | groovy            |
 *    +-------------------+
 * </pre>
 * 
 * This means that these scripting engines should *not* be accessible by any of
 * the app classloader, since this may prevent the scripting engine from seeing
 * the classes loaded by the VFSClassLoader. In other words, the scripting
 * engine classed may be loaded several times by different class loaders - once
 * for each deployed application.
 * 
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 */
public class PicoContainerDeployer implements Deployer {
	/**
	 * Module definition to be deployed
	 */
	private final ModuleLayout moduleLayout;
	private final ModuleMonitor monitor;

	/**
	 * Default constructor that makes sensible defaults.
	 * 
	 * @throws FileSystemException
	 */
	public PicoContainerDeployer() throws FileSystemException {
		this(new DefaultModuleLayout(new PicoScriptingExtensionMapper(
				new ScriptedBuilderNameResolver())));
	}

	/**
	 * Constructs a picocontainer deployer with the specified file system
	 * manager and specifies a 'base name' for the configuration file that will
	 * be loaded.
	 * 
	 * @param fileSystemManager
	 *            A VFS FileSystemManager.
	 * @todo Deprecate this and replace 'base file name' with the concept of a
	 *       ArchiveLayout that defines where jars are stored, where the
	 *       composition script is stored, etc.
	 * @param baseFileName
	 */
	public PicoContainerDeployer(final ModuleLayout moduleLayout) {
		this(moduleLayout, new CommonsLoggingModuleMonitor());
	}

	public PicoContainerDeployer(final ModuleLayout moduleLayout,
			final ModuleMonitor monitor) {
		this.moduleLayout = moduleLayout;
		this.monitor = monitor;
	}

	/** {@inheritDoc} **/
	public MutablePicoContainer deploy(final FileObject applicationFolder,
			final ClassLoader parentClassLoader,
			final MutablePicoContainer parentContainer,
			final Object assemblyScope) throws FileSystemException {

		final FileObject deploymentScript = moduleLayout
				.getDeploymentScript(applicationFolder);

		final ClassLoader applicationClassLoader = moduleLayout
				.constructModuleClassLoader(parentClassLoader,
						applicationFolder);
		final ContainerBuilder builder = moduleLayout.getFileExtensionMapper()
				.instantiateContainerBuilder(applicationClassLoader,
						deploymentScript);

		try {
			return (MutablePicoContainer) builder.buildContainer(
					parentContainer, assemblyScope, true);
		} catch (final RuntimeException e) {
			getMonitor().errorPerformingDeploy(applicationFolder, e);
			throw e;
		}
	}

	/** {@inheritDoc} **/
	public ModuleLayout getModuleLayout() {
		return this.moduleLayout;
	}

	/** {@inheritDoc} **/
	public ModuleMonitor getMonitor() {
		return this.monitor;
	}

}
