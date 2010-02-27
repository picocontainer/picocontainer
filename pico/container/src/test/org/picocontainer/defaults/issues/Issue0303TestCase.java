/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.defaults.issues;

import org.picocontainer.ComponentMonitor;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.Startable;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.lifecycle.ReflectionLifecycleStrategy;
import org.picocontainer.lifecycle.StartableLifecycleStrategy;
import org.junit.Test;

import java.lang.reflect.Method;

@SuppressWarnings("serial")
public class Issue0303TestCase {
    
	public static class SwallowingComponentMonitor extends NullComponentMonitor {
		@Override
		public void lifecycleInvocationFailed(MutablePicoContainer container,
				ComponentAdapter<?> componentAdapter, Method method, Object instance,
				RuntimeException cause) {
			// swallow it
		}
	}

	public static class Starter implements Startable {

		public void start() {
			throw new RuntimeException("deliberate exception");
		}

		/**
		 * {@inheritDoc}
		 */
		public void stop() {
			// empty
		}
	}

	@Test
	public void testCanSwallowExceptionFromReflectionLifecycleStrategy() {
		ComponentMonitor monitor = new SwallowingComponentMonitor();
		DefaultPicoContainer container =
				new DefaultPicoContainer(monitor, new StartableLifecycleStrategy(monitor), null);
		container.addComponent(new Starter());
		container.start();
	}

	@Test
	// @Ignore("filed as PICO-313 on jira.codehaus.org")
	public void testCanSwallowExceptionFromStarableLifecycleStrategy() {
		ComponentMonitor monitor = new SwallowingComponentMonitor();
		DefaultPicoContainer container =
				new DefaultPicoContainer(monitor, new ReflectionLifecycleStrategy(monitor), null);
		container.addComponent(new Starter());
		container.start();
	}
}
