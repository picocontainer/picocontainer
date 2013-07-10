/**
 * 
 */
package com.picocontainer.web.struts2;

import com.picocontainer.ComponentMonitor;
import com.picocontainer.web.providers.DelegatingAwareMonitorProvider;


public class Struts2MonitorProvider implements DelegatingAwareMonitorProvider {

	public ComponentMonitor get() {
		return new StrutsActionInstantiatingComponentMonitor();
	}

	public ComponentMonitor wrap(final ComponentMonitor delegate) {
		return new StrutsActionInstantiatingComponentMonitor(delegate);
	}

}
