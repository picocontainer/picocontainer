package org.picocontainer.modules;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.script.util.MultiException;

public interface PicoModuleSystem {

	public MutablePicoContainer deploy(MutablePicoContainer parent) throws MultiException;
	
	public MutablePicoContainer deploy() throws MultiException;
	
	public void undeploy() throws MultiException;

}
