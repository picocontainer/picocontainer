/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package com.picocontainer.lifecycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static com.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.Disposable;
import com.picocontainer.PicoContainer;
import com.picocontainer.Startable;
import com.picocontainer.lifecycle.ReflectionLifecycleStrategy;
import com.picocontainer.monitors.NullComponentMonitor;

/**
 * @author Paul Hammant
 * @author Mauro Talevi
 * @author J&ouml;rg Schaible
 */
@RunWith(JMock.class)
public class ReflectionLifecycleStrategyTestCase {

	private final Mockery mockery = mockeryWithCountingNamingScheme();

	private ReflectionLifecycleStrategy strategy;
	private ComponentMonitor monitor;

	@Before
	public void setUp() {
		monitor = mockery.mock(ComponentMonitor.class);
		strategy = new ReflectionLifecycleStrategy(monitor);
	}

	@Test
	public void testStartable() {
		Object startable = mockComponent(true, false);
		strategy.start(startable);
		strategy.stop(startable);
		strategy.dispose(startable);
	}

	@Test
	public void testDisposable() {
		Object disposable = mockComponent(false, true);
		strategy.start(disposable);
		strategy.stop(disposable);
		strategy.dispose(disposable);
	}

	@Test
	public void testNotStartableNorDisposable() {
		Object serializable = mockery.mock(Serializable.class);
		assertFalse(strategy.hasLifecycle(serializable.getClass()));
		strategy.start(serializable);
		strategy.stop(serializable);
		strategy.dispose(serializable);
	}

	@Test
	public void testStartableBarfingWithError() {
        try {
            new ReflectionLifecycleStrategy(new NullComponentMonitor()).start(new Object() {
                public void start() throws InvocationTargetException {
                    throw new NoClassDefFoundError("foo");
                }
            });
        } catch (Exception e) {
            System.out.println("");
        }
    }

	@Test
	public void testMonitorChanges() {
		final ComponentMonitor monitor2 = mockery
				.mock(ComponentMonitor.class);
		final Disposable disposable = mockery.mock(Disposable.class);
		final Matcher<Member> isDisposeMember = new IsMember("dispose");
		final Matcher<Method> isDisposeMethod = new IsMethod("dispose");
		mockery.checking(new Expectations() {
			{
				atLeast(1).of(disposable).dispose();
				one(monitor).invoking(
						with(aNull(PicoContainer.class)),
						with(aNull(ComponentAdapter.class)),
						with(isDisposeMember), with(same(disposable)), with(any(Object[].class)));
				one(monitor).invoked(with(aNull(PicoContainer.class)),
						with(aNull(ComponentAdapter.class)),
						with(isDisposeMethod), with(same(disposable)),
                        with(any(Long.class)), with(same(null)), with(any(Object[].class)));
				one(monitor2).invoking(
						with(aNull(PicoContainer.class)),
						with(aNull(ComponentAdapter.class)),
						with(isDisposeMember), with(same(disposable)), with(any(Object[].class)));
				one(monitor2).invoked(
						with(aNull(PicoContainer.class)),
						with(aNull(ComponentAdapter.class)),
						with(isDisposeMethod), with(same(disposable)),
                        with(any(Long.class)), with(same(null)), with(any(Object[].class)));
			}
		});
		strategy.dispose(disposable);
		strategy.changeMonitor(monitor2);
		strategy.dispose(disposable);
	}

	@Test
	public void testWithDifferentTypes() {
		final MyLifecycle lifecycle = mockery.mock(MyLifecycle.class);
		final Matcher<Member> isStartMember = new IsMember("start");
		final Matcher<Method> isStartMethod = new IsMethod("start");
		final Matcher<Member> isStopMember = new IsMember("stop");
		final Matcher<Method> isStopMethod = new IsMethod("stop");
		final Matcher<Member> isDisposeMember = new IsMember("dispose");
		final Matcher<Method> isDisposeMethod = new IsMethod("dispose");
		mockery.checking(new Expectations() {
			{
				one(lifecycle).start();
				one(lifecycle).stop();
				one(lifecycle).dispose();
				one(monitor).invoking(
						with(aNull(PicoContainer.class)),
						with(aNull(ComponentAdapter.class)),
						with(isStartMember), with(same(lifecycle)), with(any(Object[].class)));
				one(monitor).invoked(with(aNull(PicoContainer.class)),
						with(aNull(ComponentAdapter.class)),
						with(isStartMethod), with(same(lifecycle)),
                        with(any(Long.class)), with(same(null)), with(any(Object[].class)));
				one(monitor).invoking(
						with(aNull(PicoContainer.class)),
						with(aNull(ComponentAdapter.class)),
						with(isStopMember), with(same(lifecycle)), with(any(Object[].class)));
				one(monitor).invoked(with(aNull(PicoContainer.class)),
						with(aNull(ComponentAdapter.class)),
						with(isStopMethod), with(same(lifecycle)),
                        with(any(Long.class)), with(same(null)), with(any(Object[].class)));
				one(monitor).invoking(
						with(aNull(PicoContainer.class)),
						with(aNull(ComponentAdapter.class)),
						with(isDisposeMember), with(same(lifecycle)), with(any(Object[].class)));
				one(monitor).invoked(with(aNull(PicoContainer.class)),
						with(aNull(ComponentAdapter.class)),
						with(isDisposeMethod), with(same(lifecycle)),
                        with(any(Long.class)), with(same(null)), with(any(Object[].class)));
			}
		});

		Object startable = mockComponent(true, false);
		strategy.start(startable);
		strategy.stop(startable);
		strategy.dispose(startable);
		startable = lifecycle;
		strategy.start(startable);
		strategy.stop(startable);
		strategy.dispose(startable);
	}

