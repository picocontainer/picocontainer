/**
 * 
 */
package org.picocontainer.modules.deployer;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.modules.ModuleMonitor;

/**
 * Extends the typical
 * {@link org.picocontainer.modules.deployer.PicoContainerDeployer
 * PicoContainerDeployer} by using pure Java classes for module composition. The
 * advantages of this over using traditional Groovy/Ruby/Javascript/etc
 * scripting are:
 * <ul>
 * <li>Faster Deployment -- setup of JRuby and Groovy takes significant amount
 * of time.</li>
 * <li>IDE's catch rename refactoring. A rename/move refactoring can easily be
 * propagated by modern IDE's.</li>
 * <li>Compile time checking. Let the IDE make sure the composition script is
 * correct, rather than waiting for the module system start up.</li>
 * </ul>
 * <h2>Naming Convention</h2>
 * <p>
 * So how does the class figure out what class to load for composition? The name
 * the class attempts to load is
 * [deploy_filename_without_extension].Composition.
 * </p>
 * <p>
 * Example:<br/>
 * Module name is: <code>org.example.test.jar</code><br/>
 * Class that will attempt to be loaded is
 * <code>org.example.test.Composition</code>
 * </p>
 * <p>
 * If you need more fine grained control over the composition classname, then
 * you may use {@linkplain #setCompositionClassName(String)
 * setCompositionClassName(String)} to specify the composition class name.
 * </p>
 * 
 * @author Michael Rimov, Centerline Computers
 */
public class JavaCompositionDecoratingDeployer implements Deployer {

	private final Deployer delegate;

	private String compositionClassName;

	/**
	 * Allow people to configure autostart behavior similar to scripting
	 * behavior.
	 */
	private boolean autoStart = true;

	/**
	 * Constructs a java composition-based deployer that wraps a backup deployer
	 * if there is no delegate involved.
	 * 
	 * @param delegate
	 */
	public JavaCompositionDecoratingDeployer(final Deployer delegate) {
		this.delegate = delegate;
	}

	/** {@inheritDoc} **/
	public MutablePicoContainer deploy(final FileObject applicationFolder,
			final ClassLoader parentClassLoader,
			final MutablePicoContainer parentContainer,
			final Object assemblyScope) throws FileSystemException {

		final ClassLoader moduleClassLoader = getModuleLayout()
				.constructModuleClassLoader(parentClassLoader,
						applicationFolder);
		final MutablePicoContainer returnresult = attemptJavaClassDeploy(
				applicationFolder, moduleClassLoader, parentContainer,
				assemblyScope);
		if (returnresult != null) {
			return returnresult;
		}
		return delegate.deploy(applicationFolder, parentClassLoader,
				parentContainer, assemblyScope);
	}

	/**
	 * Attempts to construct a concrete PicoComposer implementation and run it.
	 * 
	 * @param applicationFolder
	 *            the deployment module being queried
	 * @param moduleClassLoader
	 *            classloader for the deployment module
	 * @param parentContainer
	 *            a parent picocontainer (may be null)
	 * @param assemblyScope
	 *            may be null
	 * @return constructed picocontainer.
	 * @throws FileSystemException
	 */
	private MutablePicoContainer attemptJavaClassDeploy(
			final FileObject applicationFolder,
			final ClassLoader moduleClassLoader,
			final MutablePicoContainer parentContainer,
			final Object assemblyScope) throws FileSystemException {
		final long startTime = System.currentTimeMillis();
		final String className = constructExpectedClassName(applicationFolder);
		try {
			final Class<?> compositionClass = moduleClassLoader
					.loadClass(className);
			if (!AbstractPicoComposer.class.isAssignableFrom(compositionClass)) {
				getMonitor().compositionClassNotCorrectType(applicationFolder,
						compositionClass, moduleClassLoader);
				return null;
			}

			final AbstractPicoComposer composer = (AbstractPicoComposer) compositionClass
					.newInstance();
			final MutablePicoContainer returnValue = composer.createContainer(
					parentContainer, moduleClassLoader, assemblyScope);

			if (isAutoStart()) {
				returnValue.start();
			}

			getMonitor().deploySuccess(applicationFolder, returnValue,
					System.currentTimeMillis() - startTime);
			return returnValue;
		} catch (final ClassNotFoundException e) {
			this.getMonitor().noCompositionClassFound(applicationFolder,
					className, moduleClassLoader, e);
		} catch (final InstantiationException e) {
			getMonitor().errorPerformingDeploy(applicationFolder, e);
		} catch (final IllegalAccessException e) {
			getMonitor().errorPerformingDeploy(applicationFolder, e);
		}
		// Failure path
		return null;
	}

	/**
	 * Constrcuts the expected full name of the composition class. The name is
	 * derived by convention
	 * 
	 * @param applicationFolder
	 * @return
	 * @throws FileSystemException
	 */
	private String constructExpectedClassName(final FileObject applicationFolder)
			throws FileSystemException {
		String baseName;
		String extension = null;
		if (this.compositionClassName != null) {
			return compositionClassName;
		}

		if (applicationFolder.getFileSystem().getParentLayer() == null) {
			// If in raw directory.
			baseName = applicationFolder.getName().getRoot().getBaseName();
			extension = applicationFolder.getName().getExtension();
		} else {
			// If in zip files
			final FileObject parentObject = applicationFolder.getFileSystem()
					.getParentLayer();
			baseName = parentObject.getName().getBaseName();
			extension = parentObject.getName().getExtension();
		}
		String expectedName = baseName.substring(0, baseName.length()
				- extension.length() - 1);

		final String moduleName = getModuleLayout().getFileBasename();

		expectedName = expectedName + "."
				+ Character.toUpperCase(moduleName.charAt(0))
				+ moduleName.substring(1);
		return expectedName;

	}

	/** {@inheritDoc} **/
	public ModuleLayout getModuleLayout() {
		return delegate.getModuleLayout();
	}

	/** {@inheritDoc} **/
	public ModuleMonitor getMonitor() {
		return delegate.getMonitor();
	}

	public String getCompositionClassName() {
		return compositionClassName;
	}

	/**
	 * Allows you to specify a specific class name to use as the composition
	 * class.
	 * 
	 * @param compositionClassName
	 * @return <code>this</code> to allow for method chaining.
	 */
	public JavaCompositionDecoratingDeployer setCompositionClassName(
			final String compositionClassName) {
		this.compositionClassName = compositionClassName;
		return this;
	}

	public boolean isAutoStart() {
		return autoStart;
	}

	/**
	 * Allows you to turn off automatic starting of the container once it is
	 * constructed.
	 * 
	 * @param autoStart
	 * @return <code>this</code> to allow for method chaining.
	 */
	public JavaCompositionDecoratingDeployer setAutoStart(
			final boolean autoStart) {
		this.autoStart = autoStart;
		return this;
	}

}
