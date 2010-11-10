package org.picocontainer.modules.deployer;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.picocontainer.script.ContainerBuilder;


public interface FileExtensionMapper {

	public abstract boolean isExtensionAKnownScript(String fileExtension);

	public abstract String getAllSupportedExtensions();
	
	public ContainerBuilder instantiateContainerBuilder(ClassLoader cl, FileObject script) throws FileSystemException;

}