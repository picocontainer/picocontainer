/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.behaviors;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.testmodel.SimpleTouchable;
import org.picocontainer.testmodel.Touchable;


/**
 * @author Mauro Talevi
 */
@RunWith(JMock.class)
public class CachedTestCase {

	private final Mockery mockery = mockeryWithCountingNamingScheme();

    @Test public void testComponentIsNotStartedWhenCachedAndCanBeStarted() {
        Caching.Cached adapter = new Caching.Cached(
                mockComponentAdapterSupportingLifecycleStrategy(true, false, false, false, false));
        PicoContainer pico = new DefaultPicoContainer();
        adapter.getComponentInstance(pico, ComponentAdapter.NOTHING.class);
        adapter.start(pico);
    }

    @Test public void testComponentCanBeStartedAgainAfterBeingStopped() {
        Caching.Cached adapter = new Caching.Cached(
                mockComponentAdapterSupportingLifecycleStrategy(true, true, false, false, false));
        PicoContainer pico = new DefaultPicoContainer();
        adapter.start(pico);
        Object instanceAfterFirstStart = adapter.getComponentInstance(pico, ComponentAdapter.NOTHING.class);
        adapter.stop(pico);
        adapter.start(pico);
        Object instanceAfterSecondStart = adapter.getComponentInstance(pico, ComponentAdapter.NOTHING.class);
        assertSame(instanceAfterFirstStart, instanceAfterSecondStart);
    }

    @Test public void testComponentCannotBeStartedIfDisposed() {
        Caching.Cached adapter = new Caching.Cached(
                mockComponentAdapterSupportingLifecycleStrategy(false, false, true, true, true));
        PicoContainer pico = new DefaultPicoContainer();
        adapter.getComponentInstance(pico, ComponentAdapter.NOTHING.class);
        adapter.dispose(pico);
        try {
            adapter.start(pico);
            fail("IllegalStateException expected");
        } catch (Exception e) {
            assertEquals("'interface org.picocontainer.testmodel.Touchable' already disposed", e.getMessage());
        }
    }

    @Test public void testComponentDisposedEffectivelyIgnoredIfNotInstantiated() throws NoSuchFieldException, IllegalAccessException {
        Caching.Cached adapter = new Caching.Cached(
                mockComponentAdapterSupportingLifecycleStrategy(false, false, false, false, false));
        PicoContainer pico = new DefaultPicoContainer();
        adapter.dispose(pico);
        assertNull(adapter.getStoredObject());
    }

    @Test public void testComponentCannotBeStartedIfAlreadyStarted() {
        Caching.Cached adapter = new Caching.Cached(
                mockComponentAdapterSupportingLifecycleStrategy(true, false, false, true, false));
        PicoContainer pico = new DefaultPicoContainer();
        adapter.start(pico);
        try {
            adapter.start(pico);
            fail("IllegalStateException expected");
        } catch (Exception e) {
            assertEquals("'interface org.picocontainer.testmodel.Touchable' already started", e.getMessage());
        }
    }

    @Test public void testComponentCannotBeStoppedIfDisposed() {
        Caching.Cached adapter = new Caching.Cached(
                mockComponentAdapterSupportingLifecycleStrategy(false, false, true, true, false));
        PicoContainer pico = new DefaultPicoContainer();
        adapter.getComponentInstance(pico, ComponentAdapter.NOTHING.class);
        adapter.dispose(pico);
        try {
            adapter.stop(pico);
            fail("IllegalStateException expected");
        } catch (Exception e) {
            assertEquals("'interface org.picocontainer.testmodel.Touchable' already disposed", e.getMessage());
        }
    }

    @Test public void testComponentCannotBeStoppedIfNotInstantiated() {
        Caching.Cached adapter = new Caching.Cached(
                mockComponentAdapterSupportingLifecycleStrategy(false, false, false, true, false));
        PicoContainer pico = new DefaultPicoContainer();
        try {
            adapter.stop(pico);
            fail("IllegalStateException expected");
        } catch (Exception e) {
            assertEquals("'interface org.picocontainer.testmodel.Touchable' not instantiated", e.getMessage());
        }
    }

