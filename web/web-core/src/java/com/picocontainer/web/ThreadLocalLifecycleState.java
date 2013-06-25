package com.picocontainer.web;

import com.picocontainer.lifecycle.DefaultLifecycleState;
import com.picocontainer.lifecycle.LifecycleState;

public class ThreadLocalLifecycleState implements LifecycleState {

    private LifecycleStateThreadLocal tl = new LifecycleStateThreadLocal();

   
    public void removingComponent() {
    	LifecycleState ls = getOrCreateThreadlocalLifecycleState();
    	if (ls != null) {
    		ls.removingComponent();	
    	}
        
    }

    public void starting(String containerName) {
    	LifecycleState ls = getOrCreateThreadlocalLifecycleState();
    	if (ls != null) {    	
    		ls.starting(containerName);
    	}
    }

    public void stopping(String containerName) {
    	LifecycleState ls = getOrCreateThreadlocalLifecycleState();
    	if (ls != null) {    	
    		ls.stopping(containerName);
    	}
    }

    public void stopped() {
    	LifecycleState ls = getOrCreateThreadlocalLifecycleState();
    	if (ls != null) {
    		ls.stopped();	
    	}
        
    }

    public boolean isStarted() {
    	LifecycleState ls = getOrCreateThreadlocalLifecycleState();
    	if (ls != null) {
    		return ls.isStarted();
    	}
    	return false;
    }

    public boolean isStopped() {
    	LifecycleState ls = getOrCreateThreadlocalLifecycleState();
    	if (ls != null) {
    		return ls.isStopped();
    	}
    	
    	return false;
    }

    public boolean isDisposed() {
    	LifecycleState ls = getOrCreateThreadlocalLifecycleState();
    	if (ls != null) {
    		return ls.isDisposed();
    	}
    	
    	return true;
    }

    public void disposing(String containerName) {
    	LifecycleState ls = getOrCreateThreadlocalLifecycleState();
    	if (ls != null) {
            ls.disposing(containerName);    		
    	}
    }

    public void disposed() {
    	LifecycleState ls = getOrCreateThreadlocalLifecycleState();
    	if (ls != null) {
            ls.disposed();
    	}
    }

    public void putLifecycleStateModelForThread(LifecycleState lifecycleState) {
    	//Forces creation of lifecycle threadlocal
    	getOrCreateThreadlocalLifecycleState();
		tl.set(lifecycleState);	
    }

    public LifecycleState resetStateModelForThread() {
        DefaultLifecycleState dls = new DefaultLifecycleState();
        invalidateStateModelForThread();
        tl = new LifecycleStateThreadLocal();
        tl.set(dls);
        return dls;
    }

    public void invalidateStateModelForThread() {
    	if (tl != null) {
    		tl.remove();
    		tl = null;
    	}
    }

    private LifecycleState getOrCreateThreadlocalLifecycleState() {
    	if (tl == null) {
    		tl = new LifecycleStateThreadLocal();
    	}
		return tl.get();
	}


	private static class LifecycleStateThreadLocal extends ThreadLocal<LifecycleState> {
        protected LifecycleState initialValue() {
            return new DefaultLifecycleState();
        }
    }

}
