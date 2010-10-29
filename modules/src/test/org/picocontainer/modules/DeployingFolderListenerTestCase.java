package org.picocontainer.modules;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.ObjectReference;
import org.picocontainer.tck.MockFactory;

/**
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 */
@RunWith(JMock.class)
public class DeployingFolderListenerTestCase {
	
	private Mockery mockery = MockFactory.mockeryWithCountingNamingScheme();
	
    @Test public void testFolderAddedShouldDeployApplication() throws FileSystemException {
    	final FileObject folder = mockery.mock(FileObject.class);

    	final Deployer deployer = mockery.mock(Deployer.class);
        mockery.checking(new Expectations(){{
        	one(deployer).deploy(with(same(folder)), with(any(ClassLoader.class)), with(any(MutablePicoContainer.class)), with(aNull(Object.class)));
        	will(returnValue(null));
        }});
        
        DifferenceAnalysingFolderContentHandler handler = null;
        DeployingFolderListener deployingFolderListener = new DeployingFolderListener(deployer, handler);

        deployingFolderListener.folderAdded(folder);
    }

}