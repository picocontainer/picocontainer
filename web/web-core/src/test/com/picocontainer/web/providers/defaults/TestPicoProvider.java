package com.picocontainer.web.providers.defaults;

import javax.servlet.ServletContext;

import com.picocontainer.PicoContainer;
import com.picocontainer.containers.EmptyPicoContainer;
import com.picocontainer.web.providers.ParentPicoProvider;

import static org.junit.Assert.*;

public class TestPicoProvider implements ParentPicoProvider {
	
	public static final EmptyPicoContainer INSTANCE = new EmptyPicoContainer();


	public PicoContainer getParentPicoContainer(ServletContext context) {
		assertNotNull(context);
		return INSTANCE;
	}

}
