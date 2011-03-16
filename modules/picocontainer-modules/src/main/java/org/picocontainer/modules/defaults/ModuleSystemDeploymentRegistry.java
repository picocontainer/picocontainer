package org.picocontainer.modules.defaults;


/**
 * Internal use only adapter for allowing importer to get access to the current deploying
 * module.
 * @author Michael Rimov
 *
 */
public class ModuleSystemDeploymentRegistry {

	private static ThreadLocal<PicoModuleSystemSPI> currentDeployingModule = new ThreadLocal<PicoModuleSystemSPI>();
	
	public static void setDeployingModule(PicoModuleSystemSPI moduleSystem) {
		currentDeployingModule.set(moduleSystem);
	}
	
	public static void deploymentComplete() {
		//No clearing yet for JDK 1.5 :(  This is the best we can do for now.
		currentDeployingModule.set(null);
	}
	
	public static PicoModuleSystemSPI getDeployingModuleSystem() {
		PicoModuleSystemSPI returnValue = currentDeployingModule.get();
		if (returnValue == null) {
			throw new IllegalStateException("No module system is being currently deployed");
		}
		return returnValue;
	}
}
