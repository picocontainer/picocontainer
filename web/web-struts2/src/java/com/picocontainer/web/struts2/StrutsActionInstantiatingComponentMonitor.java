package com.picocontainer.web.struts2;

import static com.picocontainer.Characteristics.NO_CACHE;

import java.lang.reflect.Modifier;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.Result;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.monitors.NullComponentMonitor;

@SuppressWarnings("serial")
public class StrutsActionInstantiatingComponentMonitor extends NullComponentMonitor {
	
	private final ComponentMonitor delegate;

	public StrutsActionInstantiatingComponentMonitor() {
		this(null);
	}
	
	public StrutsActionInstantiatingComponentMonitor(ComponentMonitor delegate) {
		this.delegate = delegate;
		
	}
	
    public Object noComponentFound(MutablePicoContainer mutablePicoContainer, Object key) {
        return noComponent(mutablePicoContainer, key);
    }

    private Object noComponent(MutablePicoContainer mutablePicoContainer, Object key) {
    	
    	boolean isConcrete = true;
    	//We get first crack at it.
        if (key instanceof Class) {
            Class<?> clazz = (Class<?>) key;
            
            if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            	isConcrete = false;
            }
            
            if (isConcrete) {
	            //If its a struts object then we add the component to the request pico
	            //and return that.
	            if (Action.class.isAssignableFrom(clazz) || Result.class.isAssignableFrom(clazz)) {
	                try {
	                	mutablePicoContainer.as(NO_CACHE).addComponent(clazz);
	                } catch (NoClassDefFoundError e) {
	                    if (e.getMessage().equals("org/apache/velocity/context/Context")) {
	                        // half expected. XWork seems to setup stuff that cannot
	                        // work
	                        // TODO if this is the case we should make configurable
	                        // the list of classes we "expect" not to find.  Odd!
	                    } else {
	                        throw e;
	                    }
	                }
	
	                return mutablePicoContainer.getComponent(clazz);
	            }
         
            }
            
        }
        
        //Otherwise we give the delegate a chance to instantiate.
        Object result = (delegate != null) ? delegate.noComponentFound(mutablePicoContainer, key) : null;
        
        //If that still fails then we try to access a default constructor.
        if (result == null && key instanceof Class && isConcrete ) {
            Class<?> clazz = (Class<?>) key;
            try {
                if (clazz.getConstructor(new Class[0]) != null) {
                    return clazz.newInstance();
                }
            } catch (InstantiationException e) {
                throw new PicoCompositionException("can't instantiate " + key);
            } catch (IllegalAccessException e) {
                throw new PicoCompositionException("illegal access " + key);
            } catch (NoSuchMethodException e) {
            	  throw new PicoCompositionException("no default constructor. " + key);
            }
        }
        return result;
         
    }
}