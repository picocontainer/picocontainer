package com.picocontainer.modules.web;

import static com.picocontainer.modules.web.WebConstants.DEFAULT_MODULES_LOCATION;
import static com.picocontainer.modules.web.WebConstants.MODULES_APP_LOCATION;
import static com.picocontainer.modules.web.WebConstants.MODULES_LOCATION;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import com.picocontainer.MutablePicoContainer;
import com.picocontainer.containers.JSRPicoContainer;
import com.picocontainer.modules.ModuleBuilder;
import com.picocontainer.modules.PicoModuleSystem;

public class PicoModuleContextListener implements ServletContextListener {

	public PicoModuleContextListener() {
		// TODO Auto-generated constructor stub
	}

	public void contextInitialized(ServletContextEvent sce) {
		try {
			ServletContext context = sce.getServletContext();

			String modulesLocation = context.getInitParameter(MODULES_LOCATION);
			if (modulesLocation == null) {
				modulesLocation = DEFAULT_MODULES_LOCATION;
			}
			FileObject moduleLocation = null;
			FileSystemManager fsManager = VFS.getManager();
			if (modulesLocation.startsWith("/")) {
				String fullModulesLocation = context.getRealPath(modulesLocation);
				if (fullModulesLocation == null) {
					throw new IllegalStateException(
							"We're sorry, but Pico Modules can't be used inside a compressed war yet. "
									+ " Either unpack your war or move the modules outside and set the parameter "
									+ MODULES_LOCATION + " to a full URL.  (for example, file://something)");
				}

				moduleLocation = fsManager.resolveFile(fullModulesLocation);
			} else {
				moduleLocation = fsManager.resolveFile(modulesLocation);
			}

			PicoModuleSystem moduleSystem = new ModuleBuilder().withAutoDeployFolder(moduleLocation).build();

			moduleSystem.deploy(getParentPicoContainer());

			context.setAttribute(MODULES_APP_LOCATION, moduleSystem);

		} catch (FileSystemException e) {
			throw new RuntimeException("File System Exception loading picocontainer modules", e);
		}

	}

	/**
	 * Override for your own picocontainer.
	 * 
	 * @return
	 */
	protected MutablePicoContainer getParentPicoContainer() {
		return new JSRPicoContainer();
	}

	public void contextDestroyed(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		PicoModuleSystem moduleSystem = (PicoModuleSystem) context.getAttribute(MODULES_APP_LOCATION);
		if (moduleSystem != null) {
			try {
				moduleSystem.undeploy();
			} finally {
				context.removeAttribute(MODULES_APP_LOCATION);
			}
		}

	}

}
