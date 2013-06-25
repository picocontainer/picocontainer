package com.picocontainer.script;

import com.picocontainer.PicoContainer;
import com.picocontainer.Startable;

public class StartContainerPostBuildContainerAction implements
		PostBuildContainerAction {

	public PicoContainer onNewContainer(final PicoContainer container) {
        if (container instanceof Startable) {
            ((Startable) container).start();
        }
        return container;
	}

}