	public static class DummyLifecycle implements MyLifecycle {

		public int startCount = 0;
		public int stopCount = 0;
		public int disposeCount = 0;

		public void start() {
			startCount++;
		}

		public void stop() {
			stopCount++;
		}

		public void dispose() {
			disposeCount++;
		}

	}

	/**
	 * <a href="http://jira.codehaus.org/browse/PICO-379">PICO-379</a>
	 */
	@Test
	public void testNullArgsResultsInNoExecution() {
		//Control Test -- verify model
		monitor = new NullComponentMonitor();
		strategy = new ReflectionLifecycleStrategy(monitor, "start", "stop", "dispose");
		DummyLifecycle dummyTest = new DummyLifecycle();
		strategy.start(dummyTest);
		assertEquals(1, dummyTest.startCount);
		strategy.stop(dummyTest);
		assertEquals(1, dummyTest.stopCount);
		strategy.dispose(dummyTest);
		assertEquals(1, dummyTest.disposeCount);

		//What we want test.
		strategy = new ReflectionLifecycleStrategy(monitor, null, "stop", null);
		dummyTest = new DummyLifecycle();
		strategy.start(dummyTest);
		assertEquals(0, dummyTest.startCount);
		strategy.stop(dummyTest);
		assertEquals(1, dummyTest.stopCount);
		strategy.dispose(dummyTest);
		assertEquals(0, dummyTest.disposeCount);

		strategy = new ReflectionLifecycleStrategy(monitor, null, null, "dispose");
		dummyTest = new DummyLifecycle();
		strategy.start(dummyTest);
		assertEquals(0, dummyTest.startCount);
		strategy.stop(dummyTest);
		assertEquals(0, dummyTest.stopCount);
		strategy.dispose(dummyTest);
		assertEquals(1, dummyTest.disposeCount);
	}

	private Object mockComponent(final boolean startable, final boolean disposable) {
		final Matcher<Member> isStartMember = new IsMember("start");
		final Matcher<Method> isStartMethod = new IsMethod("start");
		final Matcher<Member> isStopMember = new IsMember("stop");
		final Matcher<Method> isStopMethod = new IsMethod("stop");
		final Matcher<Member> isDisposeMember = new IsMember("dispose");
		final Matcher<Method> isDisposeMethod = new IsMethod("dispose");
		if (startable) {
			final Startable mock = mockery.mock(Startable.class);
			mockery.checking(new Expectations() {
				{
					atLeast(1).of(mock).start();
					atLeast(1).of(mock).stop();
					one(monitor).invoking(
							with(aNull(PicoContainer.class)),
							with(aNull(ComponentAdapter.class)),
							with(isStartMember), with(same(mock)), with(any(Object[].class)));
					one(monitor)
							.invoked(with(aNull(PicoContainer.class)),
									with(aNull(ComponentAdapter.class)),
									with(isStartMethod), with(same(mock)),
                                    with(any(Long.class)), with(same(null)), with(any(Object[].class)));
					one(monitor).invoking(
							with(aNull(PicoContainer.class)),
							with(aNull(ComponentAdapter.class)),
							with(isStopMember), with(same(mock)), with(any(Object[].class)));
					one(monitor).invoked(
							with(aNull(PicoContainer.class)),
							with(aNull(ComponentAdapter.class)),
							with(isStopMethod), with(same(mock)), with(any(Long.class)), with(same(null)), with(any(Object[].class)));
				}
			});
			return mock;
		}
		if (disposable) {
			final Disposable mock = mockery.mock(Disposable.class);
			mockery.checking(new Expectations() {
				{
					atLeast(1).of(mock).dispose();
					one(monitor).invoking(
							with(aNull(PicoContainer.class)),
							with(aNull(ComponentAdapter.class)),
							with(isDisposeMember), with(same(mock)), with(any(Object[].class)));
					one(monitor)
							.invoked(with(aNull(PicoContainer.class)),
									with(aNull(ComponentAdapter.class)),
									with(isDisposeMethod), with(same(mock)),
                                    with(any(Long.class)), with(same(null)), with(any(Object[].class)));
				}
			});
			return mock;
		}
		return mockery.mock(Serializable.class);
	}

	public static interface MyLifecycle {
		void start();

		void stop();

		void dispose();
	}

	static class IsMember extends BaseMatcher<Member> {
		private final String name;

		public IsMember(final String name) {
			this.name = name;
		}

		public boolean matches(final Object item) {
			return ((Member) item).getName().equals(name);
		}

		public void describeTo(final Description description) {
			description.appendText("Should have been a member of name ");
			description.appendText(name);
		}
	};

	static class IsMethod extends BaseMatcher<Method> {
		private final String name;

		public IsMethod(final String name) {
			this.name = name;
		}

		public boolean matches(final Object item) {
			return ((Method) item).getName().equals(name);
		}

		public void describeTo(final Description description) {
			description.appendText("Should have been a method of name ");
			description.appendText(name);
		}
	};

}
