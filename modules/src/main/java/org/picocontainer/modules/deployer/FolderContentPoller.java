package org.picocontainer.modules.deployer;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.picocontainer.Startable;

/**
 * Component that polls a folder for children at regular intervals.
 * 
 * @author Aslak Helles&oslash;y
 */
public final class FolderContentPoller implements Startable {
	private final FolderContentHandler folderContentHandler;
	private final FileObject folder;

	private final Runnable poller = new Runnable() {
		public void run() {
			while (!Thread.interrupted()) {
				try {
					// Have to "close" the folder to invalidate child cache
					folder.close();
					final FileObject[] currentChildren = folder.getChildren();
					folderContentHandler.setCurrentChildren(currentChildren);
					synchronized (FolderContentPoller.this) {
						FolderContentPoller.this.notify();
						FolderContentPoller.this.wait(2000);
					}
				} catch (final FileSystemException e) {
					e.printStackTrace();
				} catch (final InterruptedException e) {
					thread.interrupt();
				}
			}
		}
	};
	private Thread thread;
	private boolean started = false;

	public FolderContentPoller(final FolderContentHandler folderChangeNotifier) {
		this.folderContentHandler = folderChangeNotifier;
		folder = folderChangeNotifier.getFolder();
	}

	public void start() {
		if (started) {
			throw new IllegalStateException("Already started");
		}
		thread = new Thread(poller);
		thread.start();
		started = true;
	}

	public void stop() {
		if (!started) {
			throw new IllegalStateException("Already stopped");
		}
		thread.interrupt();
		started = true;
	}

}