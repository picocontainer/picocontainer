/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.web.remoting;

import org.picocontainer.ComponentMonitor;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.containers.TransientPicoContainer;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.web.PicoServletContainerListener;
import org.picocontainer.web.StringFromRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.lang.reflect.Modifier;

@SuppressWarnings("serial")
public class PWRServletContainerListener extends PicoServletContainerListener {

    protected ComponentMonitor makeRequestComponentMonitor() {
        return new LateInstantiatingComponentMonitor();
    }

    private static class LateInstantiatingComponentMonitor extends NullComponentMonitor implements Serializable {

        public Object noComponentFound(MutablePicoContainer mutablePicoContainer, Object key) {
            if (key instanceof Class) {
                Class<?> clazz = (Class<?>) key;
                if (clazz.getName().startsWith("java.lang")) {
                    return null;
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
            return null;
        }

    }

//    protected BehaviorFactory addRequestBehaviors(BehaviorFactory beforeThisBehaviorFactory) {
//        return (BehaviorFactory) new Intercepting().wrap(beforeThisBehaviorFactory);    
//    }
}