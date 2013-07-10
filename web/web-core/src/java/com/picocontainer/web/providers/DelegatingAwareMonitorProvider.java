package com.picocontainer.web.providers;

import com.picocontainer.ComponentMonitor;

public interface DelegatingAwareMonitorProvider extends MonitorProvider {

	public ComponentMonitor wrap(ComponentMonitor delegate);
}
