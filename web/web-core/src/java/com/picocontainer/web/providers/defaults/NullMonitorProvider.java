/**
 * 
 */
package com.picocontainer.web.providers.defaults;

import com.picocontainer.ComponentMonitor;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.web.providers.MonitorProvider;


/**
 * @author Mike
 *
 */
public class NullMonitorProvider implements MonitorProvider {

	public ComponentMonitor get() {
		return new NullComponentMonitor();
	}

}
