package org.picocontainer.modules.deployer;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.impl.VFSClassLoader;
import org.picocontainer.modules.DeploymentException;
import org.picocontainer.script.ScriptedBuilderNameResolver;

/**
 * For default layouts, The root folder to deploy must have the following file
 * structure:
 * 
 * <pre>
 * +-someapp/
 *   +-META-INF/
 *   | +-picocontainer.[py|js|xml|bsh]
 *   +-com/
 *     +-blablah/
 *       +-Hip.class
 *       +-Hop.class
 * </pre>
 * 
 * For those familiar with J2EE containers (or other containers for that
 * matter), the META-INF/picocontainer script is the
 * ScriptedContainerBuilderFactory <em>composition script</em>. It plays the
 * same role as more classical "deployment descriptors", except that deploying
 * via a full blown scripting language is a lot more powerful!
 * 
 * A new class loader (which will be a child of parentClassLoader) will be
 * created. This classloader will make the classes under the root folder
 * available to the deployment script.
 * 
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 * 
 */
public class DefaultModuleLayout implements ModuleLayout {

	private final FileExtensionMapper fileExtensionMapper;

	/**
	 * Directory in the archive where the composition scripts are located.
	 */
	private String scriptFolder = "/META-INF";

	/**
	 * Directory in the archive where additional jars to be added to the
	 * classpath are located. For example, in a web-app archive (.war), the
	 * library folder would be /WEB-INF/lib. May be null to indicate no library
	 * folder.
	 */
	private String libraryFolder = null;

	/**
	 * Directory where the classes are located. In a typical jar format, the
	 * value is &quot;/&quot;, in a webapp archive, it would be
	 * /WEB-INF/classes.
	 */
	private String classesFolder = "/";

	/**
	 * The name of the composition script minus the extension. Example:
	 * &quot;picocontainer&quot;
	 */
	private String filebasename = "composition";

	public DefaultModuleLayout() {
		this(
				new PicoScriptingExtensionMapper(
						new ScriptedBuilderNameResolver()));
	}

	/**
	 * 
	 * @param filebasename
	 *            the root portion of the composition script name. For example:
	 *            setting it to &quot;picocontainer&quot; would allow loading of
	 *            META-INF/picocontainer.groovy, META-INF/
	 */
	public DefaultModuleLayout(final FileExtensionMapper fileExtensionMapper) {
		this.fileExtensionMapper = fileExtensionMapper;

	}

	public FileObject getDeploymentScript(final FileObject applicationFolder)
			throws FileSystemException, DeploymentException {

		FileObject scriptFolder = null;
		try {
			scriptFolder = applicationFolder.resolveFile(makePath(
					applicationFolder.getName().getPathDecoded(),
					this.getScriptFolder()));
		} catch (final FileSystemException ex) {
			throw new MalformedArchiveException("Missing " + getScriptFolder()
					+ " folder in "
					+ applicationFolder.getName().getFriendlyURI(), ex);
		}

		if (!FileType.FOLDER.equals(scriptFolder.getType())) {
			throw new MalformedArchiveException(getScriptFolder() + " in "
					+ applicationFolder.getName().getFriendlyURI()
					+ " is a file, not a folder. " + scriptFolder.getType());
		}

		// Perform a find files that only checks the first level of depth
		final List<FileObject> picoContainerScripts = new ArrayList<FileObject>();
		for (final FileObject eachChild : scriptFolder.getChildren()) {
			if (eachChild.getName().getBaseName().startsWith(getFileBasename())) {
				picoContainerScripts.add(eachChild);
			}
		}

		if (picoContainerScripts == null || picoContainerScripts.size() < 1) {
			throw new MalformedArchiveException("No deployment script ("
					+ getFileBasename()
					+ ".["
					+ fileExtensionMapper.getAllSupportedExtensions()
					+ "]) in "
					+ makePath(applicationFolder.getName().getPathDecoded(),
							this.getScriptFolder()));
		}

		//
		// Iterate through the list of candidate scripts, making sure they are
		// known executables as scripts.
		//
		FileObject returnScript = null;
		for (final FileObject eachObject : picoContainerScripts) {
			if (fileExtensionMapper.isExtensionAKnownScript(eachObject
					.getName().getExtension())) {
				if (returnScript != null) {
					throw new MalformedArchiveException(
							"Found multiple possible deployment scripts in "
									+ applicationFolder.getName().getPath()
									+ getScriptFolder() + ".  \n\tFile 1:  '"
									+ returnScript.getName().getFriendlyURI()
									+ "'\n\t  File 2: "
									+ eachObject.getName().getFriendlyURI());
				}
				returnScript = eachObject;
			}
		}

		if (returnScript == null) {
			throw new MalformedArchiveException("No deployment script ("
					+ getFileBasename() + ".["
					+ fileExtensionMapper.getAllSupportedExtensions()
					+ "]) in " + applicationFolder.getName().getPathDecoded()
					+ getScriptFolder());
		}

		return returnScript;

	}

