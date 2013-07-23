package com.picocontainer.web.providers;

import com.picocontainer.ComponentFactory;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoContainer;
import com.picocontainer.lifecycle.StartableLifecycleStrategy;
import com.picocontainer.monitors.NullComponentMonitor;

public abstract class AbstractScopedContainerBuilder implements ScopedContainerProvider {

	private ComponentMonitor applicationMonitor = new NullComponentMonitor();
	
	private ComponentMonitor sessionMonitor= new NullComponentMonitor();
	
	private ComponentMonitor requestMonitor = new NullComponentMonitor();
	
	private LifecycleStrategy lifecycleStrategy = new StartableLifecycleStrategy(requestMonitor);
	
	private ComponentFactory[] requestComponentFactories = new ComponentFactory[0];
	
	private ComponentFactory[] sessionComponentFactories= new ComponentFactory[0];
	
	private ComponentFactory[] appComponentFactories = new ComponentFactory[0];
	
	
	private PicoContainer parentContainer = null;
	
	private Class<? extends MutablePicoContainer> picoImplementation = DefaultPicoContainer.class;
	

	public ComponentMonitor getApplicationMonitor() {
		return applicationMonitor;
	}

	public void setApplicationMonitor(ComponentMonitor applicationMonitor) {
		this.applicationMonitor = applicationMonitor;
	}

	public ComponentMonitor getSessionMonitor() {
		return sessionMonitor;
	}

	public void setSessionMonitor(ComponentMonitor sessionMonitor) {
		this.sessionMonitor = sessionMonitor;
	}

	public ComponentMonitor getRequestMonitor() {
		return requestMonitor;
	}

	public void setRequestMonitor(ComponentMonitor requestMonitor) {
		this.requestMonitor = requestMonitor;
	}

	public LifecycleStrategy getLifecycleStrategy() {
		return lifecycleStrategy;
	}

	public void setLifecycleStrategy(LifecycleStrategy lifecycleStrategy) {
		this.lifecycleStrategy = lifecycleStrategy;
	}

	public ComponentFactory[] getRequestComponentFactories() {
		return requestComponentFactories;
	}

	public void setRequestComponentFactories(ComponentFactory[] componentFactories) {
		this.requestComponentFactories = componentFactories;
	}

	public Class<? extends MutablePicoContainer> getPicoImplementation() {
		return picoImplementation;
	}

	public void setPicoImplementation(Class<? extends MutablePicoContainer> picoImplementation) {
		this.picoImplementation = picoImplementation;
	}

	public PicoContainer getParentContainer() {
		return parentContainer;
	}

	public void setParentContainer(PicoContainer parentContainer) {
		this.parentContainer = parentContainer;
	}

	public ComponentFactory[] getSessionComponentFactories() {
		return sessionComponentFactories;
	}

	public void setSessionComponentFactories(ComponentFactory[] sessionComponentFactories) {
		this.sessionComponentFactories = sessionComponentFactories;
	}

	public ComponentFactory[] getAppComponentFactories() {
		return appComponentFactories;
	}

	public void setAppComponentFactories(ComponentFactory[] appComponentFactories) {
		this.appComponentFactories = appComponentFactories;
	}


}
