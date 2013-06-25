package com.picocontainer.modules.testing;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InMemoryModuleFolderTestCase {
	
	private InMemoryModuleFolder moduleFolder;

	@Before
	public void setUp() throws Exception {
		moduleFolder = new InMemoryModuleFolder(VFS.getManager());
	}

	@After
	public void tearDown() throws Exception {
		moduleFolder.tearDown();
	}
	
	@Test
	public void testConstruction() throws IOException {
		final String scriptOne = "println('This is a test')";
		moduleFolder.addModule("moduleA", scriptOne );
		moduleFolder.addModule("moduleB", "println('This is a test too')");
		
		FileObject fileObject = moduleFolder.getModuleDirectory();
		assertTrue(fileObject.exists());
		FileObject moduleA = fileObject.resolveFile("moduleA");
		assertTrue(moduleA.exists());
		assertTrue(moduleA.getType() == FileType.FOLDER);
		
		FileObject metaINF = moduleA.resolveFile("META-INF");
		assertTrue(metaINF.exists());
		assertTrue(metaINF.getType() == FileType.FOLDER);
		
		FileObject composition = metaINF.resolveFile("composition.groovy");
		assertTrue(composition.exists());
		assertTrue(composition.getType() == FileType.FILE);
		InputStream is = composition.getContent().getInputStream();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int data;
		while (( data = is.read()) > 0) {
			bos.write(data);
		}
		String result = new String(bos.toByteArray());
		assertEquals(scriptOne, result);

		FileObject moduleB = fileObject.resolveFile("moduleB");
		assertTrue(moduleB.exists());
		assertTrue(moduleB.getType() == FileType.FOLDER);

	
	}
	
	@Test
	public void testTearDownWillCauseIllegalStateExceptionIfAddModuleCalledLater() {
		moduleFolder.tearDown();
		assertNull(moduleFolder.getModuleDirectory());
		try {
			moduleFolder.addModule("something", "//something else");
			fail("Should have thrown IllegalStateException");
		} catch (IllegalStateException e) {
			assertNotNull(e.getMessage());
		}
	}
	
	@Test
	public void testAddModuleWithBadArgumentsNPEsWithProperArgumentName() {
		try {
			moduleFolder.addModule(null,null, null);
		} catch (NullPointerException e) {
			assertEquals("moduleName", e.getMessage());
		}

		try {
			moduleFolder.addModule("",null, null);
		} catch (NullPointerException e) {
			assertEquals("moduleName", e.getMessage());
		}
		
		try {
			moduleFolder.addModule("something",null, null);
		} catch (NullPointerException e) {
			assertEquals("compositionScript", e.getMessage());
		}

		try {
			moduleFolder.addModule("something", "", null);
		} catch (NullPointerException e) {
			assertEquals("compositionScript", e.getMessage());
		}		

		try {
			moduleFolder.addModule("something","something else", null);
		} catch (NullPointerException e) {
			assertEquals("scriptFilename", e.getMessage());
		}

		try {
			moduleFolder.addModule("something", "something else", "");
		} catch (NullPointerException e) {
			assertEquals("scriptFilename", e.getMessage());
		}		
	
	}

}
