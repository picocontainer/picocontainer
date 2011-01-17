package org.picocontainer.modules.deployer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;

/**
 * @author Aslak Helles&oslash;y
 */
public class DifferenceAnalysingFolderContentHandler implements
		FolderContentHandler {
	private final FileObject folder;
	private final FileSystemManager fileSystemManager;

	private FileObject[] lastChildren = new FileObject[0];

	public DifferenceAnalysingFolderContentHandler(final FileObject folder,
			final FileSystemManager fileSystemManager) {
		this.folder = folder;
		this.fileSystemManager = fileSystemManager;
	}

	public void setCurrentChildren(final FileObject[] currentChildren) {
		final List<FileObject> current = Arrays.asList(currentChildren);
		final List<FileObject> last = Arrays.asList(lastChildren);

		fireFolderAddedMaybe(current, last);
		fireFolderRemovedMaybe(current, last);

		lastChildren = currentChildren;
	}

	private void fireFolderAddedMaybe(List<FileObject> current,
			List<FileObject> last) {
		current = new ArrayList<FileObject>(current);
		last = new ArrayList<FileObject>(last);

		current.removeAll(last);
		for (final Object aCurrent : current) {
			final FileObject fileObject = (FileObject) aCurrent;
			final FileObject folderObject = convertToFolder(fileObject);
			if (folderObject != null && folderListener != null) {
				folderListener.folderAdded(folderObject);
			}
		}
	}

	private void fireFolderRemovedMaybe(List<FileObject> current,
			List<FileObject> last) {
		current = new ArrayList<FileObject>(current);
		last = new ArrayList<FileObject>(last);

		last.removeAll(current);
		for (final Object aLast : last) {
			final FileObject fileObject = (FileObject) aLast;
			final FileObject folderObject = convertToFolder(fileObject);
			if (folderObject != null && folderListener != null) {
				folderListener.folderRemoved(fileObject);
			}
		}
	}

	private FileObject convertToFolder(final FileObject fileObject) {
		FileObject result = null;
		try {
			if (fileObject.getType().equals(FileType.FOLDER)) {
				result = fileObject;
			} else if (fileObject.getType().equals(FileType.FILE)) {
				final String extension = fileObject.getName().getExtension();
				if (extension.equals("zip") || extension.equals("jar")) {
					final String url = "zip:" + fileObject.getURL().getFile();
					result = fileSystemManager.resolveFile(url);
				}
			}
		} catch (final FileSystemException ignore) {
		}
		return result;
	}

	public FileObject getFolder() {
		return folder;
	}

	private FolderListener folderListener;

	public void addFolderListener(final FolderListener folderListener) {
		if (this.folderListener != null) {
			throw new IllegalStateException(FolderListener.class.getName()
					+ " already added");
		}
		this.folderListener = folderListener;
	}

	public void removeFolderListener(
			final DeployingFolderListener deployingFolderListener) {
		this.folderListener = null;
	}
}