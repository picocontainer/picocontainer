package org.picocontainer.web;

import org.picocontainer.PicoContainer;
import org.picocontainer.containers.SystemPropertiesPicoContainer;

@SuppressWarnings("serial")
public class SystemPropertiesPicoServletContainerListener extends PicoServletContainerListener {


    protected PicoContainer makeParentContainer() {
        return new SystemPropertiesPicoContainer();
    }
}
