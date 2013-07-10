package com.picocontainer.web.providers;

import java.io.Serializable;
import java.lang.reflect.Modifier;

import javax.servlet.http.HttpServletRequest;

import com.picocontainer.ComponentMonitor;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.containers.TransientPicoContainer;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.web.StringFromRequest;

@SuppressWarnings("serial")
public class LateInstantiatingComponentMonitor extends NullComponentMonitor implements Serializable {
	
	private ComponentMonitor delegate;

	public LateInstantiatingComponentMonitor(ComponentMonitor delegate) {
		this.delegate = delegate;
	}
	
	
	public LateInstantiatingComponentMonitor() {
		this(null);
	}

    public Object noComponentFound(MutablePicoContainer mutablePicoContainer, Object key) {
        if (key instanceof Class) {
            Class<?> clazz = (Class<?>) key;
            if (clazz.getName().startsWith("java.lang")) {
                return (delegate != null) ? delegate.noComponentFound(mutablePicoContainer, key) : null;
            }
            if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                Object instance = new TransientPicoContainer(mutablePicoContainer)
                        .addComponent(clazz).getComponent(clazz);
                if (instance != null) {
                    return instance;
                }
            }
        } else if (key instanceof String) {
            Object instance = new StringFromRequest((String) key).provide(
                    mutablePicoContainer.getComponent(HttpServletRequest.class));
            if (instance != null) {
                return instance;
            }
        }
        
        //Delegate down if it exists.
        return (delegate != null) ? delegate.noComponentFound(mutablePicoContainer, key) : null;
    }

}