    @Test public void testComponentCannotBeStoppedIfNotStarted() {
        Caching.Cached adapter = new Caching.Cached(
                mockComponentAdapterSupportingLifecycleStrategy(true, true, false, true, false));
        PicoContainer pico = new DefaultPicoContainer();
        adapter.start(pico);
        adapter.stop(pico);
        try {
        adapter.stop(pico);
            fail("IllegalStateException expected");
        } catch (Exception e) {
            assertEquals("'interface org.picocontainer.testmodel.Touchable' not started", e.getMessage());
        }
    }

    @Test public void testComponentCannotBeDisposedIfAlreadyDisposed() {
        Caching.Cached adapter = new Caching.Cached(
                mockComponentAdapterSupportingLifecycleStrategy(true, true, true, true, false));
        PicoContainer pico = new DefaultPicoContainer();
        adapter.start(pico);
        adapter.stop(pico);
        adapter.dispose(pico);
        try {
            adapter.dispose(pico);
            fail("IllegalStateException expected");
        } catch (Exception e) {
            assertEquals("'interface org.picocontainer.testmodel.Touchable' already disposed", e.getMessage());
        }
    }

    @Test public void testComponentIsStoppedAndDisposedIfStartedWhenFlushed() {
        Caching.Cached adapter = new Caching.Cached(
                mockComponentAdapterSupportingLifecycleStrategy(true, true, true, false, false));
        PicoContainer pico = new DefaultPicoContainer();
        adapter.start(pico);
        adapter.flush();
    }

    @Test public void testComponentIsNotStoppedAndDisposedWhenFlushedIfNotStarted() {
        Caching.Cached adapter = new Caching.Cached(
                mockComponentAdapterSupportingLifecycleStrategy(false, false, false, false, false));
        adapter.flush();
    }

    @Test public void testComponentIsNotStoppedAndDisposedWhenFlushedIfDelegateDoesNotSupportLifecycle() {
        Caching.Cached adapter = new Caching.Cached(
                mockComponentAdapterNotSupportingLifecycleStrategy());
        adapter.flush();
    }

    @Test public void testLifecycleIsIgnoredIfDelegateDoesNotSupportIt() {
        Caching.Cached adapter = new Caching.Cached(
                mockComponentAdapterNotSupportingLifecycleStrategy());
        PicoContainer pico = new DefaultPicoContainer();
        adapter.start(pico);
        adapter.stop(pico);
        adapter.dispose(pico);
    }

    @Test public void testCanStopAComponentThatWasNeverStartedBecauseItHasNoLifecycle() {
        MutablePicoContainer pico = new DefaultPicoContainer();

        pico.addComponent(StringBuffer.class);

        pico.start();

        assertNotNull(pico.getComponent(StringBuffer.class));

        pico.stop();
        pico.dispose();
    }

    private ComponentAdapter mockComponentAdapterNotSupportingLifecycleStrategy() {
        return mockery.mock(ComponentAdapter.class);
    }

    private ComponentAdapter mockComponentAdapterSupportingLifecycleStrategy(
            final boolean start, final boolean stop, final boolean dispose, final boolean getKey, final boolean instantiate) {
        final boolean hasLifecycle = start || stop || dispose;
        final ComponentAdapterSupportingLifecycleStrategy ca = mockery.mock(ComponentAdapterSupportingLifecycleStrategy.class);
        mockery.checking(new Expectations() {{
            if (getKey) {
                atLeast(1).of(ca).getComponentKey();
                will(returnValue(Touchable.class));
            }
            if (start) {
                atLeast(1).of(ca).start(with(any(Touchable.class)));
            }
            if (stop) {
                one(ca).stop(with(any(Touchable.class)));
            }
            if (dispose) {
                one(ca).dispose(with(any(Touchable.class)));
            }
            if (hasLifecycle || instantiate) {
            	one(ca).getComponentInstance(with(any(PicoContainer.class)), with(same(ComponentAdapter.NOTHING.class)));
            	will(returnValue(new SimpleTouchable()));
            }
            one(ca).getComponentImplementation();
            will(returnValue(SimpleTouchable.class));
            one(ca).hasLifecycle(with(same(SimpleTouchable.class)));
            will(returnValue(true));
        }});
        return ca;
    }

    public static interface ComponentAdapterSupportingLifecycleStrategy extends ComponentAdapter,
            LifecycleStrategy {
    }
}