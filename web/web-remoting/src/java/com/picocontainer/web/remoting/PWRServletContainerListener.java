/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package com.picocontainer.web.remoting;

import com.picocontainer.web.PicoServletContainerListener;
import com.picocontainer.web.providers.LateInstantiatingComponentMonitor;
import com.picocontainer.ComponentMonitor;


@SuppressWarnings("serial")
public class PWRServletContainerListener extends PicoServletContainerListener {

    protected ComponentMonitor makeRequestComponentMonitor() {
        return new LateInstantiatingComponentMonitor();
    }

//    protected BehaviorFactory addRequestBehaviors(BehaviorFactory beforeThisBehaviorFactory) {
//        return (BehaviorFactory) new Intercepting().wrap(beforeThisBehaviorFactory);    
//    }
}