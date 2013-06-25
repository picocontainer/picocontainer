package com.picocontainer.web.webwork2;

import com.picocontainer.web.PicoServletContainerListener;

import com.opensymphony.xwork.ObjectFactory;

import javax.servlet.ServletContextEvent;

import ognl.OgnlRuntime;

@SuppressWarnings("serial")
public class WebWork2PicoServletContainerListener extends PicoServletContainerListener {

    public void contextInitialized(ServletContextEvent event) {
        OgnlRuntime.setSecurityManager(null);
        super.contextInitialized(event);
        ObjectFactory.setObjectFactory(new PicoObjectFactory());
    }
}
