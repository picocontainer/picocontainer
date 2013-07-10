package com.picocontainer.web.providers;

import static com.picocontainer.web.ContextParameters.APP_BEHAVIORS;
import static com.picocontainer.web.ContextParameters.APP_COMPONENT_MONITORS;
import static com.picocontainer.web.ContextParameters.LIFECYCLE_STRATEGY;
import static com.picocontainer.web.ContextParameters.REQUEST_BEHAVIORS;
import static com.picocontainer.web.ContextParameters.REQUEST_COMPONENT_MONITORS;
import static com.picocontainer.web.ContextParameters.SESSION_BEHAVIORS;
import static com.picocontainer.web.ContextParameters.SESSION_COMPONENT_MONITORS;
import static com.picocontainer.web.ContextParameters.STATELESS_WEBAPP;

import javax.servlet.ServletContext;

import com.picocontainer.web.PicoServletContainerListener;

public class PicoServletParameterProcessor {


	private boolean stateless;
	

	public AbstractScopedContainerBuilder processContextParameters(ServletContext context) {
		@SuppressWarnings("deprecation")
		String statelessAsString = context.getInitParameter(PicoServletContainerListener.STATELESS_WEBAPP);
		if (statelessAsString != null) {
			System.err.println("Context parameter 'stateless-webapp' is deprecated.  Please change your web.xml's init parameter to 'pico.stateless-webapp'");
		}
		
		stateless = Boolean.parseBoolean(statelessAsString);
		
		if (stateless == false) {
			stateless = Boolean.parseBoolean(context.getInitParameter(STATELESS_WEBAPP));
		}
		
		AbstractScopedContainerBuilder returnResult = constructScopedContainerBuilder();
		
		MonitorProviderFactory monitorProviders = new MonitorProviderFactory();
		
		String appMonitors = context.getInitParameter(APP_COMPONENT_MONITORS);
		String sessionMonitors = context.getInitParameter(SESSION_COMPONENT_MONITORS);
		String requestMonitors = context.getInitParameter(REQUEST_COMPONENT_MONITORS);
		returnResult.setApplicationMonitor(monitorProviders.constructProvider(context, appMonitors));
		returnResult.setSessionMonitor(monitorProviders.constructProvider(context, sessionMonitors));
		returnResult.setRequestMonitor(monitorProviders.constructProvider(context, requestMonitors));
		
		String appBehaviors =context.getInitParameter(APP_BEHAVIORS);
		String sessionBehaviors =context.getInitParameter(SESSION_BEHAVIORS); 
		String requestBehaviors =context.getInitParameter(REQUEST_BEHAVIORS); 
		ComponentFactoryProviders cafp = new ComponentFactoryProviders();
		returnResult.setRequestComponentFactories(cafp.constructProvider(context, requestBehaviors));
		returnResult.setSessionComponentFactories(cafp.constructProvider(context, sessionBehaviors));
		returnResult.setAppComponentFactories(cafp.constructProvider(context, appBehaviors));
		
		String lifecycleStrategy = context.getInitParameter(LIFECYCLE_STRATEGY);
		returnResult.setLifecycleStrategy(new LifecycleProviderFactory(returnResult.getApplicationMonitor()).constructProvider(context, lifecycleStrategy) );

		return returnResult;
	}
	
	/**
	 * Override for your own needs.
	 * @return
	 */
	protected AbstractScopedContainerBuilder constructScopedContainerBuilder() {
		return new DefaultScopedContainerBuilder();
	}
	
	/**
	 * Override for your own needs.
	 * @param containerBuilder
	 * @param context
	 */
	protected void processAdditionalParameters(AbstractScopedContainerBuilder containerBuilder, ServletContext context) {
		
	}

	public boolean isStateless() {
		return stateless;
	}

	public void setStateless(boolean stateless) {
		this.stateless = stateless;
	}
}
