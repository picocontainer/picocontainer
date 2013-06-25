/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.defaults.issues;

import static org.junit.Assert.assertNotNull;
import static com.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.picocontainer.DefaultPicoContainerTestCase;

import com.picocontainer.ChangedBehavior;
import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.PicoContainer;
import com.picocontainer.Startable;
import com.picocontainer.injectors.AbstractInjector;

@RunWith(JMock.class)
public class Issue0265TestCase {

	private final Mockery mockery = mockeryWithCountingNamingScheme();

    @Test public void testCanReallyChangeMonitor() throws SecurityException, NoSuchMethodException {
        final Method start = Startable.class.getMethod("start");
        final Method stop = Startable.class.getMethod("stop");
        final ComponentMonitor monitor1 = mockery.mock(ComponentMonitor.class, "Monitor1");
        final ComponentMonitor monitor2 = mockery.mock(ComponentMonitor.class, "Monitor2");
        DefaultPicoContainer pico = new DefaultPicoContainer(monitor1);
        mockery.checking(new Expectations() {{
            one(monitor1).changedBehavior(with(any(ChangedBehavior.class)));
            will(returnParameterAction(0));
            allowing(monitor1).newInjector(with(any(AbstractInjector.class)));
            will(returnParameterAction(0));
            one(monitor1).instantiating(with(any(PicoContainer.class)), with(any(ComponentAdapter.class)), with(any(Constructor.class)));
            will(returnValue(DefaultPicoContainerTestCase.MyStartable.class.getConstructor()));
            one(monitor1).instantiated(with(any(PicoContainer.class)), with(any(ComponentAdapter.class)), with(any(Constructor.class)),
            		with(any(Object.class)), with(any(Object[].class)), with(any(Long.class)));
            one(monitor1).invoking(with(any(PicoContainer.class)), with(any(ComponentAdapter.class)), with(equal(start)),
            		with(any(Object.class)), with(any(Object[].class)));
            one(monitor1).invoked(with(any(PicoContainer.class)), with(any(ComponentAdapter.class)), with(equal(start)),
            		with(any(Object.class)), with(any(Long.class)), with(same(null)), with(any(Object[].class)));
            one(monitor1).invoking(with(any(PicoContainer.class)), with(any(ComponentAdapter.class)), with(equal(stop)),
            		with(any(Object.class)), with(any(Object[].class)));
            one(monitor1).invoked(with(any(PicoContainer.class)), with(any(ComponentAdapter.class)), with(equal(stop)),
            		with(any(Object.class)), with(any(Long.class)), with(same(null)), with(any(Object[].class)));
        }});
        pico.as(Characteristics.CACHE).addComponent(DefaultPicoContainerTestCase.MyStartable.class);
        pico.start();
        pico.stop();
        Startable startable = pico.getComponent(DefaultPicoContainerTestCase.MyStartable.class);
        assertNotNull(startable);
        pico.changeMonitor(monitor2);
        mockery.checking(new Expectations() {{
            one(monitor2).invoking(with(any(PicoContainer.class)), with(any(ComponentAdapter.class)), with(equal(start)),
            		with(any(Object.class)), with(any(Object[].class)));
            one(monitor2).invoked(with(any(PicoContainer.class)), with(any(ComponentAdapter.class)), with(equal(start)),
            		with(any(Object.class)), with(any(Long.class)), with(same(null)), with(any(Object[].class)));
            one(monitor2).invoking(with(any(PicoContainer.class)), with(any(ComponentAdapter.class)), with(equal(stop)),
            		with(any(Object.class)), with(any(Object[].class)));
            one(monitor2).invoked(with(any(PicoContainer.class)), with(any(ComponentAdapter.class)), with(equal(stop)),
            		with(any(Object.class)), with(any(Long.class)), with(same(null)), with(any(Object[].class)));
        }});
        pico.start();
        pico.stop();
    }

    public static Action returnParameterAction(final int param) {
        return new ReturnParameterAction(param);
    }

    public static class ReturnParameterAction implements Action {
        private final int parameter;

        public ReturnParameterAction(final int parameter) {
            this.parameter = parameter;
        }

        public void describeTo(final Description description) {
            description.appendText("returns param[")
                    .appendValue(parameter)
                    .appendText("]");
        }

        public Object invoke(final Invocation invocation) {
            return invocation.getParameter(parameter);
        }
    }


}
