package com.picocontainer.modules.deployer;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.DefaultFileReplicator;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.impl.VFSClassLoader;
import org.apache.commons.vfs.provider.jar.JarFileProvider;
import org.apache.commons.vfs.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs.provider.url.UrlFileProvider;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class DefaultModuleLayoutTestCase {
    private static final String JAR_DIRECTORY = "target/deployer/apps";
    public static final String MODULE_DIRECTORY = "target/deployer/modules";

    private Mockery context = new JUnit4Mockery();
    
	@Test
	public void testDefaultClassLoader() throws FileSystemException, ClassNotFoundException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationArchive(manager, JAR_DIRECTORY + "/successful-deploy.jar");
        DefaultModuleLayout moduleLayout = new DefaultModuleLayout();
        VFSClassLoader cl = (VFSClassLoader) moduleLayout.constructModuleClassLoader(DefaultModuleLayoutTestCase.class.getClassLoader(), applicationArchive);
        assertNotNull(cl.getResourceAsStream("/META-INF/composition.groovy"));
        assertNotNull(cl.loadClass("foo.bar.Zap"));
        //Make sure things are wired right up the chain.
        assertNotNull(cl.loadClass("java.lang.Object"));
	}
	
	@Test
	public void testGetDeploymentScript() throws FileSystemException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationArchive(manager, JAR_DIRECTORY + "/successful-deploy.jar");
        DefaultModuleLayout moduleLayout = new DefaultModuleLayout();
        FileObject deployScript = moduleLayout.getDeploymentScript(applicationArchive);
        assertNotNull(deployScript);
        assertEquals("composition.groovy", deployScript.getName().getBaseName());
	}
	
	@Test(expected=MalformedArchiveException.class)	
	public void testMalformedDefaultPackageThrowsMalformedArchiveException() throws FileSystemException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationArchive(manager, JAR_DIRECTORY + "/malformed-deploy.jar");
        DefaultModuleLayout moduleLayout = new DefaultModuleLayout();
		FileObject deployScript = moduleLayout.getDeploymentScript(applicationArchive);
		if (deployScript != null) {
			fail("Expected exception and got " + deployScript);
		}
	}
		
	@Test
	public void testAlternateDeployScriptLocation() throws FileSystemException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationArchive(manager, JAR_DIRECTORY + "/malformed-deploy.jar");
        DefaultModuleLayout moduleLayout = new DefaultModuleLayout();
		moduleLayout.setScriptFolder("/deploy");
		FileObject deployScript = moduleLayout.getDeploymentScript(applicationArchive);
        assertNotNull(deployScript);
        assertEquals("composition.groovy", deployScript.getName().getBaseName());				
	}
	
	@Test
	public void testBogusScriptLocationGetsCaught() throws FileSystemException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationArchive(manager, JAR_DIRECTORY + "/malformed-deploy.jar");
        DefaultModuleLayout moduleLayout = new DefaultModuleLayout()
    	.setScriptFolder("/WEB-INF")
    	.setLibraryFolder("/WEB-INF/lib")
    	.setClassesFolder("/WEB-INF/classes")
    	.setFilebasename("picocontainer");
		try {
			FileObject deployScript = moduleLayout.getDeploymentScript(applicationArchive);
			fail("Should have thrown an exception, instead got " + deployScript);
		} catch (MalformedArchiveException e) {
			assertNotNull(e.getMessage());
		}
		
	}

	@Test
	public void testWebappStyleModuleDeployLocation() throws FileSystemException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationArchive(manager, JAR_DIRECTORY + "/alternate-layout.jar");
        DefaultModuleLayout moduleLayout = new DefaultModuleLayout()
        	.setScriptFolder("/WEB-INF")
        	.setLibraryFolder("/WEB-INF/lib")
        	.setClassesFolder("/WEB-INF/classes")
        	.setFilebasename("picocontainer");
		FileObject deployScript = moduleLayout.getDeploymentScript(applicationArchive);
        assertEquals("picocontainer.groovy", deployScript.getName().getBaseName());				
	}
	
	@Test
	public void testAlternateClasspathConstruction() throws FileSystemException, ClassNotFoundException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationArchive(manager, JAR_DIRECTORY + "/alternate-layout.jar");
        DefaultModuleLayout moduleLayout = new DefaultModuleLayout()
        	.setScriptFolder("/WEB-INF")
        	.setLibraryFolder("/WEB-INF/lib")
        	.setClassesFolder("/WEB-INF/classes")
        	.setFilebasename("picocontainer");
        
        ClassLoader loader = moduleLayout.constructModuleClassLoader(getClass().getClassLoader(), applicationArchive);
        assertNotNull(loader.loadClass("foo.bar.Zap"));
        assertNotNull(loader.loadClass("org.apache.commons.beanutils.BeanUtils"));
        assertNotNull(loader.loadClass("java.lang.Object"));
		
	}
	
	
	@Test
	public void testModuleAppClassLoader() throws FileSystemException, ClassNotFoundException, MalformedURLException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationFolder(manager, MODULE_DIRECTORY + "/com.picocontainer.testmodules.moduleTwo");
        DefaultModuleLayout moduleLayout = new DefaultModuleLayout();
        VFSClassLoader cl = (VFSClassLoader) moduleLayout
        	.constructModuleClassLoader(DefaultModuleLayoutTestCase.class.getClassLoader(), 
        			applicationArchive);
        assertEquals("com.picocontainer.testmodules.moduleTwo.DefaultServiceTwo",cl.loadClass("com.picocontainer.testmodules.moduleTwo.DefaultServiceTwo").getName());
	}
	
	@Test
	public void testFileSystemExceptionWhileLookingForDeploymentScriptTurnsIntoMalformedArchiveException() throws FileSystemException {
        final FileObject appFolder = context.mock(FileObject.class);
        final FileName fileName = context.mock(FileName.class);
        final FileSystemException ex = new FileSystemException("Testing!");
        context.checking(new Expectations() {{
        	oneOf(appFolder).resolveFile(with(any(String.class)));
        	will(throwException(ex));
        	
        	allowing(appFolder).getName();
        	will(returnValue(fileName));
        	
        	oneOf(fileName).getPathDecoded();
        	will(returnValue("/test"));
        	
        	oneOf(fileName).getFriendlyURI();
        	will(returnValue("/able/baker"));
        }});
		

        try {
			DefaultModuleLayout moduleLayout = new DefaultModuleLayout();
			moduleLayout.getDeploymentScript(appFolder);
		} catch (MalformedArchiveException e) {
			assertNotNull(e.getMessage());
			assertTrue(ex == e.getCause());
		}
		
	}
	
	/**
	 * To handle nested jars, the VFS manager has to have a few more things configured so
	 * it can replicate the nested jars.
	 * @param manager
	 * @param jarPath
	 * @return
	 * @throws FileSystemException
	 */
    private FileObject getApplicationArchive(final DefaultFileSystemManager manager, final String jarPath) throws FileSystemException {
        manager.setDefaultProvider(new UrlFileProvider());
        manager.addProvider("file", new DefaultLocalFileProvider());
        manager.addProvider("jar", new JarFileProvider());
        manager.addExtensionMap("jar", "jar");
        manager.setReplicator(new DefaultFileReplicator());
        manager.init();
        
        System.out.println("Custom File system schemes: " + Arrays.deepToString(manager.getSchemes()));
        File src = new File(jarPath);
        return manager.resolveFile("jar:/" + src.getAbsolutePath());
    }
	
    private FileObject getApplicationFolder(final DefaultFileSystemManager manager, String folderPath) throws FileSystemException, MalformedURLException {
        manager.setDefaultProvider(new DefaultLocalFileProvider());
        manager.addProvider("file", new DefaultLocalFileProvider());
        manager.init();
        File testapp = new File(folderPath);
        assertTrue(testapp.exists());
        return manager.toFileObject(testapp);
    }
    
}
