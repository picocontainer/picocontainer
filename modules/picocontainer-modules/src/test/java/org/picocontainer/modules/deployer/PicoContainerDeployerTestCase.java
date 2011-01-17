package org.picocontainer.modules.deployer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import javax.script.ScriptEngineManager;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.impl.VFSClassLoader;
import org.apache.commons.vfs.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs.provider.zip.ZipFileProvider;
import org.junit.Test;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoContainer;
import org.picocontainer.classname.DefaultClassLoadingPicoContainer;
import org.picocontainer.script.ScriptedBuilderNameResolver;

/**
 * @author Aslak Helles&oslash;y
 */
@SuppressWarnings("restriction")
public final class PicoContainerDeployerTestCase {

    public static final String JAR_DIRECTORY = "target/deployer/apps";
    
    public static final String MODULE_DIRECTORY = "target/deployer/modules";

    @Test public void testZipWithDeploymentScriptAndClassesCanBeDeployed() throws FileSystemException, MalformedURLException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationArchive(manager, JAR_DIRECTORY + "/successful-deploy.jar");

        Deployer deployer = new PicoContainerDeployer(new DefaultModuleLayout(new PicoScriptingExtensionMapper(new ScriptedBuilderNameResolver())));
        PicoContainer pico = deployer.deploy(applicationArchive, getClass().getClassLoader(), null, null);
        Object zap = pico.getComponent("zap");
        assertEquals("Not started", zap.toString());
    }

    @Test public void testZipWithBadScriptNameThrowsFileSystemException() throws ClassNotFoundException, FileSystemException {

      DefaultFileSystemManager manager = new DefaultFileSystemManager();
      FileObject applicationFolder = getApplicationArchive(manager,  JAR_DIRECTORY + "/badscript-deploy.jar");

      try {
        Deployer deployer = new PicoContainerDeployer(new DefaultModuleLayout());
        PicoContainer pico = deployer.deploy(applicationFolder, getClass().getClassLoader(), null,null);
        fail("Deployment should have thrown FileSystemException for bad script file name.  Instead got:" + pico + " built.");
      }
      catch (DeploymentException ex) {
          assertNotNull(ex.getMessage());
      }
    }

    @Test public void testMalformedDeployerArchiveThrowsFileSystemException() throws ClassNotFoundException, FileSystemException {
      DefaultFileSystemManager manager = new DefaultFileSystemManager();
      FileObject applicationFolder = getApplicationArchive(manager,  JAR_DIRECTORY + "/malformed-deploy.jar");

      try {
        Deployer deployer = new PicoContainerDeployer(new DefaultModuleLayout());
        PicoContainer pico = deployer.deploy(applicationFolder, getClass().getClassLoader(), null,null);
        fail("Deployment should have thrown FileSystemException for badly formed archive. Instead got:" + pico + " built.");
      }
      catch (DeploymentException ex) {
          assertNotNull(ex.getMessage());
      }
    }

    @Test public void testFolderWithDeploymentScriptAndClassesCanBeDeployed() throws FileSystemException, MalformedURLException, ClassNotFoundException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationFolder = getApplicationFolder(manager, JAR_DIRECTORY + "/folder-test");

        try {
            Deployer deployer;
            deployer = new PicoContainerDeployer(new DefaultModuleLayout());
            PicoContainer pico = deployer.deploy(applicationFolder, getClass().getClassLoader(), null,null);
            Object zap = pico.getComponent("zap");
            assertEquals("Not started", zap.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Exploratory test that validates the concept of the VFS classloader
     */
    @Test public void testZapClassCanBeLoadedByVFSClassLoader() throws FileSystemException, MalformedURLException, ClassNotFoundException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationFolder = getApplicationFolder(manager, JAR_DIRECTORY + "/folder-test");
        assertTrue(applicationFolder.exists());
        
        ClassLoader moduleClassLoader = new VFSClassLoader(applicationFolder, manager, getClass().getClassLoader());
        assertNotNull(moduleClassLoader.getResourceAsStream("META-INF/composition.groovy"));
        assertNotNull(moduleClassLoader.getResourceAsStream("foo/bar/Zap.class"));
        moduleClassLoader.loadClass("foo.bar.Zap");
        
    }

    @Test public void testSettingDifferentBaseNameWillResultInChangeForWhatBuilderLooksFor() throws FileSystemException, MalformedURLException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationFolder = getApplicationFolder(manager, JAR_DIRECTORY + "/folder-test");

        DefaultModuleLayout layout = new DefaultModuleLayout(new PicoScriptingExtensionMapper(new ScriptedBuilderNameResolver()));
        layout.setFilebasename("foo");
        PicoContainerDeployer deployer = new PicoContainerDeployer(layout);


        try {
           PicoContainer pico  = deployer.deploy(applicationFolder, getClass().getClassLoader(), null, null);
            fail("Deployer should have now thrown an exception after changing the base name. Instead got: " + pico);
        }
        catch (DeploymentException ex) {
            assertNotNull(ex.getMessage());
        }

    }


    @Test public void testParentClassLoadersArePropertyPropagated() throws FileSystemException, MalformedURLException, ClassNotFoundException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationFolder = getApplicationFolder(manager, JAR_DIRECTORY + "/folder-test");
        
        PicoContainerDeployer deployer = new PicoContainerDeployer();
        FileObject badArchive = getApplicationArchive(manager, JAR_DIRECTORY + "/successful-deploy.jar");
        
        VFSClassLoader classLoader = new VFSClassLoader(new FileObject[] {badArchive}, manager, getClass().getClassLoader());
        deployer.deploy(applicationFolder, classLoader, null, null);

    }

    @Test public void testAssemblyScope() throws FileSystemException, MalformedURLException, ClassNotFoundException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationArchive(manager, JAR_DIRECTORY + "/successful-deploy.jar");

        Deployer deployer = new PicoContainerDeployer();

        PicoContainer pico = deployer.deploy(applicationArchive, getClass().getClassLoader(), null, "Test");
        assertEquals("Assembly Scope Test", pico.getComponent(String.class));
        assertNull(pico.getComponent("zap"));
    }
    
    
    @Test
    public void testUsingJavaScriptingEngine() throws Exception {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationArchive(manager, JAR_DIRECTORY + "/successful-deploy.jar");

        Deployer deployer = new PicoContainerDeployer(new DefaultModuleLayout(new JDKScriptingFileExtensionMapper(new ScriptEngineManager())));
        PicoContainer pico = deployer.deploy(applicationArchive, getClass().getClassLoader(), null, null);
        Object zap = pico.getComponent("zap");
        assertEquals("Not started", zap.toString());
    }
    
    @Test
    public void testJavascriptScriptingEngineAndSingleModule() throws Exception {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationFolder(manager, MODULE_DIRECTORY + "/org.picocontainer.testmodules.moduleTwo");

        Deployer deployer = new PicoContainerDeployer();

        MutablePicoContainer parent = new DefaultClassLoadingPicoContainer(new PicoBuilder()
        		.withCaching()
        		.withLifecycle()
        		.build());
        MutablePicoContainer pico = deployer.deploy(applicationArchive, getClass().getClassLoader(), parent, "Test");
        assertEquals("org.picocontainer.testmodules.moduleTwo.DefaultServiceTwo",
        		pico.getComponent("ServiceTwo").getClass().getName());    	
    }


    private FileObject getApplicationFolder(final DefaultFileSystemManager manager, String folderPath) throws FileSystemException, MalformedURLException {
        manager.setDefaultProvider(new DefaultLocalFileProvider());
        manager.addProvider("file", new DefaultLocalFileProvider());
        manager.init();
        File testapp = new File(folderPath);
        assertTrue(testapp.exists());
        return manager.toFileObject(testapp);
    }

    public static FileObject getApplicationArchive(final DefaultFileSystemManager manager, final String jarPath) throws FileSystemException {
    	if (manager.getSchemes().length == 0) {
		    manager.addProvider("file", new DefaultLocalFileProvider());
		    manager.addProvider("zip", new ZipFileProvider());
		    manager.init();
    	}
        File src = new File(jarPath);
        assertTrue(src.exists());
        return manager.resolveFile("zip:/" + src.getAbsolutePath());
    }
}
