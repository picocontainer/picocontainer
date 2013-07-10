package com.picocontainer.web.providers.defaults;

import com.picocontainer.ComponentMonitor;
import com.picocontainer.gems.monitors.CommonsLoggingComponentMonitor;
import com.picocontainer.web.providers.DelegatingAwareMonitorProvider;

/**
 * Optional way to 
 * @author Mike
 *
 */
public class CommonsLoggingMonitorProvider implements DelegatingAwareMonitorProvider {


	public ComponentMonitor get() {
		return new CommonsLoggingComponentMonitor();
	}

	public ComponentMonitor wrap(ComponentMonitor delegate) {
		return new CommonsLoggingComponentMonitor(delegate);
	}

}
