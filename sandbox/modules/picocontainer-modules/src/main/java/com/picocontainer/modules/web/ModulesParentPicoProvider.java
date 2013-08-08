package com.picocontainer.modules.web;

import static com.picocontainer.modules.web.WebConstants.MODULES_APP_LOCATION;

import javax.servlet.ServletContext;

import com.picocontainer.PicoContainer;
import com.picocontainer.modules.PicoModuleSystem;
import com.picocontainer.web.PicoServletContainerListener;
import com.picocontainer.web.providers.ParentPicoProvider;

public class ModulesParentPicoProvider implements ParentPicoProvider {

	public PicoContainer getParentPicoContainer(ServletContext context) {
		PicoModuleSystem moduleSystem = (PicoModuleSystem) context.getAttribute(MODULES_APP_LOCATION);
		if (moduleSystem == null) {
			throw new RuntimeException("Pico Module System has not yet been deployed.  Please make sure that " 
				+ PicoModuleContextListener.class.getName() 
				+ " is a context listener before " 
				+ PicoServletContainerListener.class.getName() + " inside your web.xml");
		}
		return moduleSystem.getPico();
	}

}
