package com.picocontainer.modules;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.picocontainer.modules.defaults.DefaultModuleSystem;
import com.picocontainer.modules.deployer.Deployer;
import com.picocontainer.modules.deployer.PicoContainerDeployer;
import com.picocontainer.modules.monitor.nullImpl.NullModuleMonitor;

@RunWith(JMock.class)
public class ModuleBuilderTestCase {
	
	private Mockery context = new JUnit4Mockery();
	
	public static class DependencyModuleSystem extends DefaultModuleSystem {

		public final ModuleMonitor monitor;
		public final Deployer deployer;
		public final FileObject moduleDirectory;
		public final ClassLoader classLoader;

		public DependencyModuleSystem(ModuleMonitor monitor, Deployer deployer,
				FileObject moduleDirectory, ClassLoader classLoader) {
			super(monitor, deployer, moduleDirectory, classLoader);
			this.classLoader = classLoader;
			this.moduleDirectory = moduleDirectory;
			this.monitor = monitor;
			this.deployer = deployer;
		}
		
	}

	@Test
	public void testSensibleDefaultsAllowsConstruction() {
		PicoModuleSystem moduleSystem = new ModuleBuilder()
				.withAutoDeployFolder(new File( System.getProperty("java.io.tmpdir")))
				.build();
		assertNotNull(moduleSystem);
	}
	
	@Test
	public void testSwapMonitor() {
		DependencyModuleSystem moduleSystem = (DependencyModuleSystem)new ModuleBuilder()
			.withModuleSystem(DependencyModuleSystem.class)
			.withMonitor(NullModuleMonitor.class)
			.withAutoDeployFolder(new File( System.getProperty("java.io.tmpdir")))
				.build();
		assertNotNull(moduleSystem);
		assertTrue(moduleSystem.monitor instanceof NullModuleMonitor);
	}
	
	@Test
	public void testSwapDeployer() throws FileSystemException {
		DependencyModuleSystem moduleSystem = (DependencyModuleSystem)new ModuleBuilder()
		.withModuleSystem(DependencyModuleSystem.class)
		.withDeployer(PicoContainerDeployer.class)
		.withAutoDeployFolder(new File( System.getProperty("java.io.tmpdir")))
			.build();

		assertNotNull(moduleSystem);
		assertTrue(moduleSystem.deployer instanceof PicoContainerDeployer);
		assertNotNull(moduleSystem.moduleDirectory);
	}
	
	@Test
	public void testSwapClassLoder() {
		ClassLoader parent = getClass().getClassLoader().getParent();
		DependencyModuleSystem moduleSystem = (DependencyModuleSystem)new ModuleBuilder()
		.withModuleSystem(DependencyModuleSystem.class)
		.withAutoDeployFolder(new File( System.getProperty("java.io.tmpdir")))
			.build();

		assertNotSame(parent, moduleSystem.classLoader);
		
		
		moduleSystem = (DependencyModuleSystem)new ModuleBuilder()
			.withModuleSystem(DependencyModuleSystem.class)
			.withParentClassLoader(parent)
			.withAutoDeployFolder(new File( System.getProperty("java.io.tmpdir")))
				.build();

		assertNotNull(moduleSystem);
		assertEquals(parent, moduleSystem.classLoader);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFileThatIsNotFolderThrowsImmediateException() throws IOException {
		File testFile = File.createTempFile("picocontainer", "temp");
		try {
			new ModuleBuilder().withAutoDeployFolder(testFile);		
		} finally {
			testFile.delete();
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testVFSFileThatIsNotFolderThrowsImmediateException() throws IOException {
		File testFile = File.createTempFile("picocontainer", "temp");
		FileObject fileObject = VFS.getManager().toFileObject(testFile);
		try {
			new ModuleBuilder().withAutoDeployFolder(fileObject);		
		} finally {
			testFile.delete();
		}
	}
	
	@Test
	public void testPhonyVFSErrorsGetProperlyThrown()  {
		final FileObject fileObject = context.mock(FileObject.class);
		final FileSystemException ex =  new FileSystemException("Danger Will Robinson");
		try {
			context.checking(new Expectations() {{
				oneOf(fileObject).getType();
				will(throwException(ex));
			}});
		} catch (FileSystemException e) {
			e.printStackTrace();
			fail("Got error setting up JMock");
		}
		
		try {
			new ModuleBuilder().withAutoDeployFolder(fileObject);
			fail("Should have thrown FileSystemRuntimeException");
		} catch (FileSystemRuntimeException e) {
			assertTrue(e.getCause() == ex);
			assertNotNull(e.getMessage());
		}
	}
	
}