	public String getFileBasename() {
		return this.filebasename;
	}

	public ClassLoader constructModuleClassLoader(
			final ClassLoader parentClassLoader,
			final FileObject applicationFolder) throws FileSystemException {
		final List<FileObject> classPaths = new ArrayList<FileObject>();
		if (this.getClassesFolder() != null) {
			classPaths.add(applicationFolder.resolveFile(makePath(
					applicationFolder.getName().getPathDecoded(),
					getClassesFolder())));
		}

		if (this.getLibraryFolder() != null) {
			final FileObject libFolder = applicationFolder
					.resolveFile((makePath(applicationFolder.getName()
							.getPathDecoded(), this.getLibraryFolder())));
			for (final FileObject eachFile : libFolder.getChildren()) {
				final String extension = eachFile.getName().getExtension();
				if ("jar".equalsIgnoreCase(extension)
						|| "zip".equalsIgnoreCase(extension)) {
					classPaths.add(eachFile);
				}
			}
		}
		final VFSClassLoader cl = AccessController
				.doPrivileged(new PrivilegedAction<VFSClassLoader>() {
					public VFSClassLoader run() {
						try {
							return new VFSClassLoader(
									classPaths
											.toArray(new FileObject[classPaths
													.size()]),
									applicationFolder.getFileSystem()
											.getFileSystemManager(),
									parentClassLoader);
						} catch (final FileSystemException e) {
							throw new DeploymentException(
									"Error creating VFS classpath", e);
						}
					}

				});
		return cl;
	}

	public FileExtensionMapper getFileExtensionMapper() {
		return this.fileExtensionMapper;
	}

	public String getScriptFolder() {
		return scriptFolder;
	}

	/**
	 * Defines the folder for the composition script. Ex: /META-INF.
	 * <p>
	 * Default value is: <code>/META-INF</code>
	 * </p>
	 * 
	 * @param scriptFolder
	 * @return <code>this</code> to allow for method chaining.
	 */
	public DefaultModuleLayout setScriptFolder(final String scriptFolder) {
		this.scriptFolder = scriptFolder;
		return this;
	}

	public String getLibraryFolder() {
		return libraryFolder;
	}

	/**
	 * Defines the folder for the any private dependencies. Ex: /WEB-INF/lib.
	 * <p>
	 * Default value is: &lt;blank -- there is no library folder in the default
	 * archives.&gt;
	 * </p>
	 * 
	 * @param libraryFolder
	 * @return <code>this</code> to allow for method chaining.
	 */
	public DefaultModuleLayout setLibraryFolder(final String libraryFolder) {
		this.libraryFolder = libraryFolder;
		return this;
	}

	public String getClassesFolder() {
		return classesFolder;
	}

	/**
	 * Defines the folder for the classes to be loaded with this module. Ex:
	 * /WEB-INF/classes.
	 * <p>
	 * Default value is: '/'
	 * </p>
	 * 
	 * @param classesFolder
	 * @return <code>this</code> to allow for method chaining.
	 */
	public DefaultModuleLayout setClassesFolder(final String classesFolder) {
		this.classesFolder = classesFolder;
		return this;
	}

	public String getFilebasename() {
		return filebasename;
	}

	/**
	 * Defines the basic name of the composition file expected to be found in
	 * the archive. Ex: nanocontainer
	 * <p>
	 * Default value is: 'picocontainer'
	 * </p>
	 * 
	 * @param filebasename
	 *            The archive looks for any file that matches the pattern
	 *            <code>filebasename.*</code>, and then matches it to a script
	 *            engine.
	 * @return <code>this</code> to allow for method chaining.
	 */
	public DefaultModuleLayout setFilebasename(final String filebasename) {
		this.filebasename = filebasename;
		return this;
	}

	private String makePath(final String rootPath, final String path) {
		if (rootPath == null) {
			return path;
		}

		if ("/".equals(rootPath)) {
			return path;
		}

		if (rootPath.endsWith("/") && path.length() > 0) {
			return rootPath + path.substring(1);
		}

		return rootPath + path;
	}

}
