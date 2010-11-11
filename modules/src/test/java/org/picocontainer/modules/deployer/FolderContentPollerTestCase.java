package org.picocontainer.modules.deployer;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.modules.deployer.FolderContentHandler;
import org.picocontainer.modules.deployer.FolderContentPoller;
import org.picocontainer.tck.MockFactory;

/**
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 */
@RunWith(JMock.class)
public class FolderContentPollerTestCase {

	private Mockery mockery = MockFactory.mockeryWithCountingNamingScheme();
	
    @Test public void testShouldPollForNewFoldersAtRegularIntervals() throws InterruptedException, FileSystemException {
    	final FileObject rootFolder = mockery.mock(FileObject.class, "rootFolder");
        final FileObject[] noChildren = new FileObject[0];

        // Adding a child that will be returned at the second invocation of getChildren
    	final FileObject newChildFolder = mockery.mock(FileObject.class, "childFolder");
        final FileObject[] newChildren = new FileObject[] {newChildFolder};

        final FolderContentHandler folderContentHandler = mockery.mock(FolderContentHandler.class, "folderContentHandlerMock");

        mockery.checking(new Expectations(){{
        	one(rootFolder).close();
        	one(rootFolder).getChildren();
        	will(returnValue(noChildren));
        	one(folderContentHandler).getFolder();
        	will(returnValue(rootFolder));
        	one(folderContentHandler).setCurrentChildren(with(same(noChildren)));
        }});
        FolderContentPoller fileMonitor = new FolderContentPoller(folderContentHandler);

        fileMonitor.start();
        synchronized(fileMonitor) {
        	fileMonitor.wait(200);
        }

        mockery.checking(new Expectations(){{
        	one(rootFolder).close();
        	one(rootFolder).getChildren();
        	will(returnValue(newChildren));
        	one(folderContentHandler).setCurrentChildren(with(same(newChildren)));
        }});
       
        synchronized(fileMonitor) {
            fileMonitor.notify();
            fileMonitor.wait(200);
        }
        fileMonitor.stop();
    }
}