/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaible                                           *
 *****************************************************************************/
package com.picocontainer.adapters;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;
import com.picocontainer.tck.AbstractComponentAdapterTest;
import com.picocontainer.testmodel.NullLifecycle;
import com.picocontainer.testmodel.SimpleTouchable;
import com.picocontainer.testmodel.Touchable;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.Disposable;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoContainer;
import com.picocontainer.Startable;
import com.picocontainer.adapters.InstanceAdapter;
import com.picocontainer.lifecycle.NullLifecycleStrategy;
import com.picocontainer.lifecycle.StartableLifecycleStrategy;
import com.picocontainer.monitors.NullComponentMonitor;


/**
 * Test the InstanceAdapter.
 *
 * @author J&ouml;rg Schaible
 */
public final class InstanceAdapterTestCase extends AbstractComponentAdapterTest {

    @Test public void testComponentAdapterReturnsSame() {
        final Touchable touchable = new SimpleTouchable();
        final ComponentAdapter componentAdapter = new InstanceAdapter(Touchable.class, touchable, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor());
        assertSame(touchable, componentAdapter.getComponentInstance(null, null));
    }

    @Test public void testDefaultLifecycleStrategy() {
        LifecycleComponent component = new LifecycleComponent();
        InstanceAdapter adapter =
            new InstanceAdapter(LifecycleComponent.class, component, new StartableLifecycleStrategy(new NullComponentMonitor()),
                                                                        new NullComponentMonitor());
        PicoContainer pico = new DefaultPicoContainer();
        adapter.start(pico);
        adapter.stop(pico);
        adapter.dispose(pico);
        assertEquals("start>stop>dispose>", component.buffer.toString());
        adapter.start(component);
        adapter.stop(component);
        adapter.dispose(component);
        assertEquals("start>stop>dispose>start>stop>dispose>", component.buffer.toString());
    }

    private static final class LifecycleComponent implements Startable, Disposable {
        final StringBuffer buffer = new StringBuffer();

        public void start() {
            buffer.append("start>");
        }

        public void stop() {
            buffer.append("stop>");
        }

        public void dispose() {
            buffer.append("dispose>");
        }
    }

    @Test public void testCustomLifecycleCanBeInjected() {
        NullLifecycle component = new NullLifecycle();
        RecordingLifecycleStrategy strategy = new RecordingLifecycleStrategy(new StringBuffer());
        InstanceAdapter adapter = new InstanceAdapter(NullLifecycle.class, component, strategy, new NullComponentMonitor());
        PicoContainer pico = new DefaultPicoContainer();
        adapter.start(pico);
        adapter.stop(pico);
        adapter.dispose(pico);
        assertEquals("<start<stop<dispose", strategy.recording());
        adapter.start(component);
        adapter.stop(component);
        adapter.dispose(component);
        assertEquals("<start<stop<dispose<start<stop<dispose", strategy.recording());
    }

    @Test public void testComponentAdapterCanIgnoreLifecycle() {
        final Touchable touchable = new SimpleTouchable();
        InstanceAdapter adapter = new InstanceAdapter(Touchable.class, touchable, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor());
        PicoContainer pico = new DefaultPicoContainer();
        adapter.start(pico);
        adapter.stop(pico);
        adapter.dispose(pico);
        adapter.start(touchable);
        adapter.stop(touchable);
        adapter.dispose(touchable);
    }

    @Test public void testGuardAgainstNullInstance() {
        try {
            new InstanceAdapter(Map.class, null, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor());
            fail("should have barfed");
        } catch (NullPointerException e) {
            assertEquals("componentInstance cannot be null", e.getMessage());
        }
    }

    @Test
    public void testFindAdapterOfType() {
    	ComponentAdapter adapter = new InstanceAdapter("test", "test");
    	assertEquals(adapter, adapter.findAdapterOfType(InstanceAdapter.class));
    }


    /**
     * {@inheritDoc}
     * @see com.picocontainer.tck.AbstractComponentAdapterTestCase#getComponentAdapterType()
     */
    @Override
	protected Class getComponentAdapterType() {
        return InstanceAdapter.class;
    }

    /**
     * {@inheritDoc}
     * @see com.picocontainer.tck.AbstractComponentAdapterTestCase#getComponentAdapterNature()
     */
    @Override
	protected int getComponentAdapterNature() {
        return super.getComponentAdapterNature() & ~(RESOLVING | VERIFYING | INSTANTIATING);
    }

    /**
     * {@inheritDoc}
     * @see com.picocontainer.tck.AbstractComponentAdapterTestCase#prepDEF_verifyWithoutDependencyWorks(com.picocontainer.MutablePicoContainer)
     */
    @Override
	protected ComponentAdapter prepDEF_verifyWithoutDependencyWorks(final MutablePicoContainer picoContainer) {
        return new InstanceAdapter("foo", "bar", new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor());
    }

    /**
     * {@inheritDoc}
     * @see com.picocontainer.tck.AbstractComponentAdapterTestCase#prepDEF_verifyDoesNotInstantiate(com.picocontainer.MutablePicoContainer)
     */
    @Override
	protected ComponentAdapter prepDEF_verifyDoesNotInstantiate(
            final MutablePicoContainer picoContainer) {
        return new InstanceAdapter("Key", 4711, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor());
    }

    /**
     * {@inheritDoc}
     * @see com.picocontainer.tck.AbstractComponentAdapterTestCase#prepDEF_visitable()
     */
    @Override
	protected ComponentAdapter prepDEF_visitable() {
        return new InstanceAdapter("Key", 4711, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor());
    }

    /**
     * {@inheritDoc}
     * @see com.picocontainer.tck.AbstractComponentAdapterTestCase#prepSER_isSerializable(com.picocontainer.MutablePicoContainer)
     */
    @Override
	protected ComponentAdapter prepSER_isSerializable(final MutablePicoContainer picoContainer) {
        return new InstanceAdapter("Key", 4711, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor());
    }

    /**
     * {@inheritDoc}
     * @see com.picocontainer.tck.AbstractComponentAdapterTestCase#prepSER_isXStreamSerializable(com.picocontainer.MutablePicoContainer)
     */
    @Override
	protected ComponentAdapter prepSER_isXStreamSerializable(final MutablePicoContainer picoContainer) {
        return new InstanceAdapter("Key", 4711, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor());
    }

}
