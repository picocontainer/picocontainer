package org.picocontainer.adapters;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Properties;

import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.AbstractBehaviorFactory;
import org.picocontainer.injectors.AbstractInjector;
import org.picocontainer.injectors.AbstractInjectionFactory;


/**
 * @author Paul Hammant
 * @author J&ouml;rg Schaible
 */
@SuppressWarnings("serial")
public class SimpleNamedBindingAnnotationTestCase {

    @Test public void testNamedBinding() {
        MutablePicoContainer mpc = new DefaultPicoContainer(new FieldInjection());
        mpc.addComponent(FruitBasket.class);
        mpc.addComponent(bindKey(Apple.class, "one"), AppleImpl1.class);
        mpc.addComponent(bindKey(Apple.class, "two"), AppleImpl2.class);
        mpc.addComponent(bindKey(Apple.class, "three"), AppleImpl3.class);
        mpc.addComponent(bindKey(Apple.class, "four"), AppleImpl4.class);
        // this level of terseness is the other way ....
        // this should not be barfing if if we can get binding to annotations working
        FruitBasket fb = mpc.getComponent(FruitBasket.class);
        assertEquals(fb.one.getX(), 1);
        assertEquals(fb.two.getX(), 2);
        assertEquals(fb.three.getX(), 3);
        assertEquals(fb.four.getX(), 4);
    }

    public interface Apple {
        int getX();
    }

    public static class AppleImpl1 implements Apple {
        public int getX() {
            return 1;
        }
    }

    public static class AppleImpl2 implements Apple {
        public int getX() {
            return 2;
        }
    }

    public static class AppleImpl3 implements Apple {
        public int getX() {
            return 3;
        }
    }

    public static class AppleImpl4 implements Apple {
        public int getX() {
            return 4;
        }
    }

    public static class FruitBasket {
        private @Named("one")
        Apple one;
        private @Named("two")
        Apple two;
        private @Named("three")
        Apple three;
        private @Named("four")
        Apple four;

        public FruitBasket() {
        }
    }

    // to become an annotation
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    public @interface Named {
    	String value();
    }

    // implicitly this function goes into DPC
    public static String bindKey(Class type, String bindingId) {
        return type.getName() + "/" + bindingId;
    }

    public class FieldInjection extends AbstractInjectionFactory {

        public <T> ComponentAdapter<T> createComponentAdapter(
            ComponentMonitor componentMonitor, LifecycleStrategy lifecycleStrategy,
            Properties componentProperties, Object componentKey,
            Class<T> componentImplementation, Parameter ... parameters)
            throws PicoCompositionException {
            boolean useNames = AbstractBehaviorFactory.arePropertiesPresent(
                componentProperties, Characteristics.USE_NAMES, true);
            return new FieldInjector(componentKey, componentImplementation, parameters, componentMonitor, useNames);
        }
    }

    public static class FieldInjector<T> extends AbstractInjector<T> {

        protected FieldInjector(Object componentKey, Class componentImplementation, Parameter[] parameters, ComponentMonitor monitor, boolean useNames) {
            super(componentKey, componentImplementation, parameters, monitor, useNames);
        }

        @Override
        public void verify(PicoContainer container) throws PicoCompositionException {
            // TODO Auto-generated method stub
        }

        public T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            final T inst;
            try {
                inst = getComponentImplementation().newInstance();
                Field[] declaredFields = getComponentImplementation().getDeclaredFields();
                for (final Field field : declaredFields) {
                    Named bindAnnotation = field.getAnnotation(Named.class);
                    Object value;
                    if (bindAnnotation != null) {
                        value = container.getComponent(bindKey(field.getType(), bindAnnotation.value()));
                    } else {
                        value = container.getComponent(field.getType());
                    }
                    field.setAccessible(true);
                    field.set(inst, value);
                }

            } catch (InstantiationException e) {
                return caughtInstantiationException(currentMonitor(), null, e, container);
            } catch (IllegalAccessException e) {
                return caughtIllegalAccessException(currentMonitor(), null, e, container);
            }
            return inst;
        }

        public String getDescriptor() {
            return "FieldInjector";
        }

    }
}
