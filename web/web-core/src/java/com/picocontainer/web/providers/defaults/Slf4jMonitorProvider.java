package com.picocontainer.web.providers.defaults;

import com.picocontainer.ComponentMonitor;
import com.picocontainer.gems.monitors.Slf4jComponentMonitor;
import com.picocontainer.web.providers.DelegatingAwareMonitorProvider;

public class Slf4jMonitorProvider implements DelegatingAwareMonitorProvider {


	public ComponentMonitor get() {
		return new Slf4jComponentMonitor();
	}

	public ComponentMonitor wrap(ComponentMonitor delegate) {
		return new Slf4jComponentMonitor(delegate);
	}

}
