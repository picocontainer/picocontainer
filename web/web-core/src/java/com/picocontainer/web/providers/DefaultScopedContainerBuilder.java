package com.picocontainer.web.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.picocontainer.ComponentFactory;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.PicoContainer;
import com.picocontainer.behaviors.Caching;
import com.picocontainer.behaviors.Guarding;
import com.picocontainer.behaviors.Storing;
import com.picocontainer.web.ScopedContainers;
import com.picocontainer.web.ThreadLocalLifecycleState;

public class DefaultScopedContainerBuilder extends AbstractScopedContainerBuilder {

	public DefaultScopedContainerBuilder() {
	}

	public ScopedContainers makeScopedContainers(boolean stateless) {
		
        DefaultPicoContainer appCtnr = new DefaultPicoContainer(getParentContainer(), getLifecycleStrategy(), getApplicationMonitor(),getAllApplicationComponentFactories());
        appCtnr.setName("application");
        DefaultPicoContainer sessCtnr;
        PicoContainer parentOfRequestContainer;
        ThreadLocalLifecycleState sessionState;
        Storing sessStoring;
        if (stateless) {
            sessionState = null;
            sessStoring = null;
            sessCtnr = null;
            parentOfRequestContainer = appCtnr;
        } else {
            sessionState = new ThreadLocalLifecycleState();
            sessStoring = new Storing();
            sessCtnr = new DefaultPicoContainer(appCtnr, getLifecycleStrategy(), getSessionMonitor(), getAllSessionComponentFactories(sessStoring));
            sessCtnr.setLifecycleState(sessionState);
            sessCtnr.setName("session");
            parentOfRequestContainer = sessCtnr;
        }
        Storing reqStoring = new Storing();
        List<ComponentFactory> factories = new ArrayList<ComponentFactory>();
        factories.add(new Guarding());
        factories.addAll(Arrays.asList(this.getRequestComponentFactories()));
        factories.add(reqStoring);
        
        DefaultPicoContainer reqCtnr = new DefaultPicoContainer(parentOfRequestContainer, getLifecycleStrategy(), getRequestMonitor(), getAllRequestComponentFactories(reqStoring));
        reqCtnr.setName("request");
        ThreadLocalLifecycleState requestState = new ThreadLocalLifecycleState();
        reqCtnr.setLifecycleState(requestState);
        return new ScopedContainers(appCtnr, sessCtnr, reqCtnr, sessStoring, reqStoring, sessionState, requestState);
	}
	
	protected ComponentFactory[] getAllRequestComponentFactories(Storing reqStoring) {
        List<ComponentFactory> factories = new ArrayList<ComponentFactory>();
        factories.add(new Guarding());
        factories.addAll(Arrays.asList(this.getRequestComponentFactories()));
        factories.add(reqStoring);
        return factories.toArray(new ComponentFactory[factories.size()]);		
	}
	
	protected ComponentFactory[] getAllApplicationComponentFactories() {
		 List<ComponentFactory> factories = new ArrayList<ComponentFactory>();
		 factories.add(new Guarding());
	        factories.addAll(Arrays.asList(this.getAppComponentFactories()));
		 return factories.toArray(new ComponentFactory[factories.size()]);
	}
	
	protected ComponentFactory[] getAllSessionComponentFactories(Storing sessionStoring) {
		 List<ComponentFactory> factories = new ArrayList<ComponentFactory>();
		 factories.add(new Guarding());
	     factories.addAll(Arrays.asList(this.getSessionComponentFactories()));
	     factories.add(new Caching());
		 return factories.toArray(new ComponentFactory[factories.size()]);		
	}

}
