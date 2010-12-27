package org.picocontainer.modules;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.junit.Test;
import org.picocontainer.modules.defaults.DefaultModuleSystem;
import org.picocontainer.modules.deployer.Deployer;
import org.picocontainer.modules.deployer.PicoContainerDeployer;
import org.picocontainer.modules.monitor.nullImpl.NullModuleMonitor;

public class ModuleBuilderTestCase {
	
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
	
}
