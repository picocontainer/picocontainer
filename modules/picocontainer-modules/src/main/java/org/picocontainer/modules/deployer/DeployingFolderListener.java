package org.picocontainer.modules.deployer;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.picocontainer.Startable;

/**
 * @author Aslak Helles&oslash;y
 */
public class DeployingFolderListener implements FolderListener, Startable {
	private final Deployer deployer;
	private final DifferenceAnalysingFolderContentHandler handler;

	public DeployingFolderListener(final Deployer deployer,
			final DifferenceAnalysingFolderContentHandler handler) {
		this.deployer = deployer;
		this.handler = handler;
	}

	public void folderAdded(final FileObject folder) {
		try {
			deployer.deploy(folder, getClass().getClassLoader(), null, null);
		} catch (final FileSystemException e) {
			throw new DeploymentException("Error deploying folder " + folder, e);
		}
	}

	public void folderRemoved(final FileObject fileObject) {

	}

	public void start() {
		handler.addFolderListener(this);
	}

	public void stop() {
		handler.removeFolderListener(this);
	}
}