/**
 * 
 */
package org.picocontainer.modules.defaults;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.modules.ModuleMonitor;
import org.picocontainer.modules.PicoModuleSystem;
import org.picocontainer.modules.deployer.Deployer;
import org.picocontainer.script.util.MultiException;

/**
 * @author Mike
 *
 */
public class DefaultModuleSystem implements PicoModuleSystem {
	
	private final ModuleMonitor monitor;
	private final Deployer deployer;
	private final FileObject moduleDirectory;
	private final ClassLoader classLoader;

	public DefaultModuleSystem(ModuleMonitor monitor, Deployer deployer, FileObject moduleDirectory, ClassLoader classLoader) {
		this.monitor = monitor;
		this.deployer = deployer;
		this.moduleDirectory = moduleDirectory;
		this.classLoader = classLoader;		
	}


	public MutablePicoContainer deploy(MutablePicoContainer parent)
			throws MultiException {
		long startTime = System.currentTimeMillis();
		monitor.startingMultiModuleDeployment(moduleDirectory);
		MultiException errors = new MultiException("Pico Module Deployment");
		MutablePicoContainer returnValue = parent;
		try {
			for (FileObject eachChild : moduleDirectory.getChildren()) {
				try {
					if (!isDeployable(eachChild)) {
						monitor.skippingDeploymentBecauseNotDeployable(eachChild);
					}
					
					deployer.deploy(eachChild, this.classLoader, parent, null);
				} catch (Exception ex) {
					errors.addException(ex);
				}				
			}
		
		} catch (FileSystemException ex) {
			errors.addException(ex);			
		}
		
		long endTime = System.currentTimeMillis();
		if (errors.getErrorCount() > 0) {
			monitor.multiModuleDeploymentFailed(moduleDirectory, errors);
			throw errors;
		} 
		monitor.multiModuleDeploymentSuccess(moduleDirectory, returnValue, endTime - startTime);
		return parent;
	}



	public MutablePicoContainer deploy() throws MultiException {
		MutablePicoContainer basicPico = new PicoBuilder().withCaching().withLifecycle().build();
		return deploy(basicPico);
	}


	public void undeploy() throws MultiException {


	}
	


	protected boolean isDeployable(FileObject allChildren) {
		return true;
	}	

}
