package org.picocontainer.script;

import org.picocontainer.PicoContainer;
import org.picocontainer.Startable;

public class StartContainerPostBuildContainerAction implements
		PostBuildContainerAction {

	public PicoContainer onNewContainer(final PicoContainer container) {
        if (container instanceof Startable) {
            ((Startable) container).start();
        }
        return container;
	}

}
