package org.picocontainer.modules;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.containers.TransientPicoContainer;
import org.picocontainer.modules.defaults.DefaultModuleSystem;
import org.picocontainer.modules.deployer.DefaultModuleLayout;
import org.picocontainer.modules.deployer.Deployer;
import org.picocontainer.modules.deployer.FileExtensionMapper;
import org.picocontainer.modules.deployer.ModuleLayout;
import org.picocontainer.modules.deployer.JavaCompositionDecoratingDeployer;
import org.picocontainer.modules.deployer.PicoScriptingExtensionMapper;
import  org.picocontainer.modules.monitor.commonslogging.CommonsLoggingModuleMonitor;
import org.picocontainer.script.ScriptedBuilderNameResolver;

public class ModuleBuilder {

	private ModuleLayout layout;
	private FileObject fileObject;
	private Class<? extends ModuleMonitor> monitor;
	private Class<? extends PicoModuleSystem> moduleClass = DefaultModuleSystem.class;
	private Class<? extends Deployer> deployerType = JavaCompositionDecoratingDeployer.class;
	private ClassLoader cl;
	private ModuleMonitor monitorInstance;
	private Class<? extends FileExtensionMapper> extensionMapperType  = PicoScriptingExtensionMapper.class;
	private ScriptedBuilderNameResolver nameBuilderResolver = new ScriptedBuilderNameResolver();
	
	public ModuleBuilder() {
		cl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>(){

			public ClassLoader run() {
				return Thread.currentThread().getContextClassLoader();
			}			
		});
		
		monitor = CommonsLoggingModuleMonitor.class;
		layout = null;
	}

	public ModuleBuilder withLayout(ModuleLayout layout) {
		this.layout = layout;
		return this;
	}
	
	public ModuleBuilder withAutoDeployFolder(FileObject fileObject) {
		try {
			if (!FileType.FOLDER.equals(fileObject.getType())) {
				throw new IllegalArgumentException("File object "+ fileObject + " must be of folder type");
			}
		} catch (FileSystemException e) {
			throw new FileSystemRuntimeException("Error querying file type of " + fileObject, e);
		}
		this.fileObject = fileObject;
		return this;		
	}
	
	public ModuleBuilder withAutoDeployFolder(File directory) {
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException(directory + " is not a folder");
		}
		
		try {
			return withAutoDeployFolder(VFS.getManager().toFileObject(directory));
		} catch (FileSystemException e) {
			throw new FileSystemRuntimeException("Fatal error transforming " + directory + " to file object",e);
		}
	}
	
	public ModuleBuilder withMonitor(Class<? extends ModuleMonitor> monitor) {
		this.monitor = monitor;
		return this;		
	}
	
	public ModuleBuilder withMonitor(ModuleMonitor monitorInstance) {
		this.monitorInstance = monitorInstance;
		return this;
	}
	
	public ModuleBuilder withModuleSystem(Class<? extends PicoModuleSystem> moduleClass) {
		this.moduleClass = moduleClass;
		return this;
	}
	
	public ModuleBuilder withParentClassLoader(ClassLoader cl) {
		this.cl = cl;
		return this;
	}
	
	public ModuleBuilder withDeployer(Class<? extends Deployer> deployerType) {		
		this.deployerType = deployerType;
		return this;
	}
	
	public ModuleBuilder withScriptedNameBuilderResolver(ScriptedBuilderNameResolver resolver) {
		if (resolver == null) {
			throw new NullPointerException("resolver");
		}
		
		this.nameBuilderResolver = resolver;
		return this;
	}
	
	public ModuleBuilder withFileExtensionMapper(Class<? extends FileExtensionMapper> fileExtensionMapper) {
		if (fileExtensionMapper == null) {
			throw new NullPointerException("fileExtensionMapper");
		}
		
		this.extensionMapperType = fileExtensionMapper;
		return this;
	}
	
	public PicoModuleSystem build() {
		if (fileObject == null) {
			throw new IllegalStateException("Must set autoDeployFolder before calling build()");
		}
		
		MutablePicoContainer pico = new TransientPicoContainer()
			.addComponent(Deployer.class, deployerType)
			.addComponent(PicoModuleSystem.class, moduleClass)
			.addComponent(FileObject.class, this.fileObject)
			.addComponent(ClassLoader.class, cl)
			.addComponent(ScriptedBuilderNameResolver.class, nameBuilderResolver)
			.addComponent(FileExtensionMapper.class, extensionMapperType);

		if (this.layout == null) {
			pico.addComponent(ModuleLayout.class, DefaultModuleLayout.class);
		} else {
			pico.addComponent(ModuleLayout.class, layout);
		}
		
		
		if (this.monitorInstance == null) {
			pico.addComponent(ModuleMonitor.class, monitor);
		} else {
			pico.addComponent(ModuleMonitor.class, monitorInstance);
		}
		
		return pico.getComponent(PicoModuleSystem.class);
	}
}
