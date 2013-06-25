package com.picocontainer.lifecycle;

import static com.picocontainer.Characteristics.CACHE;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.containers.EmptyPicoContainer;
import com.picocontainer.lifecycle.CompositeLifecycleStrategy;
import com.picocontainer.lifecycle.StartableLifecycleStrategy;
import com.picocontainer.monitors.NullComponentMonitor;

public class CompositeLifecycleStrategyTestCase {

    @Test
    public void testMixOfThirdPartyAndBuiltInStartableAndDisposable() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new CompositeLifecycleStrategy(
                    new MyStartableLifecycleStrategy(),
                    new StartableLifecycleStrategy(new NullComponentMonitor()))
       );
        StringBuilder sb = new StringBuilder();
        pico.addComponent(sb);
        pico.as(CACHE).addComponent(ThirdPartyStartableComponent.class);
        pico.as(CACHE).addComponent(BuiltInStartableComponent.class);
        pico.start();
        pico.stop();
        pico.dispose();
        assertEquals("<<>>!!", sb.toString());
    }

}
