/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.defaults;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Vector;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.ComponentMonitorStrategy;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.monitors.AbstractComponentMonitor;

/**
 * @author Mauro Talevi
 */
@RunWith(JMock.class)
public class AbstractComponentMonitorTestCase {

	private final Mockery mockery = mockeryWithCountingNamingScheme();

    @Test public void testDelegatingMonitorThrowsExpectionWhenConstructionWithNullDelegate() {
        try {
            new AbstractComponentMonitor(null);
            fail("NPE expected");
        } catch (NullPointerException e) {
            assertEquals("NPE", "monitor", e.getMessage());
        }
    }

    @Test public void testDelegatingMonitorThrowsExpectionWhenChangingToNullMonitor() {
        AbstractComponentMonitor dcm = new AbstractComponentMonitor();
        try {
            dcm.changeMonitor(null);
            fail("NPE expected");
        } catch (NullPointerException e) {
            assertEquals("NPE", "monitor", e.getMessage());
        }
    }

    @Test public void testDelegatingMonitorCanChangeMonitorInDelegateThatDoesSupportMonitorStrategy() {
        ComponentMonitor monitor = mockMonitorWithNoExpectedMethods();
        AbstractComponentMonitor dcm = new AbstractComponentMonitor(mockMonitorThatSupportsStrategy(monitor));
        dcm.changeMonitor(monitor);
        assertEquals(monitor, dcm.currentMonitor());
        dcm.instantiating(null, null, null);
    }

    @Test public void testDelegatingMonitorChangesDelegateThatDoesNotSupportMonitorStrategy() {
        ComponentMonitor delegate = mockMonitorWithNoExpectedMethods();
        AbstractComponentMonitor dcm = new AbstractComponentMonitor(delegate);
        ComponentMonitor monitor = mockMonitorWithNoExpectedMethods();
        assertEquals(delegate, dcm.currentMonitor());
        dcm.changeMonitor(monitor);
        assertEquals(monitor, dcm.currentMonitor());
    }

    @Test public void testDelegatingMonitorReturnsDelegateThatDoesNotSupportMonitorStrategy() {
        ComponentMonitor delegate = mockMonitorWithNoExpectedMethods();
        AbstractComponentMonitor dcm = new AbstractComponentMonitor(delegate);
        assertEquals(delegate, dcm.currentMonitor());
    }

    private ComponentMonitor mockMonitorWithNoExpectedMethods() {
        return mockery.mock(ComponentMonitor.class);
    }

    private ComponentMonitor mockMonitorThatSupportsStrategy(final ComponentMonitor currentMonitor) {
    	final TestMonitorThatSupportsStrategy monitor = mockery.mock(TestMonitorThatSupportsStrategy.class);
    	mockery.checking(new Expectations() {{
            one(monitor).changeMonitor(with(equal(currentMonitor)));
            one(monitor).currentMonitor();
            will(returnValue(currentMonitor));
            one(monitor).instantiating(with(any(PicoContainer.class)), with(any(ComponentAdapter.class)), with(any(Constructor.class)));
    	}});
        return monitor;
    }

    @Test public void testMonitoringHappensBeforeAndAfterInstantiation() throws NoSuchMethodException {
        final Vector ourIntendedInjectee0 = new Vector();
        final String ourIntendedInjectee1 = "hullo";
        final DefaultPicoContainer parent = new DefaultPicoContainer();
        final ComponentMonitor monitor = mockery.mock(ComponentMonitor.class);
        final DefaultPicoContainer child = new DefaultPicoContainer(parent, new AbstractComponentMonitor(monitor));
        final Constructor needsACoupleOfThings = NeedsACoupleOfThings.class.getConstructors()[0];
        final Matcher<Long> durationIsGreaterThanOrEqualToZero = new BaseMatcher<Long>() {
			public boolean matches(final Object item) {
                Long duration = (Long)item;
                return 0 <= duration;
			}

			public void describeTo(final Description description) {
				description.appendText("The endTime wasn't after the startTime");
			}
        };
        final Matcher<Object> isANACOTThatWozCreated = new BaseMatcher<Object>() {
			public boolean matches(final Object item) {
                return item instanceof NeedsACoupleOfThings;
			}

			public void describeTo(final Description description) {
				description.appendText("Should have been a NeedsACoupleOfThings");
			}
        };
        final Matcher<Object[]> collectionAndStringWereInjected = new BaseMatcher<Object[]>() {
			public boolean matches(final Object item) {
				 Object[] args = (Object[]) item;
				 return args.length == 2 && args[0] == ourIntendedInjectee0 && args[1] == ourIntendedInjectee1;
			}
			public void describeTo(final Description description) {
				description.appendText("Should have injected our intended vector and string");
			}
        };
        mockery.checking(new Expectations() {{
        	one(monitor).instantiating(with(same(child)), with(any(ConstructorInjection.ConstructorInjector.class)), with(equal(needsACoupleOfThings)));
        	will(returnValue(needsACoupleOfThings));
        	one(monitor).instantiated(with(same(child)), with(any(ConstructorInjection.ConstructorInjector.class)), with(equal(needsACoupleOfThings)), with(isANACOTThatWozCreated), with(collectionAndStringWereInjected), with(durationIsGreaterThanOrEqualToZero));
            atLeast(2).of(monitor).noComponentFound(with(any(DefaultPicoContainer.class)), with(any(Object.class)));
            will(returnValue(null));
        }});
        parent.addComponent(ourIntendedInjectee0);
        parent.addComponent(ourIntendedInjectee1);
        child.addComponent(NeedsACoupleOfThings.class);
        child.getComponent(NeedsACoupleOfThings.class);
    }

    public static class NeedsACoupleOfThings {
        public NeedsACoupleOfThings(final Collection collection, final String string) {
        }
    }

    public static interface TestMonitorThatSupportsStrategy extends ComponentMonitor, ComponentMonitorStrategy {
    }
}
