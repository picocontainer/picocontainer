package org.picocontainer.web.webwork2;

import org.picocontainer.web.PicoServletContainerListener;

import javax.servlet.ServletContextEvent;

import com.opensymphony.xwork.ObjectFactory;
import ognl.OgnlRuntime;

@SuppressWarnings("serial")
public class WebWork2PicoServletContainerListener extends PicoServletContainerListener {

    public void contextInitialized(ServletContextEvent event) {
        OgnlRuntime.setSecurityManager(null);
        super.contextInitialized(event);
        ObjectFactory.setObjectFactory(new PicoObjectFactory());
    }
}
