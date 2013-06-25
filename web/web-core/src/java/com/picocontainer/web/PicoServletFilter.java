package com.picocontainer.web;

import com.picocontainer.MutablePicoContainer;

@SuppressWarnings("serial")
public class PicoServletFilter extends AbstractPicoServletContainerFilter {

    private static transient ThreadLocal<MutablePicoContainer> currentAppContainer = new ThreadLocal<MutablePicoContainer>();
    private static transient ThreadLocal<MutablePicoContainer> currentSessionContainer = new ThreadLocal<MutablePicoContainer>();
    private static transient ThreadLocal<MutablePicoContainer> currentRequestContainer = new ThreadLocal<MutablePicoContainer>();


    protected final void setAppContainer(MutablePicoContainer container) {
         if (currentRequestContainer == null) {
            currentRequestContainer = new ThreadLocal<MutablePicoContainer>();
        }
        currentAppContainer.set(container);
    }

    protected final void setRequestContainer(MutablePicoContainer container) {
        if (currentRequestContainer == null) {
            currentRequestContainer = new ThreadLocal<MutablePicoContainer>();
        }
        currentRequestContainer.set(container);
    }

    protected final void setSessionContainer(MutablePicoContainer container) {
        if (currentSessionContainer == null) {
            currentSessionContainer = new ThreadLocal<MutablePicoContainer>();
        }
        currentSessionContainer.set(container);
    }
    
    protected final MutablePicoContainer getRequestContainer() {
    	MutablePicoContainer result = currentRequestContainer != null ? currentRequestContainer.get() : null;
    	if (result == null) {
    		throw new PicoContainerWebException("No request container has been set.  Is PicoServletContainerFilter installed in your web.xml?  " +
    				"And if it is, is exposeServletInfrastructure set to true in filter init parameters?");
    	}
    	
    	return result;
    }

	public void destroy() {
		if (currentRequestContainer != null) {
			currentRequestContainer.remove();
			currentRequestContainer = null;
		}
		
		if (currentSessionContainer != null) {
			currentSessionContainer.remove();
			currentSessionContainer = null;
		}
		
		if (currentAppContainer != null) {
			currentAppContainer.remove();
			currentAppContainer = null;
		}
		
	}

}