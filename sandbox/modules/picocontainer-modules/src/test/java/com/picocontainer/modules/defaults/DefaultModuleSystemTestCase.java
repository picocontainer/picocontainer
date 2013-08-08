package com.picocontainer.modules.defaults;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import com.picocontainer.modules.ModuleBuilder;
import com.picocontainer.modules.ModuleMonitor;
import com.picocontainer.modules.PicoModuleSystem;
import com.picocontainer.modules.deployer.JDKScriptingFileExtensionMapper;
import com.picocontainer.modules.deployer.MalformedArchiveException;
import com.picocontainer.modules.monitor.commonslogging.CommonsLoggingModuleMonitor;
import com.picocontainer.modules.monitor.nullImpl.NullModuleMonitor;
import com.picocontainer.script.util.MultiException;

import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.PicoClassNotFoundException;
import com.picocontainer.PicoLifecycleException;

public class DefaultModuleSystemTestCase {
	
	private PicoModuleSystem moduleSystem;
	
	private Mockery context = new JUnit4Mockery();
	
	private ModuleMonitor monitor;
	

    public static final String JAR_DIRECTORY = "target/deployer/modules";	
    
    public static final String BAD_JAR_DIRECTORY = "target/deployer/apps";

	@Before
	public void setUp() throws Exception {
		monitor = context.mock(ModuleMonitor.class);	
		
		//We don't care about any deployer related messages.
		context.checking(new Expectations() {{
			allowing(monitor).compositionClassNotCorrectType(with(any(FileObject.class)), with(any(Class.class)), with(any(ClassLoader.class)));
			allowing(monitor).deploySuccess(with(any(FileObject.class)), with(any(MutablePicoContainer.class)), with(any(Long.TYPE)));
			allowing(monitor).errorPerformingDeploy(with(any(FileObject.class)), with(any(Throwable.class)));
			allowing(monitor).noCompositionClassFound(with(any(FileObject.class)), 
					with(any(String.class)), with(any(ClassLoader.class)),
							with(any(ClassNotFoundException.class)));
		}});
		File f = new File(JAR_DIRECTORY);
		if (!f.exists()) {
			throw new IllegalArgumentException("Cannot find directory " 
					+ f + " check your current working directory");
		}
		
		if (!f.isDirectory()) {
			throw new IllegalArgumentException(f + " does not appear to be a directory");
		}
		
		moduleSystem = new ModuleBuilder()
					.withParentClassLoader(DefaultModuleSystemTestCase.class.getClassLoader())
					.withMonitor(monitor)
					.withAutoDeployFolder(f)
					.build();
	}

	@After
	public void tearDown() throws Exception {
		monitor = null;
		moduleSystem = null;
	}

	@Test
	public void testDeploy() {
		
		context.checking(new Expectations() {{
			oneOf(monitor).startingMultiModuleDeployment(with(any(FileObject.class)));
			oneOf(monitor).multiModuleDeploymentSuccess(with(any(FileObject.class)), 
					with(any(MutablePicoContainer.class)), 
					with(any(Long.TYPE)));
		}});
		MutablePicoContainer pico = moduleSystem.deploy().getPico();
		assertNotNull(pico.getComponent("com.picocontainer.testmodules.moduleOne.ServiceOne"));
		assertEquals("com.picocontainer.testmodules.moduleOne.DefaultServiceOne", 
				pico.getComponent("com.picocontainer.testmodules.moduleOne.ServiceOne").getClass().getName());
		
		//Should be in child container
		assertNull(pico.getComponent("moduleOneTest"));
		
		assertNotNull(pico.getComponent("ServiceTwo"));
		assertEquals("com.picocontainer.testmodules.moduleTwo.DefaultServiceTwo", 
				pico.getComponent("ServiceTwo").getClass().getName());
	}
	
	@Test
	public void testDeployWithCustomPico() {
		context.checking(new Expectations() {{
			oneOf(monitor).startingMultiModuleDeployment(with(any(FileObject.class)));
			oneOf(monitor).multiModuleDeploymentSuccess(with(any(FileObject.class)), 
					with(any(MutablePicoContainer.class)), 
					with(any(Long.TYPE)));
		}});
		
		MutablePicoContainer customRoot = new PicoBuilder()
			.withCaching()
			.withJavaEE5Lifecycle()
			.build();
		
		MutablePicoContainer pico = moduleSystem.deploy(customRoot).getPico();
		assertTrue(customRoot == pico);
		assertTrue(pico.getLifecycleState().isStarted());
		
		assertNotNull(pico.getComponent("ServiceTwo"));
		assertEquals("com.picocontainer.testmodules.moduleTwo.DefaultServiceTwo", 
				pico.getComponent("ServiceTwo").getClass().getName());
	}

