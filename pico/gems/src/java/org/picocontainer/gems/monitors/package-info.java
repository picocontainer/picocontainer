/**
 * This package contains various implementations of the {@link org.picocontainer.ComponentMonitor}
 * interface that extend the capabilities supplied by the default PicoContainer. 
 * Most Monitors rely on one or more 3rd party libraries to perform their job, 
 * however, they are independent and you only need to import the library associated
 * with the monitor you desire to utilize.
 * <h4>Logging Monitors</h4>
 * <p>This package contains monitors that log to the various popular logging packages:<p>
 * <ul>
 * 	<li><a href="http://commons.apache.org/logging/">Apache Commons Logging</a>: 
 *  {@link org.picocontainer.gems.monitors.CommonsLoggingComponentMonitor</li>
 * 	<li><a href="http://logging.apache.org/log4j/">Apache Log4j</a>:  
 * {@link org.picocontainer.gems.monitors.Log4jComponentMonitor</li>
 * 	<li><a href="http://www.slf4j.org/">Slf4j</a>:  
 * {@link org.picocontainer.gems.monitors.Slf4jComponentMonitor</li>
 * </ul>
 */
package org.picocontainer.gems.monitors;

