package org.picocontainer.modules.deployer;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.modules.deployer.PicoScriptingExtensionMapper;
import org.picocontainer.script.ScriptedBuilderNameResolver;

public class PicoScriptingExtensionMapperTestCase {
	
	private PicoScriptingExtensionMapper mapper;

	@Before
	public void setUp() throws Exception {
		mapper = new PicoScriptingExtensionMapper(new ScriptedBuilderNameResolver());
	}

	@After
	public void tearDown() throws Exception {
		mapper = null;
	}
	
	@Test
	public void testIsExtensionAKnownScript() {
		assertFalse(mapper.isExtensionAKnownScript("coksjd"));
		assertTrue(mapper.isExtensionAKnownScript("js"));
		assertTrue(mapper.isExtensionAKnownScript("rb"));
		assertTrue(mapper.isExtensionAKnownScript("groovy"));
	}

	@Test
	public void testGetAllSupportedExtensions() {
		String result = mapper.getAllSupportedExtensions();
		assertNotNull(result);
		assertTrue("Got " + result, result.contains("js"));
		assertTrue("Got " + result, result.contains("groovy"));
		assertTrue("Got " + result, result.contains("xml"));
		assertTrue("Got " + result, result.contains("rb"));
	}
	

}
