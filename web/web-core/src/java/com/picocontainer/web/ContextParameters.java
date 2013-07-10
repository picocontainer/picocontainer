package com.picocontainer.web;

/**
 * List of supported context parameters for PicoContainer {@link com.picocontainer.web.PicoServletContainerListener}.
 * @author Michael Rimov
 */
public interface ContextParameters {

	
	/**
	 * Set to true if you don't want Session-level picocontainers created.  This is recommended for 
	 * public-facing websites/services so the PicoContainer filter doesn't automatically create a session
	 * with each webserver hit.
	 */
	String STATELESS_WEBAPP = "pico.stateless-webapp";
	
	/**
	 * Comma-delimited list of @link com.picocontainer.web.providers.MonitorProvider MonitorProvider} class names
	 * for request-level container monitor.
	 */
	String REQUEST_COMPONENT_MONITORS = "pico.request-component-monitor";

	/**
	 * Comma-delimited list of {@link com.picocontainer.web.providers.MonitorProvider MonitorProvider} class names
	 * for session-level container monitor.  If <code>pico.stateless-webapp</code> is set to true, then
	 * this value will be ignored.
	 */
	String SESSION_COMPONENT_MONITORS = "pico.session-component-monitor";

	/**
	 * Comma-delimieted list of {@link com.picocontainer.web.providers.MonitorProvider MonitorProvider} class names. 
	 */
	String APP_COMPONENT_MONITORS = "pico.app-component-monitor";
	
	/**
	 * Comma-delimited list of behavior classnames for request-level components.
	 */
	String REQUEST_BEHAVIORS = "pico.request-behaviors";
	
	/**
	 * Comma-delimited list of behaviors for session-level components.  If <code>pico.stateless-webapp</code> is
	 * defined, this value will be ignored.
	 */
	String SESSION_BEHAVIORS = "pico.session-behaviors";
	
	/**
	 * Comma-delimited list of behaviors for application-level components:
	 * example:
	 * <p>
	 * <code>com.picocontainer.behaviors.Locking, com.picocontainer.gems.jmx.JMXExposing</code> 
	 * </p>
	 */
	String APP_BEHAVIORS = "pico.app-behaviors";

	/**
	 * Classname of the lifecycle strategy to use with the picocontainers.  
	 * One value only.  Example:  <code>com.picocontainer.lifecycle.StartableLifecycleStrategy</code>
	 */
	String LIFECYCLE_STRATEGY = "pico.lifecycle-strategy";
}
