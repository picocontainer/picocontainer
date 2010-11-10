/**
 * 
 */
package org.picocontainer.modules.deployer;

import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.picocontainer.script.ContainerBuilder;
import org.picocontainer.script.ScriptedBuilderNameResolver;
import org.picocontainer.script.ScriptedContainerBuilderFactory;
import org.picocontainer.script.UnsupportedScriptTypeException;

/**
 * @author Mike
 *
 */
public class NanoDeployScriptExtensionMapper implements FileExtensionMapper {
	
	private final ScriptedBuilderNameResolver nanoNameResolver;


	public NanoDeployScriptExtensionMapper(ScriptedBuilderNameResolver nanoNameResolver) {
		this.nanoNameResolver = nanoNameResolver;		
	}

	public boolean isExtensionAKnownScript(String fileExtension) {
		try {
			nanoNameResolver.getBuilderClassName("." + fileExtension);
			return true;
		} catch (UnsupportedScriptTypeException e) {
			return false;
		}
	}

	public String getAllSupportedExtensions() {
		StringBuilder builder = new StringBuilder();
		boolean needPipe = false;
		for (String eachExtension : nanoNameResolver.getAllSupportedExtensions()) {
			if (needPipe) {
				builder.append("|");				
			} else {
				needPipe = true;
			}
			builder.append(eachExtension);
		}
		return builder.toString();
	}

	public ContainerBuilder instantiateContainerBuilder(ClassLoader cl,
			FileObject script) throws FileSystemException {
        Reader scriptReader = new InputStreamReader(script.getContent().getInputStream());
        String builderClassName;
        try {
            builderClassName = nanoNameResolver.getBuilderClassName("." + script.getName().getExtension());
        } catch (UnsupportedScriptTypeException ex) {
            throw new FileSystemException("Could not find a suitable builder for: " + script.getName()
                + ".  Known extensions are: [groovy|bsh|js|py|xml]", ex);
        }


        ScriptedContainerBuilderFactory scriptedContainerBuilderFactory = new ScriptedContainerBuilderFactory(scriptReader, builderClassName, cl);
        ContainerBuilder builder = scriptedContainerBuilderFactory.getContainerBuilder();
        return builder;
	}

}
