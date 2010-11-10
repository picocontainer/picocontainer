package org.picocontainer.modules.deployer;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.modules.deployer.DifferenceAnalysingFolderContentHandler;
import org.picocontainer.modules.deployer.FolderListener;
import org.picocontainer.tck.MockFactory;

/**
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 */
@RunWith(JMock.class)
public class DifferenceAnalysingFolderContentHandlerTestCase {
	
	private Mockery mockery = MockFactory.mockeryWithCountingNamingScheme();
	
    @Test public void testAddedFoldersShouldCauseFolderAddedEvent() throws FileSystemException {
    	final FileObject folder = mockery.mock(FileObject.class);
        DifferenceAnalysingFolderContentHandler handler = new DifferenceAnalysingFolderContentHandler(folder, null);

        final FileObject addedFolder = mockery.mock(FileObject.class);
        mockery.checking(new Expectations(){{
        	one(addedFolder).getType();
        	will(returnValue(FileType.FOLDER));
        }});
    	

        final FolderListener folderListener = mockery.mock(FolderListener.class);
        mockery.checking(new Expectations(){{
        	one(folderListener).folderAdded(with(same(addedFolder)));
        }});

        handler.addFolderListener(folderListener);

        handler.setCurrentChildren(new FileObject[]{addedFolder});
    }

    @Test public void testRemovedFoldersShouldCauseFolderRemovedEvent() throws FileSystemException {
    	final FileObject folder = mockery.mock(FileObject.class);
        DifferenceAnalysingFolderContentHandler handler = new DifferenceAnalysingFolderContentHandler(folder, null);

        final FileObject initialFolderOne = mockery.mock(FileObject.class);
        mockery.checking(new Expectations(){{
        	one(initialFolderOne).getType();
        	will(returnValue(FileType.FOLDER));
        }});
        final FileObject initialFolderTwo = mockery.mock(FileObject.class);
        mockery.checking(new Expectations(){{
        	one(initialFolderTwo).getType();
        	will(returnValue(FileType.FOLDER));
        	one(initialFolderTwo).getType();
        	will(returnValue(FileType.FOLDER));
        }});
        FileObject[] initialFolders = new FileObject[] {initialFolderOne, initialFolderTwo};

        handler.setCurrentChildren(initialFolders);

        FileObject[] foldersAfterRemoval = new FileObject[] {initialFolderOne};
        
        final FolderListener folderListener = mockery.mock(FolderListener.class);
        mockery.checking(new Expectations(){{
        	one(folderListener).folderRemoved(with(same(initialFolderTwo)));
        }});

        handler.addFolderListener(folderListener);

        handler.setCurrentChildren(foldersAfterRemoval);
    }
}