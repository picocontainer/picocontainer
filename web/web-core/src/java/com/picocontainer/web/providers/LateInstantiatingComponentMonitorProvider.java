package com.picocontainer.web.providers;

import com.picocontainer.ComponentMonitor;

public class LateInstantiatingComponentMonitorProvider implements DelegatingAwareMonitorProvider {

	public LateInstantiatingComponentMonitorProvider() {
	}

	public ComponentMonitor get() {
		return new LateInstantiatingComponentMonitor();
	}

	public ComponentMonitor wrap(ComponentMonitor delegate) {
		return new LateInstantiatingComponentMonitor(delegate);
	}

}
