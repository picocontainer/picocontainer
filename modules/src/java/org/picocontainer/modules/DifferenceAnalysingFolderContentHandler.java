package org.picocontainer.modules;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Aslak Helles&oslash;y
 */
public class DifferenceAnalysingFolderContentHandler implements FolderContentHandler {
    private final FileObject folder;
    private final FileSystemManager fileSystemManager;

    private FileObject[] lastChildren = new FileObject[0];

    public DifferenceAnalysingFolderContentHandler(FileObject folder, FileSystemManager fileSystemManager) {
        this.folder = folder;
        this.fileSystemManager = fileSystemManager;
    }

    public void setCurrentChildren(FileObject[] currentChildren) {
        List<FileObject> current = Arrays.asList(currentChildren);
        List<FileObject> last = Arrays.asList(lastChildren);

        fireFolderAddedMaybe(current, last);
        fireFolderRemovedMaybe(current, last);

        lastChildren = currentChildren;
    }

    private void fireFolderAddedMaybe(List<FileObject> current, List<FileObject> last) {
        current = new ArrayList<FileObject>(current);
        last = new ArrayList<FileObject>(last);

        current.removeAll(last);
        for (Object aCurrent : current) {
            FileObject fileObject = (FileObject)aCurrent;
            FileObject folderObject = convertToFolder(fileObject);
            if (folderObject != null && folderListener != null) {
                folderListener.folderAdded(folderObject);
            }
        }
    }

    private void fireFolderRemovedMaybe(List<FileObject> current, List<FileObject> last) {
        current = new ArrayList<FileObject>(current);
        last = new ArrayList<FileObject>(last);

        last.removeAll(current);
        for (Object aLast : last) {
            FileObject fileObject = (FileObject)aLast;
            FileObject folderObject = convertToFolder(fileObject);
            if (folderObject != null && folderListener != null) {
                folderListener.folderRemoved(fileObject);
            }
        }
    }

    private FileObject convertToFolder(FileObject fileObject) {
        FileObject result = null;
        try {
            if (fileObject.getType().equals(FileType.FOLDER)) {
                result = fileObject;
            } else if (fileObject.getType().equals(FileType.FILE)) {
                String extension = fileObject.getName().getExtension();
                if (extension.equals("zip") || extension.equals("jar")) {
                    String url = "zip:" + fileObject.getURL().getFile();
                    result = fileSystemManager.resolveFile(url);
                }
            }
        } catch (FileSystemException ignore) {
        }
        return result;
    }

    public FileObject getFolder() {
        return folder;
    }

    private FolderListener folderListener;

    public void addFolderListener(FolderListener folderListener) {
        if (this.folderListener != null) {
            throw new IllegalStateException(FolderListener.class.getName() + " already added");
        }
        this.folderListener = folderListener;
    }

    public void removeFolderListener(DeployingFolderListener deployingFolderListener) {
        this.folderListener = null;
    }
}