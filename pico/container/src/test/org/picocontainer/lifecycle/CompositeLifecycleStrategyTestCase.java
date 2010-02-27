package org.picocontainer.lifecycle;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.picocontainer.DefaultPicoContainer;
import static org.picocontainer.Characteristics.CACHE;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.monitors.NullComponentMonitor;

public class CompositeLifecycleStrategyTestCase {
    
    @Test
    public void testMixOfThirdPartyAndBuiltInStartableAndDisposable() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new CompositeLifecycleStrategy(
                    new MyStartableLifecycleStrategy(),
                    new StartableLifecycleStrategy(new NullComponentMonitor())),
                new EmptyPicoContainer());
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
