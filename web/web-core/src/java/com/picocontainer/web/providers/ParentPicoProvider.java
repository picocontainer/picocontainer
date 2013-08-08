package com.picocontainer.web.providers;

import javax.servlet.ServletContext;

import com.picocontainer.PicoContainer;

public interface ParentPicoProvider {

	public PicoContainer getParentPicoContainer(ServletContext context);
	
}
