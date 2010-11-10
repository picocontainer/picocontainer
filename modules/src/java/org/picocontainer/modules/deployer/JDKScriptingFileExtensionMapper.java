package org.picocontainer.modules.deployer;

import java.io.InputStreamReader;
import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import javax.script.ScriptEngineFactory;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.picocontainer.script.ContainerBuilder;
import org.picocontainer.script.JdkScriptingContainerBuilder;
import org.picocontainer.script.LifecycleMode;

@SuppressWarnings("restriction")
public class JDKScriptingFileExtensionMapper implements FileExtensionMapper {

	private final ScriptEngineManager mgr;

	public JDKScriptingFileExtensionMapper(ScriptEngineManager mgr) {
		this.mgr = mgr;		
	}
	
	public boolean isExtensionAKnownScript(String fileExtension) {
		if (fileExtension == null || fileExtension.length() == 0) {
			return false;
		}
		
		return (mgr.getEngineByExtension(fileExtension) != null);
	}
	
	public String getAllSupportedExtensions() {
		StringBuilder result = new StringBuilder();
		boolean needPipe = false;
		for (ScriptEngineFactory eachFactory : mgr.getEngineFactories()) {
			for (String eachExtension : eachFactory.getExtensions()) {
				if (needPipe) {
					result.append("|");
				} else {
					needPipe = true;
				}
				
				result.append(eachExtension);
			}
		}
		
		return result.toString();
	}

	public ContainerBuilder instantiateContainerBuilder(ClassLoader cl,
			FileObject script) throws FileSystemException {
        Reader scriptReader = new InputStreamReader(script.getContent().getInputStream());
        ScriptEngine engine = mgr.getEngineByExtension(script.getName().getExtension());
        return new JdkScriptingContainerBuilder(engine.getFactory().getNames().get(0), scriptReader, cl, LifecycleMode.AUTO_LIFECYCLE);
	}
}
