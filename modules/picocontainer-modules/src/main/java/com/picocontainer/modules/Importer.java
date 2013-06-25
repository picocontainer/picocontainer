package com.picocontainer.modules;

import com.picocontainer.modules.defaults.ModuleSystemDeploymentRegistry;
import com.picocontainer.modules.defaults.PicoModuleSystemSPI;

public class Importer {

	public static void importModule(String moduleName) {
		PicoModuleSystemSPI moduleSystem = ModuleSystemDeploymentRegistry.getDeployingModuleSystem();
		moduleSystem.deployModuleByName(moduleName);
	}
}
