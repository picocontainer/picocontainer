package com.picocontainer;

import java.lang.reflect.Field;

import com.picocontainer.containers.ImmutablePicoContainer;
import com.picocontainer.containers.TransientPicoContainer;
import com.picocontainer.injectors.ConstructorInjection;

public class Emjection {

    private PicoContainer pico;

    public void setPico(final ImmutablePicoContainer container) {
        if (pico != null) {
            throw new PicoCompositionException("Emjection can only be setup once per component");
        }
        pico = container;
    }

    public static <T> T neu(final Class<T> type, final Emjection emjection, final Object... args) {
        if (emjection.pico == null) {
            throw new PicoCompositionException("blah");
        }
        TransientPicoContainer tpc = new TransientPicoContainer(new ConstructorInjection(), emjection.pico);
        for (Object arg : args) {
            tpc.addComponent(arg);
        }
        T inst = tpc.getComponentInto(type, ComponentAdapter.NOTHING.class);
        if (inst == null) {
            tpc.addComponent(type);
            inst = tpc.getComponentInto(type, ComponentAdapter.NOTHING.class);
        }
        setPico(inst, tpc);
        return inst;
    }

    private static <T> void setPico(final Object inst, final PicoContainer container) {
        try {
            Field field = inst.getClass().getDeclaredField("emjection");
            field.setAccessible(true);
            Emjection e2 = (Emjection) field.get(inst);
            e2.setPico(new ImmutablePicoContainer(container));
        } catch (NoSuchFieldException e) {
            throw new PicoCompositionException("Components created via emjection have to have a field 'private Emjection emjection'. " + inst.getClass() + " is missing that field");
        } catch (IllegalAccessException e) {
            throw new PicoCompositionException("unable to access field called emjection on " + inst.getClass());
        }
    }

    public static void setupEmjection(final Object inst, final PicoContainer container) {
            setPico(inst, container);
    }


}
