package org.picocontainer.modules.deployer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.script.ScriptEngineManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class JDKScriptingFileExtensionMapperTestCase {

	private JDKScriptingFileExtensionMapper mapper = null;
	
	private ScriptEngineManager scriptManager;
	
	@Before
	public void setUp() throws Exception {
		scriptManager = new ScriptEngineManager();
		mapper = new JDKScriptingFileExtensionMapper(scriptManager);		
	}

	@After
	public void tearDown() throws Exception {
		scriptManager = null;
		mapper = null;
	}

	@Test
	public void testIsExtensionAKnownScript() {
		assertFalse(mapper.isExtensionAKnownScript("coksjd"));
		assertTrue(mapper.isExtensionAKnownScript("js"));
	}

	@Test
	public void testGetAllSupportedExtensions() {
		String result = mapper.getAllSupportedExtensions();
		assertNotNull(result);
		assertTrue("Got " + result, result.contains("js"));
		assertTrue("Got " + result, result.contains("groovy"));
	}

}
