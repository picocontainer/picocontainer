package org.picocontainer.web;

import org.picocontainer.MutablePicoContainer;

@SuppressWarnings("serial")
public class PicoServletFilter extends AbstractPicoServletContainerFilter {

    static transient ThreadLocal<MutablePicoContainer> currentAppContainer = new ThreadLocal<MutablePicoContainer>();
    static transient ThreadLocal<MutablePicoContainer> currentSessionContainer = new ThreadLocal<MutablePicoContainer>();
    static transient ThreadLocal<MutablePicoContainer> currentRequestContainer = new ThreadLocal<MutablePicoContainer>();

    protected void setAppContainer(MutablePicoContainer container) {
         if (currentRequestContainer == null) {
            currentRequestContainer = new ThreadLocal<MutablePicoContainer>();
        }
        currentAppContainer.set(container);
    }

    protected void setRequestContainer(MutablePicoContainer container) {
        if (currentRequestContainer == null) {
            currentRequestContainer = new ThreadLocal<MutablePicoContainer>();
        }
        currentRequestContainer.set(container);
    }

    protected void setSessionContainer(MutablePicoContainer container) {
        if (currentSessionContainer == null) {
            currentSessionContainer = new ThreadLocal<MutablePicoContainer>();
        }
        currentSessionContainer.set(container);
    }
    
    protected MutablePicoContainer getRequestContainer() {
    	MutablePicoContainer result = currentRequestContainer != null ? currentRequestContainer.get() : null;
    	if (result == null) {
    		throw new PicoContainerWebException("No request container has been set.  Is PicoServletContainerFilter installed in your web.xml?  " +
    				"And if it is, is exposeServletInfrastructure set to true in filter init parameters?");
    	}
    	
    	return result;
    }
}