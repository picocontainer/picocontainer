package org.picocontainer.modules.deployer;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileReplicator;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.impl.VFSClassLoader;
import org.apache.commons.vfs.impl.VirtualFileProvider;
import org.apache.commons.vfs.provider.jar.JarFileProvider;
import org.apache.commons.vfs.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs.provider.ram.RamFileProvider;
import org.apache.commons.vfs.provider.res.ResourceFileProvider;
import org.apache.commons.vfs.provider.temp.TemporaryFileProvider;
import org.apache.commons.vfs.provider.url.UrlFileProvider;
import org.apache.commons.vfs.provider.zip.ZipFileProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.modules.deployer.DefaultModuleLayout;
import org.picocontainer.modules.deployer.MalformedArchiveException;

public class DefaultModuleLayoutTestCase {
    private final String jarsDir = "target/deployer/apps";
    private final String folderPath = "src/deploytest";

	@Test
	public void testDefaultClassLoader() throws FileSystemException, ClassNotFoundException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationArchive(manager, jarsDir + "/successful-deploy.jar");
        DefaultModuleLayout moduleLayout = new DefaultModuleLayout();
        VFSClassLoader cl = (VFSClassLoader) moduleLayout.constructModuleClassLoader(DefaultModuleLayoutTestCase.class.getClassLoader(), applicationArchive);
        assertNotNull(cl.getResourceAsStream("/META-INF/picocontainer.groovy"));
        assertNotNull(cl.loadClass("foo.bar.Zap"));
        //Make sure things are wired right up the chain.
        assertNotNull(cl.loadClass("java.lang.Object"));
	}
	
	@Test
	public void testGetDeploymentScript() throws FileSystemException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationArchive(manager, jarsDir + "/successful-deploy.jar");
        DefaultModuleLayout moduleLayout = new DefaultModuleLayout();
        FileObject deployScript = moduleLayout.getDeploymentScript(applicationArchive);
        assertNotNull(deployScript);
        assertEquals("picocontainer.groovy", deployScript.getName().getBaseName());
	}
	
	@Test(expected=MalformedArchiveException.class)	
	public void testMalformedDefaultPackageThrowsMalformedArchiveException() throws FileSystemException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationArchive(manager, jarsDir + "/malformed-deploy.jar");
        DefaultModuleLayout moduleLayout = new DefaultModuleLayout();
		FileObject deployScript = moduleLayout.getDeploymentScript(applicationArchive);
		if (deployScript != null) {
			fail("Expected exception and got " + deployScript);
		}
	}
		
	@Test
	public void testAlternateDeployScriptLocation() throws FileSystemException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationArchive(manager, jarsDir + "/malformed-deploy.jar");
        DefaultModuleLayout moduleLayout = new DefaultModuleLayout();
		moduleLayout.setScriptFolder("/deploy");
		FileObject deployScript = moduleLayout.getDeploymentScript(applicationArchive);
        assertNotNull(deployScript);
        assertEquals("picocontainer.groovy", deployScript.getName().getBaseName());				
	}
	
	@Test
	public void testBogusScriptLocationGetsCaught() throws FileSystemException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationArchive(manager, jarsDir + "/malformed-deploy.jar");
        DefaultModuleLayout moduleLayout = new DefaultModuleLayout()
    	.setScriptFolder("/WEB-INF")
    	.setLibraryFolder("/WEB-INF/lib")
    	.setClassesFolder("/WEB-INF/classes")
    	.setFilebasename("composition");
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
        FileObject applicationArchive = getApplicationArchive(manager, jarsDir + "/alternate-layout.jar");
        DefaultModuleLayout moduleLayout = new DefaultModuleLayout()
        	.setScriptFolder("/WEB-INF")
        	.setLibraryFolder("/WEB-INF/lib")
        	.setClassesFolder("/WEB-INF/classes")
        	.setFilebasename("picocontainer");
		FileObject deployScript = moduleLayout.getDeploymentScript(applicationArchive);
        assertEquals("composition.groovy", deployScript.getName().getBaseName());				
	}
	
	@Test
	public void testAlternateClasspathConstruction() throws FileSystemException, ClassNotFoundException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationArchive(manager, jarsDir + "/alternate-layout.jar");
        DefaultModuleLayout moduleLayout = new DefaultModuleLayout()
        	.setScriptFolder("/WEB-INF")
        	.setLibraryFolder("/WEB-INF/lib")
        	.setClassesFolder("/WEB-INF/classes")
        	.setFilebasename("composition");
        
        ClassLoader loader = moduleLayout.constructModuleClassLoader(getClass().getClassLoader(), applicationArchive);
        assertNotNull(loader.loadClass("foo.bar.Zap"));
        assertNotNull(loader.loadClass("org.apache.commons.beanutils.BeanUtils"));
        assertNotNull(loader.loadClass("java.lang.Object"));
		
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
	
}