	@Test
	public void testUndeploy() {
		context.checking(new Expectations() {{
			oneOf(monitor).startingMultiModuleDeployment(with(any(FileObject.class)));
			oneOf(monitor).multiModuleDeploymentSuccess(with(any(FileObject.class)), 
					with(any(MutablePicoContainer.class)), 
					with(any(Long.TYPE)));
		}});

		MutablePicoContainer pico = moduleSystem.deploy().getPico();
		context.checking(new Expectations() {{
			oneOf(monitor).multiModuleUndeploymentBeginning(with(any(MutablePicoContainer.class)));
			oneOf(monitor).multiModuleUndeploymentSuccess(with(any(Long.TYPE)));
		}});
		moduleSystem.undeploy();
		assertTrue(pico.getLifecycleState().isDisposed());
	}
	
	@Test
	public void testMultipleUndeploysResultsInLifecycleErrors() {
		File f = new File(JAR_DIRECTORY);
		moduleSystem = new ModuleBuilder()
			.withParentClassLoader(DefaultModuleSystemTestCase.class.getClassLoader())
			.withMonitor(NullModuleMonitor.class)
			.withAutoDeployFolder(f)
			.build();		

		MutablePicoContainer pico = moduleSystem.deploy().getPico();
		assertNotNull(pico.getComponent("com.picocontainer.testmodules.moduleOne.ServiceOne"));
		assertNotNull(pico.getComponent("ServiceTwo"));
		
		moduleSystem.undeploy();
	
		try {
			moduleSystem.undeploy();
			fail("Second undeploy should have thrown IllegalStateException");
		} catch (IllegalStateException e) {
			assertNotNull(e.getMessage());
		}
	}
	
	@Test
	public void testMultipleDeploysWithoutUndeployResultsInIllegalStateException() {
		File f = new File(JAR_DIRECTORY);
		moduleSystem = new ModuleBuilder()
			.withParentClassLoader(DefaultModuleSystemTestCase.class.getClassLoader())
			.withMonitor(NullModuleMonitor.class)
			.withAutoDeployFolder(f)
			.build();		

		MutablePicoContainer pico = moduleSystem.deploy().getPico();
		assertNotNull(pico.getComponent("com.picocontainer.testmodules.moduleOne.ServiceOne"));

		try {
			pico = moduleSystem.deploy().getPico();
			fail("Should have thrown IllegalStateExcpetion, instead got " + pico);
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage().length() > 0);
		}
	}
	
	@Test
	public void testBadJarDirectoryGetsSeveralErrors() {
		File f= new File(BAD_JAR_DIRECTORY);
		moduleSystem = new ModuleBuilder()
			.withParentClassLoader(DefaultModuleSystemTestCase.class.getClassLoader())
			.withMonitor(CommonsLoggingModuleMonitor.class)
			.withAutoDeployFolder(f)
			.build();		

		final HashSet<Class> expectedErrors = new HashSet<Class>();
		expectedErrors.add(MalformedArchiveException.class);
		
		try {
			MutablePicoContainer pico = moduleSystem.deploy().getPico();
			fail("Should have thrown Multiple exceptions, instead got " + pico);
		} catch (MultiException ex) {
			assertTrue(ex.getErrorCount() > 0);
			HashSet<Class> receivedErrors = new HashSet<Class>();
			for (Throwable eachException : ex.getNestedExceptions()) {
				assertTrue(eachException.getMessage().length() > 0);
				receivedErrors.add(eachException.getClass());
			}
			
			StringWriter writer = new StringWriter();
			ex.printStackTrace(new PrintWriter(writer));
			expectedErrors.removeAll(receivedErrors);
			assertEquals("Unexpected Errors or Expected Errors not thrown " 
					+ Arrays.deepToString(expectedErrors.toArray()) + "\n Fill Stack Trace" + writer,
					0,expectedErrors.size());
		}

	}
	
	
	/**
	 * Does JDK scripting use a different Rhino version or something?  IT cannot
	 * handle Var-args.   Will revisit later.
	 */
	@Test
	@Ignore
	public void testIntegrationWithJDKScripting() {
		File f = new File(JAR_DIRECTORY);
		moduleSystem = new ModuleBuilder()
			.withAutoDeployFolder(f)
			.withFileExtensionMapper(JDKScriptingFileExtensionMapper.class)
			.build();
		
		MutablePicoContainer pico = moduleSystem.deploy().getPico();
		assertTrue(pico.getLifecycleState().isStarted());
		assertNotNull(pico.getComponent("com.picocontainer.testmodules.moduleOne.ServiceOne"));
		assertNotNull(pico.getComponent("ServiceTwo"));
		moduleSystem.undeploy();

	}
}
