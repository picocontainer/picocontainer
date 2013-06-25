/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package com.picocontainer.gems.monitors;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.picocontainer.ComponentMonitor;

/**
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class SingleLoggerSlf4JComponentMonitorTestCase extends ComponentMonitorHelperTestCase {

    @Override
	protected ComponentMonitor makeComponentMonitor() {
        return new Slf4jComponentMonitor(Slf4jComponentMonitor.class);
    }

    @Override
	protected Constructor getConstructor() throws NoSuchMethodException {
        return getClass().getConstructor((Class[])null);
    }

    @Override
	protected Method getMethod() throws NoSuchMethodException {
        return getClass().getDeclaredMethod("makeComponentMonitor", (Class[])null);
    }

    @Override
	protected String getLogPrefix() {
        return "[" + Slf4jComponentMonitor.class.getName() + "] ";
    }

    @Override
	public void testShouldTraceNoComponent() throws IOException {
        super.testShouldTraceNoComponent();
    }

}
