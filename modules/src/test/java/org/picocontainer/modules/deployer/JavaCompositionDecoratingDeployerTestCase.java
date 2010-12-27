package org.picocontainer.modules.deployer;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.impl.VFSClassLoader;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.PicoContainer;
import org.picocontainer.modules.ModuleMonitor;
import org.picocontainer.script.ScriptedBuilderNameResolver;

@RunWith(JMock.class)
public class JavaCompositionDecoratingDeployerTestCase {
	
	private Mockery context = new JUnit4Mockery();
	
	private ModuleMonitor moduleMonitor = null;
	
	@Before
	public void setUp() {
		moduleMonitor = context.mock(ModuleMonitor.class);
	}
	
	@After
	public void tearDown() {
		moduleMonitor = null;
	}
	
	@Test
	public void testHappyPathJavaDeploy() throws FileSystemException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = PicoContainerDeployerTestCase.getApplicationArchive(manager, PicoContainerDeployerTestCase.JAR_DIRECTORY + "/foo.bar.jar");

        Deployer deployer = new JavaCompositionDecoratingDeployer(new PicoContainerDeployer(new DefaultModuleLayout(new PicoScriptingExtensionMapper(new ScriptedBuilderNameResolver()))));
        PicoContainer pico = deployer.deploy(applicationArchive, getClass().getClassLoader(), null, null);
        Object zap = pico.getComponent("zap");
        assertNotNull(zap);
        assertEquals("Not started", zap.toString());
	}
	
	@Test
	public void testClassIsIncorrectTypeWillResultInException() throws FileSystemException {
		context.checking(new Expectations() {{
			oneOf(moduleMonitor).compositionClassNotCorrectType(with(any(FileObject.class)), with(any(
					//we have to not directly reference the Composition class since it gets removed
					//from the classpath during maven builds.
					Class.class
					)), with(any(VFSClassLoader.class)));
		}});
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = PicoContainerDeployerTestCase.getApplicationArchive(manager, PicoContainerDeployerTestCase.JAR_DIRECTORY + "/foo.bat.baddeploy.jar");

        Deployer deployer = new JavaCompositionDecoratingDeployer(
        		new PicoContainerDeployer(new DefaultModuleLayout(new PicoScriptingExtensionMapper(new ScriptedBuilderNameResolver())), moduleMonitor)
        	);
        PicoContainer pico;
		try {
			pico = deployer.deploy(applicationArchive, getClass().getClassLoader(), null, null);
			fail("Should have thrown MalformedArchiveException, instead got " + pico);
		} catch (MalformedArchiveException e) {
			assertNotNull(e.getMessage());
		}
	}
	
	@Test
	public void testClassNotPresentDelegatesToNormalScriptingContainer() throws FileSystemException {		
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        FileObject applicationArchive = PicoContainerDeployerTestCase.getApplicationArchive(manager, PicoContainerDeployerTestCase.JAR_DIRECTORY + "/successful-deploy.jar");

        Deployer deployer = new JavaCompositionDecoratingDeployer(new PicoContainerDeployer(new DefaultModuleLayout(new PicoScriptingExtensionMapper(new ScriptedBuilderNameResolver()))));
        PicoContainer pico = deployer.deploy(applicationArchive, getClass().getClassLoader(), null, null);
        Object zap = pico.getComponent("zap");
        assertEquals("Not started", zap.toString());
	}
	
}
