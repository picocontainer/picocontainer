package org.picocontainer.behaviors;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.injectors.SetterInjection;
import org.picocontainer.lifecycle.NullLifecycleStrategy;

public class CircularTestCase {

    public static interface IFish {
        IWater getWater();
    }
    public static class Fish implements IFish {
        IWater water;

        public void setWater(final IWater water) {
            this.water = water;
        }

        public IWater getWater() {
            return water;
        }
    }

    public static interface IWater {
        IFish getFish();

    }
    public static class Water implements IWater {
        IFish fish;

        public void setFish(final IFish fish) {
            this.fish = fish;
        }

        public IFish getFish() {
            return fish;
        }
    }

    @Test
    public void circularIsPossibleWithACharacteristic() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new Caching().wrap(new SetterInjection()));
        pico.as(Characteristics.ENABLE_CIRCULAR).addComponent(IFish.class, Fish.class);
        pico.addComponent(IWater.class, Water.class);
        IWater water = pico.getComponent(IWater.class);
        IFish fish = pico.getComponent(IFish.class);
        assertNotNull(water.getFish());
        assertNotNull(fish.getWater());
    }

    @Test
    public void enableCircularCharacteristicIsRedundantForImplementationHiding() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new ImplementationHiding().wrap(new SetterInjection()));
        pico.addComponent(IFish.class, Fish.class);
        pico.addComponent(IWater.class, Water.class);
        IWater water = pico.getComponent(IWater.class);
        IFish fish = pico.getComponent(IFish.class);
        assertNotNull(water.getFish());
        assertNotNull(fish.getWater());

        ComponentAdapter<?> ca = pico.getComponentAdapter(IFish.class);
        assertEquals("Hidden:SetterInjector-" + IFish.class,ca.toString());

    }


}
