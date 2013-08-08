package com.picocontainer.modules.testing;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.UUID;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import com.picocontainer.modules.FileSystemRuntimeException;

/**
 * Provides an easy way to test various composition scripts by creating an in-memory
 * module directory that only provides Composition scripts.
 * @author Michael Rimov, Centerline Computers, Inc.
 *
 */
public class InMemoryModuleFolder {

	
	private FileObject moduleDirectory;

	public InMemoryModuleFolder(FileSystemManager fileSystemManager) {
		try {
			FileObject fileObject = fileSystemManager.resolveFile("ram:/");
			moduleDirectory = fileObject.resolveFile(UUID.randomUUID().toString());
			moduleDirectory.createFolder();
		} catch (FileSystemException e) {
			throw new FileSystemRuntimeException("Error initiating RAM filesystem",e);
		}
	}
	
	public FileObject getModuleDirectory() {
		return moduleDirectory;
	}
	
	/**
	 * Creates a module by the given name and writes a 'composition.groovy' script
	 * into META-INF directory of the 'module'.
	 * @param moduleName the name of the module to create.
	 * @param compositionScript the groovy script to act as the module composition script.
	 */
	public void addModule(String moduleName, String compositionScript) {
		this.addModule(moduleName, compositionScript, "composition.groovy");
	}
	
	public void addModule(String moduleName, String compositionScript, String scriptFilename) {
		if (moduleName == null || moduleName.length() == 0) {
			throw new NullPointerException("moduleName");
		}
		
		if (compositionScript == null || compositionScript.length() == 0) {
			throw new NullPointerException("compositionScript");
		}
		
		if (scriptFilename == null || scriptFilename.length() == 0) {
			throw new NullPointerException("scriptFilename");
		}
		
		if (moduleDirectory == null) {
			throw new IllegalStateException("InMemoryModuleDirectory has already been torn down");
		}
		try {
			FileObject module = moduleDirectory.resolveFile(moduleName);
			module.createFolder();
			FileObject metaInf = module.resolveFile("META-INF");
			metaInf.createFolder();
			FileObject scriptFileObject = metaInf.resolveFile(scriptFilename);
			scriptFileObject.createFile();
			OutputStream os = scriptFileObject.getContent().getOutputStream();
			Writer writer = new OutputStreamWriter(os);
			writer.append(compositionScript);
			writer.close();
			os.close();			
		} catch (FileSystemException e) {
			throw new FileSystemRuntimeException("Error creating module '" + moduleName + "'.",e);
		} catch (IOException e) {
			throw new RuntimeException("Error writing composition script to module '" 
					+ moduleName + "'.",e);
		}
	}
	
	/**
	 * Run when closed down to make sure the RAM directory is cleared.
	 */
	public void tearDown() {
		if (moduleDirectory != null) {
			try {
				//Cleanup
				moduleDirectory.delete();
				moduleDirectory = null;
			} catch (FileSystemException e) {
				throw new FileSystemRuntimeException("Error deleting test module directory", e);
			}
		}
	}
	
	
}
