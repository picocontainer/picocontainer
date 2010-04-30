/*
 * Copyright (C) 2003-2010 2005 J&ouml;rg Schaible
 * Created on 29.08.2005 by J&ouml;rg Schaible
 */
package org.picocontainer.gems.behaviors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.Before;
import org.picocontainer.Behaved;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.NameBinding;
import org.picocontainer.injectors.ConstructorInjector;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.tck.AbstractComponentAdapterTest;
import org.picocontainer.testmodel.RecordingLifecycle;
import org.picocontainer.testmodel.SimpleTouchable;
import org.picocontainer.testmodel.Touchable;

import com.thoughtworks.proxy.ProxyFactory;
import com.thoughtworks.proxy.factory.CglibProxyFactory;
import com.thoughtworks.proxy.toys.pool.Poolable;


/**
 * @author J&ouml;rg Schaible
 */
@SuppressWarnings("serial")
public final class PooledTestCase extends AbstractComponentAdapterTest{

    public static interface Identifiable {
        int getId();
    }

    public static final class InstanceCounter implements Identifiable, Serializable {

		private static int counter = 0;
        private final int id;

        public InstanceCounter() {
            id = counter++;
        }

        public int getId() {
            return id;
        }

        @Override
		public boolean equals(final Object arg) {
            return arg instanceof Identifiable && id == ((Identifiable)arg).getId();
        }
    }

    @Before
    public void setUp() throws Exception {
        InstanceCounter.counter = 0;
    }

