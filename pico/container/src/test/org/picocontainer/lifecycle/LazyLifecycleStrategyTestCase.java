package org.picocontainer.lifecycle;

import static org.junit.Assert.assertEquals;
import static org.picocontainer.Characteristics.CACHE;

import org.junit.Test;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.Disposable;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Startable;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.monitors.NullComponentMonitor;

public class LazyLifecycleStrategyTestCase {

    @Test
    public void testStartStopAndDisposeCanBeLazy() {
        final StringBuilder sb = new StringBuilder();
        MutablePicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new StartableLifecycleStrategy(new NullComponentMonitor()) {
            @Override
            public boolean isLazy(final ComponentAdapter<?> adapter) {
                return true;
            }
        });
        pico.addComponent(sb);
        pico.as(CACHE).addComponent(MyStartableComp.class);
        pico.start();
        assertEquals("", sb.toString()); // normally would be "<" here
        pico.getComponent(MyStartableComp.class);
        pico.getComponent(MyStartableComp.class);
        assertEquals("<", sb.toString()); // only one start() issued even if two or more getComponents
        pico.stop();
        assertEquals("<>", sb.toString());
        pico.dispose();
        assertEquals("<>!", sb.toString());
    }

    @Test
    public void testStartStopAndDisposeCanBeLazyWithoutGet() {
        final StringBuilder sb = new StringBuilder();
        MutablePicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new StartableLifecycleStrategy(new NullComponentMonitor()) {
            @Override
            public boolean isLazy(final ComponentAdapter<?> adapter) {
                return true;
            }
        });
        pico.addComponent(sb);
        pico.as(CACHE).addComponent(MyStartableComp.class);
        pico.start();
        assertEquals("", sb.toString());
        pico.stop();
        assertEquals("", sb.toString());
        pico.dispose();
        assertEquals("", sb.toString());
    }

    @Test
    public void testStartStopAndDisposeCanBeConditionallyLazy() {
        final StringBuilder sb = new StringBuilder();
        MutablePicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new StartableLifecycleStrategy(new NullComponentMonitor()) {
            @Override
            public boolean isLazy(final ComponentAdapter<?> adapter) {
                return adapter.getComponentImplementation() == MyStartableComp.class;
            }
        });
        pico.addComponent(sb);
        pico.as(CACHE).addComponent(MyStartableComp.class);
        pico.as(CACHE).addComponent(MyDifferentStartableComp.class);
        pico.start();
        assertEquals("{", sb.toString()); // one component started, one not
        pico.getComponent(MyStartableComp.class);
        pico.getComponent(MyStartableComp.class); // should not start a second time
        assertEquals("{<", sb.toString()); // both components now started, one lazily.
        pico.stop();
        assertEquals("{<}>", sb.toString());
        pico.dispose();
        assertEquals("{<}>?!", sb.toString());
    }

    public static class MyStartableComp implements Startable, Disposable {
        private final StringBuilder sb;

        public MyStartableComp(final StringBuilder sb) {
            this.sb = sb;
        }

        public void start() {
            sb.append("<");
        }

        public void stop() {
            sb.append(">");
        }

        public void dispose() {
            sb.append("!");
        }
    }

    public static class MyDifferentStartableComp implements Startable, Disposable {
        private final StringBuilder sb;

        public MyDifferentStartableComp(final StringBuilder sb) {
            this.sb = sb;
        }

        public void start() {
            sb.append("{");
        }

        public void stop() {
            sb.append("}");
        }

        public void dispose() {
            sb.append("?");
        }
    }

}
