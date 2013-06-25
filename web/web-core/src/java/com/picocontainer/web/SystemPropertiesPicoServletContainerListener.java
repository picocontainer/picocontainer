package com.picocontainer.web;

import com.picocontainer.PicoContainer;
import com.picocontainer.containers.SystemPropertiesPicoContainer;

@SuppressWarnings("serial")
public class SystemPropertiesPicoServletContainerListener extends PicoServletContainerListener {


    protected PicoContainer makeParentContainer() {
        return new SystemPropertiesPicoContainer();
    }
}
