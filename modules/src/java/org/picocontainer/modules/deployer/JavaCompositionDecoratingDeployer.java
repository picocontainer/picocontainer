/**
 * 
 */
package org.picocontainer.modules.deployer;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.modules.adapter.ModuleMonitor;

/**
 * @author Mike
 *
 */
public class JavaCompositionDecoratingDeployer implements Deployer {
	
	private final Deployer delegate;
	
	private String compositionClassName;

	public JavaCompositionDecoratingDeployer(Deployer delegate) {
		this.delegate = delegate;
	}


	public MutablePicoContainer deploy(FileObject applicationFolder,
			ClassLoader parentClassLoader,
			MutablePicoContainer parentContainer, Object assemblyScope)
			throws FileSystemException {

		ClassLoader moduleClassLoader = getModuleLayout().constructModuleClassLoader(parentClassLoader, applicationFolder); 
		MutablePicoContainer returnresult = attemptJavaClassDeploy(applicationFolder, parentClassLoader, parentContainer, assemblyScope) ; 
		if ( returnresult != null ) {
			return returnresult;
		}
		return delegate.deploy(applicationFolder, parentClassLoader, parentContainer, assemblyScope);
	}


	/**
	 * Attempts to construct a concrete PicoComposer implementation and run it.
	 * @param applicationFolder
	 * @param moduleClassLoader
	 * @param parentContainer
	 * @param assemblyScope
	 * @return
	 */
	private MutablePicoContainer attemptJavaClassDeploy(FileObject applicationFolder,
			ClassLoader moduleClassLoader,			
			MutablePicoContainer parentContainer, Object assemblyScope) {
		long startTime = System.currentTimeMillis();
		String className = constructExpectedClassName(applicationFolder);
		try {
			Class<?> compositionClass = moduleClassLoader.loadClass(className);
			if (!AbstractPicoComposer.class.isAssignableFrom(compositionClass)) {
				getMonitor().compositionClassNotCorrectType(applicationFolder, compositionClass, moduleClassLoader);
				return null;
			}

			AbstractPicoComposer composer = (AbstractPicoComposer) compositionClass.newInstance();
			MutablePicoContainer returnValue = composer.createContainer(parentContainer, moduleClassLoader, assemblyScope);
			
			getMonitor().deploySuccess(applicationFolder, returnValue, System.currentTimeMillis() - startTime);
			return returnValue;			
		} catch (ClassNotFoundException e) {
			this.getMonitor().noCompositionClassFound(applicationFolder, className, moduleClassLoader, e);
		} catch (InstantiationException e) {
			getMonitor().errorPerformingDeploy(applicationFolder, e);
		} catch (IllegalAccessException e) {
			getMonitor().errorPerformingDeploy(applicationFolder, e);
		}
		//Failure path
		return null;
	}

	/**
	 * Constrcuts the expected full name of the composition class.  The name is derived by convention 
	 * @param applicationFolder
	 * @return
	 */
	private String constructExpectedClassName(FileObject applicationFolder) {
		if (this.compositionClassName == null) {
			String baseName = applicationFolder.getName().getBaseName();
			String extension = applicationFolder.getName().getExtension();
			String expectedName = baseName.substring(0, baseName.length() - extension.length());
			
			String moduleName = getModuleLayout().getFileBasename();
	
			expectedName = expectedName + "." 
				+ Character.toUpperCase(moduleName.charAt(0)) 
				+ moduleName.substring(1);
			return expectedName;
		} 
		
		return compositionClassName;
		
	}


	public ModuleLayout getModuleLayout() {
		return delegate.getModuleLayout();
	}


	public ModuleMonitor getMonitor() {
		return delegate.getMonitor();
	}


	public synchronized String getCompositionClassName() {
		return compositionClassName;
	}


	public synchronized void setCompositionClassName(String compositionClassName) {
		this.compositionClassName = compositionClassName;
	}

}
