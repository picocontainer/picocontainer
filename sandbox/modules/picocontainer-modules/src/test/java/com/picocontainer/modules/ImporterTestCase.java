package com.picocontainer.modules;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.commons.vfs.VFS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.picocontainer.PicoContainer;
import com.picocontainer.modules.testing.InMemoryModuleFolder;
import com.picocontainer.testmodel.DependsOnTouchable;

public class ImporterTestCase {
	
	private PicoModuleSystem moduleSystem;
	
	private InMemoryModuleFolder moduleFolder;

	@Before
	public void setUp() throws Exception {
		moduleFolder = new InMemoryModuleFolder(VFS.getManager());
		
		moduleSystem  = new ModuleBuilder()
							.withAutoDeployFolder(moduleFolder.getModuleDirectory())
							.build();
		
	}

	@After
	public void tearDown() throws Exception {
		moduleSystem = null;
		moduleFolder.tearDown();
	}
	
	
	@Test
	public void testCrossModuleDependenciesArePossibleWithImports() {
		final String moduleA = "" 
				+ "import com.picocontainer.testmodel.*;\n" 
				+ "import com.picocontainer.modules.*;\n"
				+ "pico = parent.makeChildContainer();\n"
				+ "pico.addComponent(Touchable, SimpleTouchable);\n"
				+ "\n"
				+ "def publisher = new Publisher(pico, parent)\n"
				+ "publisher.publish(Touchable)";
		
		final String moduleB = "" 
			+ "import com.picocontainer.testmodel.*;\n" 
			+ "import com.picocontainer.modules.*;\n"
			+ "import static com.picocontainer.modules.Importer.importModule;\n"
			+ "pico = parent.makeChildContainer();\n"
			+ "\n"
			+ "importModule(\"moduleA\");\n"
			+ "\n"
			+ "pico.addComponent(DependsOnTouchable);\n"
			+ "\n"
			+ "def publisher = new Publisher(pico, parent)\n"
			+ "publisher.publish(DependsOnTouchable)";
		
		moduleFolder.addModule("moduleA", moduleA);
		moduleFolder.addModule("moduleB", moduleB);
		
		PicoContainer pico = moduleSystem.deploy().getPico();	
		DependsOnTouchable depends = pico.getComponent(DependsOnTouchable.class);
		assertNotNull(depends);
	}
	
	@Test
	public void testCircularDependenciesThrowsCircularDependencyException() {
		final String moduleA = "" 
			+ "import com.picocontainer.testmodel.*;\n" 
			+ "import com.picocontainer.modules.*;\n"
			+ "import static com.picocontainer.modules.Importer.importModule;\n"
			+ "\n"
			+ "importModule(\"moduleC\");\n"
			+ "\n"
			+ "pico = parent.makeChildContainer();\n"
			+ "pico.addComponent(Touchable, SimpleTouchable);\n"
			+ "\n"
			+ "def publisher = new Publisher(pico, parent)\n"
			+ "publisher.publish(Touchable)";
		
		final String moduleB = "" 
			+ "import com.picocontainer.testmodel.*;\n" 
			+ "import com.picocontainer.modules.*;\n"
			+ "import static com.picocontainer.modules.Importer.importModule;\n"
			+ "pico = parent.makeChildContainer();\n"
			+ "\n"
			+ "importModule(\"moduleA\");\n"
			+ "\n"
			+ "pico.addComponent(DependsOnTouchable);\n"
			+ "\n"
			+ "def publisher = new Publisher(pico, parent)\n"
			+ "publisher.publish(DependsOnTouchable)";
		
		final String moduleC = "" 
			+ "import com.picocontainer.testmodel.*;\n" 
			+ "import com.picocontainer.modules.*;\n"
			+ "import static com.picocontainer.modules.Importer.importModule;\n"
			+ "pico = parent.makeChildContainer();\n"
			+ "\n"
			+ "importModule(\"moduleB\");\n"
			+ "\n"
			+ "pico.addComponent(DependsOnTouchable);\n"
			+ "\n"
			+ "def publisher = new Publisher(pico, parent)\n"
			+ "publisher.publish(DependsOnTouchable)";
		
		moduleFolder.addModule("moduleA", moduleA);
		moduleFolder.addModule("moduleB", moduleB);
		moduleFolder.addModule("moduleC", moduleC);
		
		try {
			PicoContainer pico = moduleSystem.deploy().getPico();
			fail("Should have thrown CircularDependencyException, instead got: " + pico);
		} catch (CircularDependencyException e) {
			assertNotNull(e.getMessage());
		}	
	}

}
