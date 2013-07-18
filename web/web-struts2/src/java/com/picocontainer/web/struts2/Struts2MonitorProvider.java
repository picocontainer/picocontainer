/**
 * 
 */
package com.picocontainer.web.struts2;

import ognl.OgnlRuntime;

import com.picocontainer.ComponentMonitor;
import com.picocontainer.web.providers.DelegatingAwareMonitorProvider;


public class Struts2MonitorProvider implements DelegatingAwareMonitorProvider {
	
	public Struts2MonitorProvider() {
		//Clear ognl security manager.
		if (System.getSecurityManager() == null) {
			OgnlRuntime.setSecurityManager(null);		
		}
	}

	public ComponentMonitor get() {
		return new StrutsActionInstantiatingComponentMonitor();
	}

	public ComponentMonitor wrap(final ComponentMonitor delegate) {
		return new StrutsActionInstantiatingComponentMonitor(delegate);
	}

}
