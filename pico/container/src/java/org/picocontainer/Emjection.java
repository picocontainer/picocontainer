package org.picocontainer;

import org.picocontainer.containers.ImmutablePicoContainer;
import org.picocontainer.containers.TransientPicoContainer;
import org.picocontainer.injectors.ConstructorInjection;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class Emjection {

    private PicoContainer pico;

    public void setPico(ImmutablePicoContainer container) {
        if (pico != null) {
            throw new PicoCompositionException("Emjection can only be setup once per component");
        }
        pico = container;
    }

    public static <T> T neu(Class<T> type, Emjection emjection, Object... args) {
        if (emjection.pico == null) {
            throw new PicoCompositionException("blah");
        }
        TransientPicoContainer tpc = new TransientPicoContainer(new ConstructorInjection(), emjection.pico);
        for (Object arg : args) {
            tpc.addComponent(arg);
        }
        T inst = tpc.getComponent(type);
        if (inst == null) {
            tpc.addComponent(type);
            inst = tpc.getComponent(type);
        }
        setPico(inst, tpc);
        return inst;
    }

    private static <T> void setPico(Object inst, PicoContainer container) {
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

    public static void setupEmjection(Object inst, PicoContainer container) {
            setPico(inst, container);
    }


}
