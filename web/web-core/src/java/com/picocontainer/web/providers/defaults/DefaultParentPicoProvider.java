package com.picocontainer.web.providers.defaults;

import javax.servlet.ServletContext;

import com.picocontainer.PicoContainer;
import com.picocontainer.containers.EmptyPicoContainer;
import com.picocontainer.web.providers.ParentPicoProvider;

public class DefaultParentPicoProvider implements ParentPicoProvider {

	public PicoContainer getParentPicoContainer(ServletContext context) {
		return new EmptyPicoContainer();
	}

}
