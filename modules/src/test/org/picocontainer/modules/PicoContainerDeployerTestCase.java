package org.picocontainer.modules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.impl.VFSClassLoader;
import org.apache.commons.vfs.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs.provider.zip.ZipFileProvider;
import org.junit.Test;
import org.picocontainer.PicoContainer;

/**
 * @author Aslak Helles&oslash;y
 */
public final class PicoContainerDeployerTestCase {

    private final String jarsDir = "target/deployer/apps";
    private final String folderPath = "src/deploytest";

    @Test public void testZipWithDeploymentScriptAndClassesCanBeDeployed() throws FileSystemException, MalformedURLException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationArchive(manager, jarsDir + "/successful-deploy.jar");

        Deployer deployer = new PicoContainerDeployer(new DefaultModuleLayout("picocontainer"));
        PicoContainer pico = deployer.deploy(applicationArchive, getClass().getClassLoader(), null, null);
        Object zap = pico.getComponent("zap");
        assertEquals("Groovy Started", zap.toString());
    }

    @Test public void testZipWithBadScriptNameThrowsFileSystemException() throws ClassNotFoundException, FileSystemException {

      DefaultFileSystemManager manager = new DefaultFileSystemManager();
      FileObject applicationFolder = getApplicationArchive(manager,  jarsDir + "/badscript-deploy.jar");

      try {
        Deployer deployer = new PicoContainerDeployer(new DefaultModuleLayout("picocontainer"));
        PicoContainer pico = deployer.deploy(applicationFolder, getClass().getClassLoader(), null,null);
        fail("Deployment should have thrown FileSystemException for bad script file name.  Instead got:" + pico + " built.");
      }
      catch (FileSystemException ex) {
        //a-ok
      }
    }

    @Test public void testMalformedDeployerArchiveThrowsFileSystemException() throws ClassNotFoundException, FileSystemException {
      DefaultFileSystemManager manager = new DefaultFileSystemManager();
      FileObject applicationFolder = getApplicationArchive(manager,  jarsDir + "/malformed-deploy.jar");

      try {
        Deployer deployer = new PicoContainerDeployer(new DefaultModuleLayout("picocontainer"));
        PicoContainer pico = deployer.deploy(applicationFolder, getClass().getClassLoader(), null,null);
        fail("Deployment should have thrown FileSystemException for badly formed archive. Instead got:" + pico + " built.");
      }
      catch (FileSystemException ex) {
        //a-ok
      }
    }

    @Test public void testFolderWithDeploymentScriptAndClassesCanBeDeployed() throws FileSystemException, MalformedURLException, ClassNotFoundException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationFolder = getApplicationFolder(manager, folderPath);

        try {
            Deployer deployer;
            deployer = new PicoContainerDeployer(new DefaultModuleLayout("picocontainer"));
            PicoContainer pico = deployer.deploy(applicationFolder, getClass().getClassLoader(), null,null);
            Object zap = pico.getComponent("zap");
            assertEquals("Groovy Started", zap.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test public void testZapClassCanBeLoadedByVFSClassLoader() throws FileSystemException, MalformedURLException, ClassNotFoundException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationFolder = getApplicationFolder(manager, folderPath);
        ClassLoader applicationClassLoader = new VFSClassLoader(applicationFolder, manager, getClass().getClassLoader());
        applicationClassLoader.loadClass("foo.bar.Zap");
    }

    @Test public void testSettingDifferentBaseNameWillResultInChangeForWhatBuilderLooksFor() throws FileSystemException, MalformedURLException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationFolder = getApplicationFolder(manager, folderPath);
        PicoContainerDeployer deployer = new PicoContainerDeployer(new DefaultModuleLayout("foo"));


        try {
           PicoContainer pico  = deployer.deploy(applicationFolder, getClass().getClassLoader(), null, null);
            fail("Deployer should have now thrown an exception after changing the base name. Instead got: " + pico);
        }
        catch (FileSystemException ex) {
            //a-ok
        }

    }


    @Test public void testParentClassLoadersArePropertyPropagated() throws FileSystemException, MalformedURLException, ClassNotFoundException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationFolder = getApplicationFolder(manager, folderPath);
        PicoContainerDeployer deployer = new PicoContainerDeployer(new DefaultModuleLayout("picocontainer"));
        FileObject badArchive = getApplicationArchive(manager, jarsDir + "/successful-deploy.jar");
        VFSClassLoader classLoader = new VFSClassLoader(new FileObject[] {badArchive}, manager, getClass().getClassLoader());

        deployer.deploy(applicationFolder, classLoader, null, null);

    }

    @Test public void testAssemblyScope() throws FileSystemException, MalformedURLException, ClassNotFoundException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = getApplicationArchive(manager, jarsDir + "/successful-deploy.jar");

        Deployer deployer = new PicoContainerDeployer(new DefaultModuleLayout("picocontainer"));

        PicoContainer pico = deployer.deploy(applicationArchive, getClass().getClassLoader(), null, "Test");
        assertEquals("Assembly Scope Test", pico.getComponent(String.class));
        assertNull(pico.getComponent("zap"));
    }


    private FileObject getApplicationFolder(final DefaultFileSystemManager manager, String folderPath) throws FileSystemException, MalformedURLException {
        manager.setDefaultProvider(new DefaultLocalFileProvider());
        manager.init();
        File testapp = new File(folderPath);
        String url = testapp.toURL().toExternalForm();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return manager.resolveFile(url);
    }

    private FileObject getApplicationArchive(final DefaultFileSystemManager manager, final String jarPath) throws FileSystemException {
        manager.addProvider("file", new DefaultLocalFileProvider());
        manager.addProvider("zip", new ZipFileProvider());
        manager.init();
        File src = new File(jarPath);
        return manager.resolveFile("zip:/" + src.getAbsolutePath());
    }





}
