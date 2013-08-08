package com.picocontainer.modules.web;

public interface WebConstants {

	public static final String MODULES_LOCATION = "pico.modules-location";
	
	public static final String DEFAULT_MODULES_LOCATION = "/WEB-INF/modules";
	
	public static final String MODULES_APP_LOCATION = PicoModuleContextListener.class.getName() + ".modules";
}
