/**
 * 
 */
package org.picocontainer.modules.monitor.commonslogging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.modules.ModuleMonitor;
import org.picocontainer.modules.deployer.AbstractPicoComposer;
import org.picocontainer.script.util.MultiException;

/**
 * @author Mike
 * 
 */
@SuppressWarnings("serial")
public class CommonsLoggingModuleMonitor implements ModuleMonitor {

	private transient Log log = LogFactory.getLog(ModuleMonitor.class);

	public CommonsLoggingModuleMonitor() {
		// Default constructor for serialization.
	}

	/**
	 * Deserializes the monitor and reinitializes the logger.
	 * 
	 * @param s
	 *            the object input stream where the monitor data is stored.
	 * @throws java.io.IOException
	 *             upon error reading the default object state.
	 * @throws java.lang.ClassNotFoundException
	 *             if there is an error loading the obejct from the input stream
	 */
	private void readObject(final java.io.ObjectInputStream s)
			throws java.io.IOException, java.lang.ClassNotFoundException {
		s.defaultReadObject();
		log = LogFactory.getLog(ModuleMonitor.class);
	}

	/**
	 * Serializes the object to the output stream using default serialization.
	 * 
	 * @param s
	 *            the object output stream to utilize.
	 * @throws java.io.IOException
	 *             upon error performing serialization.
	 */
	private void writeObject(final java.io.ObjectOutputStream s)
			throws java.io.IOException {
		s.defaultWriteObject();
	}

	/** {@inheritDoc} **/
	public void noCompositionClassFound(final FileObject applicationFolder,
			final String className, final ClassLoader moduleClassLoader,
			final ClassNotFoundException e) {
		if (log.isInfoEnabled()) {
			final String folderName = getFolderName(applicationFolder);
			log.info("Did not find find class named '" + className
					+ "' inside archive '" + folderName
					+ "' using classloader '" + moduleClassLoader
					+ "'  Looking for composition script instead.");
			if (log.isDebugEnabled()) {
				log.debug("Full stack trace for class not found exception", e);
			}
		}
	}

	private String getFolderName(final FileObject applicationFolder) {
		try {
			if (applicationFolder == null) {
				return "[null]";
			}

			return applicationFolder.getName().getPathDecoded();
		} catch (final FileSystemException e) {
			e.printStackTrace();
			return "[Error retrieving file name]";
		}
	}

	/** {@inheritDoc} **/
	public void compositionClassNotCorrectType(
			final FileObject applicationFolder,
			final Class<?> compositionClass, final ClassLoader moduleClassLoader) {
		final String folderName = getFolderName(applicationFolder);
		log.error("Found class '" + compositionClass + "' inside archive '"
				+ folderName + "' using classloader '" + moduleClassLoader
				+ "'  However, it did not extends class '"
				+ AbstractPicoComposer.class.getName() + "'.");

	}

	/** {@inheritDoc} **/
	public void errorPerformingDeploy(final FileObject applicationFolder,
			final Throwable e) {
		final String folderName = getFolderName(applicationFolder);
		log.error("There was error performing deployment in archive '"
				+ folderName + "'", e);

	}

	/** {@inheritDoc} **/
	public void deploySuccess(final FileObject applicationFolder,
			final MutablePicoContainer returnValue, final long timeInMillis) {
		final String folderName = getFolderName(applicationFolder);
		if (log.isInfoEnabled()) {
			log.info("Successfully deployed archive " + folderName
					+ ".  Time for deployment " + timeInMillis + " m.s.");
		}

		if (log.isDebugEnabled()) {
			log.debug("Deployed Container: " + returnValue);
		}

	}

	public void skippingDeploymentBecauseNotDeployable(FileObject targetFile) {
		if (targetFile == null) {
			log.warn("Received skipping deployment because not deployable, but targetFile paramter was null!");
		} else {
			log.info("File " + targetFile + " has not been deployed because it is not a recognizable directory/archive");
		}
	}

	public void startingMultiModuleDeployment(FileObject moduleDirectory) {
		log.info("Beginning deployment of folder " + moduleDirectory);
	}

	public void multiModuleDeploymentFailed(FileObject moduleDirectory,
			MultiException errors) {
		log.error("Error performing deployment in folder " + moduleDirectory, errors);
		
	}

	public void multiModuleDeploymentSuccess(FileObject moduleDirectory,
			MutablePicoContainer returnValue, long deploymentTime) {
		log.info("Successfully deployed module folder " + moduleDirectory
				+ " resulting picocontainer: " 
				+ returnValue + "\n\tDeplyoment Time: " + deploymentTime + " m.s.");
	}

}