    @Test
    public void testNewIsInstantiatedOnEachRequest() {
        ComponentAdapter componentAdapter = new Pooled(new ConstructorInjector(
                Identifiable.class, InstanceCounter.class, null, new NullComponentMonitor(), false), new Pooled.DefaultContext());

        Object borrowed0 = componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);
        Object borrowed1 = componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);

        assertNotSame(borrowed0, borrowed1);
    }

    @Test
    public void testInstancesAreDifferent() throws InterruptedException {
        ComponentAdapter componentAdapter = new Pooled(new ConstructorInjector(
                Identifiable.class, InstanceCounter.class, null, new NullComponentMonitor(), false), new Pooled.DefaultContext());

        Object borrowed0 = componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);
        Object borrowed1 = componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);
        Object borrowed2 = componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);

        assertNotSame(borrowed0, borrowed1);
        assertNotSame(borrowed1, borrowed2);
    }

    @Test
    public void testInstancesCanBeRecycled() throws InterruptedException {
        ComponentAdapter componentAdapter = new Pooled(new ConstructorInjector(
                Identifiable.class, InstanceCounter.class, null, new NullComponentMonitor(), false), new Pooled.DefaultContext());

        Identifiable borrowed = (Identifiable)componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);
        assertEquals(0, borrowed.getId());

        ((Poolable)borrowed).returnInstanceToPool();

        Object borrowedReloaded = componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);
        assertEquals(borrowed, borrowedReloaded);
    }

    @Test
    public void testBlocksWhenExhausted() throws InterruptedException {
        final ComponentAdapter componentAdapter = new Pooled(new ConstructorInjector(
                Identifiable.class, InstanceCounter.class, null, new NullComponentMonitor(), false), new Pooled.DefaultContext() {
            @Override
			public int getMaxSize() {
                return 2;
            }

            @Override
			public int getMaxWaitInMilliseconds() {
                return 3000;
            }
        });

        final Identifiable[] borrowed = new Identifiable[3];
        final Throwable[] threadException = new Throwable[2];

        final StringBuffer order = new StringBuffer();
        final Thread returner = new Thread() {
            @Override
			public void run() {
                try {
                    synchronized (this) {
                        notifyAll();
                        wait(200); // ensure, that main thread is blocked
                    }
                    order.append("returner ");
                    ((Poolable)borrowed[0]).returnInstanceToPool();
                } catch (Throwable t) {
                    t.printStackTrace();
                    synchronized (componentAdapter) {
                        componentAdapter.notify();
                    }
                    threadException[1] = t;
                }
            }
        };

        borrowed[0] = (Identifiable)componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);
        borrowed[1] = (Identifiable)componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);
        synchronized (returner) {
            returner.start();
            returner.wait();
        }

        // should block
        order.append("main ");
        borrowed[2] = (Identifiable)componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);
        order.append("main");

        returner.join();

        assertNull(threadException[0]);
        assertNull(threadException[1]);

        assertEquals("main returner main", order.toString());

        assertEquals(borrowed[0].getId(), borrowed[2].getId());
        assertFalse(borrowed[1].getId() == borrowed[2].getId());
    }

    @Test
    public void testTimeoutWhenExhausted() {
        final ComponentAdapter componentAdapter = new Pooled(new ConstructorInjector(
                Identifiable.class, InstanceCounter.class, null, new NullComponentMonitor(), false), new Pooled.DefaultContext() {
            @Override
			public int getMaxSize() {
                return 2;
            }

            @Override
			public int getMaxWaitInMilliseconds() {
                return 250;
            }
        });

        Identifiable borrowed0 = (Identifiable)componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);
        Identifiable borrowed1 = (Identifiable)componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);
        assertNotNull(borrowed0);
        assertFalse(borrowed0.getId() == borrowed1.getId());
        long time = System.currentTimeMillis();
        try {
            componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);
            fail("Thrown " + Pooled.PoolException.class.getName() + " expected");
        } catch (final Pooled.PoolException e) {
            assertTrue(e.getMessage().indexOf("Time out") >= 0);
            assertTrue(System.currentTimeMillis() - time >= 250);
        }
    }

    @Test
    public void testGrowsAlways() {
        Pooled behavior = new Pooled(
                new ConstructorInjector("foo", Object.class, null, new NullComponentMonitor(), false),
                new Pooled.DefaultContext() {

                    @Override
					public ProxyFactory getProxyFactory() {
                        return new CglibProxyFactory();
                    }
                });

        final Set<Object> set = new HashSet<Object>();
        try {
            final int max = 5;
            int i;
            for (i = 0; i < max; ++i) {
                assertEquals(i, behavior.size());
                final Object object = behavior.getComponentInstance(null, ComponentAdapter.NOTHING.class);
                set.add(object);
            }
            assertEquals(i, behavior.size());
            assertEquals(i, set.size());

            for (Object aSet : set) {
                Poolable object = (Poolable)aSet;
                object.returnInstanceToPool();
                assertEquals(max, behavior.size());
            }

            for (i = 0; i < max; ++i) {
                assertEquals(max, behavior.size());
                final Object object = behavior.getComponentInstance(null, ComponentAdapter.NOTHING.class);
                assertNotNull(object);
                set.add(object);
            }

            assertEquals(max, set.size());

        } catch (Pooled.PoolException e) {
            fail("This pool should not get exhausted.");
        }
    }

    @Test
    public void testFailsWhenExhausted() {
        final Pooled behavior = new Pooled(
                new ConstructorInjector(Identifiable.class, InstanceCounter.class, null, new NullComponentMonitor(), false),
                new Pooled.DefaultContext() {
                    @Override
					public int getMaxSize() {
                        return 2;
                    }
                });

        assertEquals(0, behavior.size());
        Identifiable borrowed0 = (Identifiable)behavior.getComponentInstance(null, ComponentAdapter.NOTHING.class);
        assertEquals(1, behavior.size());
        Identifiable borrowed1 = (Identifiable)behavior.getComponentInstance(null, ComponentAdapter.NOTHING.class);
        assertEquals(2, behavior.size());
        try {
            behavior.getComponentInstance(null, ComponentAdapter.NOTHING.class);
            fail("Expected ExhaustedException, pool shouldn't be able to grow further.");
        } catch (Pooled.PoolException e) {
            assertTrue(e.getMessage().indexOf("exhausted") >= 0);
        }

        assertFalse(borrowed0.getId() == borrowed1.getId());
    }

    @Test
    public void testInternalGCCall() {
        ComponentAdapter componentAdapter = new Pooled(new ConstructorInjector(
                Identifiable.class, InstanceCounter.class, null, new NullComponentMonitor(), false), new Pooled.DefaultContext() {
            @Override
			public int getMaxSize() {
                return 1;
            }

            @Override
			public boolean autostartGC() {
                return true;
            }
        });

        for (int i = 0; i < 5; i++) {
            final Identifiable borrowed = (Identifiable)componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);
            assertNotNull(borrowed);
            assertEquals(0, borrowed.getId());
        }
    }

    /**
     * Prepare the test <em>lifecycleManagerSupport</em>. Prepare the delivered PicoContainer with an adapter, that
     * has a lifecycle and use a StringBuffer registered in the container to record the lifecycle method invocations.
     * The recorded String in the buffer must result in <strong>&qout;&lt;OneOne&gt;!One&qout;</strong>. The addAdapter
     * top test should be registered in the container and delivered as return value.
     * @param picoContainer the container
     * @return the adapter to test
     */
    private ComponentAdapter prepDEF_lifecycleManagerSupport(final MutablePicoContainer picoContainer) {
        picoContainer.addComponent(RecordingLifecycle.One.class);
        Pooled poolingBehavior = new Pooled(new ConstructorInjector(
                RecordingLifecycle.Recorder.class, RecordingLifecycle.Two.class, null, new NullComponentMonitor(), false), new Pooled.DefaultContext());
        return picoContainer.addAdapter(poolingBehavior).getComponentAdapter(RecordingLifecycle.One.class, (NameBinding) null);
    }

    @Test
    public void testDEF_lifecycleManagerSupport() {
        if ((getComponentAdapterNature() & RESOLVING) > 0) {
            final Class type = getComponentAdapterType();
            if (Behaved.class.isAssignableFrom(type)) {
                final StringBuffer buffer = new StringBuffer();
                final MutablePicoContainer picoContainer = new DefaultPicoContainer(
                        createDefaultComponentFactory());
                picoContainer.addComponent(buffer);
                final ComponentAdapter componentAdapter = prepDEF_lifecycleManagerSupport(picoContainer);
                assertSame(getComponentAdapterType(), componentAdapter.getClass());
                assertEquals(0, buffer.length());
                picoContainer.start();
                picoContainer.stop();
                picoContainer.dispose();
                // TODO Move test to AbstractAbstractCATC
                assertEquals("<OneOne>!One", buffer.toString());
            }
        }
    }

    /**
     * Prepare the test <em>lifecycleManagerHonorsInstantiationSequence</em>. Prepare the delivered PicoContainer
     * with addAdapter(s), that have dependend components, have a lifecycle and use a StringBuffer registered in the
     * container to record the lifecycle method invocations. The recorded String in the buffer must result in
     * <strong>&qout;&lt;One&lt;TwoTwo&gt;One&gt;!Two!One&qout;</strong>. The adapter top test should be registered in
     * the container and delivered as return value.
     * @param picoContainer the container
     * @return the adapter to test
     */
    private ComponentAdapter prepRES_lifecycleManagerHonorsInstantiationSequence(final MutablePicoContainer picoContainer) {
        picoContainer.addComponent(RecordingLifecycle.One.class);
        Pooled poolingBehavior = new Pooled(new ConstructorInjector(
                RecordingLifecycle.Recorder.class, RecordingLifecycle.Two.class, null, new NullComponentMonitor(), false), new Pooled.DefaultContext());
        return picoContainer.addAdapter(poolingBehavior).getComponentAdapter(RecordingLifecycle.Two.class, (NameBinding) null);
    }

    @Test
    public void testRES_lifecycleManagerHonorsInstantiationSequence() {
        if ((getComponentAdapterNature() & RESOLVING) > 0) {
            final Class type = getComponentAdapterType();
            if (Behaved.class.isAssignableFrom(type)) {
                final StringBuffer buffer = new StringBuffer();
                final MutablePicoContainer picoContainer = new DefaultPicoContainer(
                        createDefaultComponentFactory());
                picoContainer.addComponent(buffer);
                final ComponentAdapter componentAdapter = prepRES_lifecycleManagerHonorsInstantiationSequence(picoContainer);
                assertSame(getComponentAdapterType(), componentAdapter.getClass());
                assertEquals(0, buffer.length());
                picoContainer.start();
                picoContainer.stop();
                picoContainer.dispose();
                // TODO Move test to AbstractAbstractCATC
                assertEquals("<One<TwoTwo>One>!Two!One", buffer.toString());
            }
        }
    }

    // -------- TCK -----------

    @Override
	protected Class getComponentAdapterType() {
        return Pooled.class;
    }

    @Override
	protected int getComponentAdapterNature() {
        return super.getComponentAdapterNature() & ~(INSTANTIATING | RESOLVING | VERIFYING);
    }

    private ComponentAdapter createPoolOfTouchables() {
        return new Pooled(new ConstructorInjector(
                Touchable.class, SimpleTouchable.class, null, new NullComponentMonitor(), false), new Pooled.DefaultContext());
    }

    @Override
	protected ComponentAdapter prepDEF_verifyWithoutDependencyWorks(final MutablePicoContainer picoContainer) {
        return createPoolOfTouchables();
    }

    @Override
	protected ComponentAdapter prepDEF_verifyDoesNotInstantiate(final MutablePicoContainer picoContainer) {
        return createPoolOfTouchables();
    }

    @Override
	protected ComponentAdapter prepDEF_visitable() {
        return createPoolOfTouchables();
    }

    private ComponentAdapter createSerializable() {
        return new Pooled(new ConstructorInjector(
                Identifiable.class, InstanceCounter.class, null, new NullComponentMonitor(), false), new Pooled.DefaultContext());
    }

    @Override
	protected ComponentAdapter prepSER_isSerializable(final MutablePicoContainer picoContainer) {
        return createSerializable();
    }

    @Override
	protected ComponentAdapter prepSER_isXStreamSerializable(final MutablePicoContainer picoContainer) {
        return createSerializable();
    }

}
