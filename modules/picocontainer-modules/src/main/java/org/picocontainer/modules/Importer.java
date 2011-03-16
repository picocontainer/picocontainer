package org.picocontainer.modules;

import org.picocontainer.modules.defaults.ModuleSystemDeploymentRegistry;
import org.picocontainer.modules.defaults.PicoModuleSystemSPI;

public class Importer {

	public static void importModule(String moduleName) {
		PicoModuleSystemSPI moduleSystem = ModuleSystemDeploymentRegistry.getDeployingModuleSystem();
		moduleSystem.deployModuleByName(moduleName);
	}
}
