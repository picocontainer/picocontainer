package org.picocontainer.adapters;

import static java.lang.reflect.Modifier.isStatic;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Properties;

import org.junit.Test;
import org.mockito.internal.stubbing.answers.CallsRealMethods;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.injectors.AbstractInjectionType;
import org.picocontainer.injectors.AbstractInjector;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;


/**
 * @author Paul Hammant
 * @author J&ouml;rg Schaible
 */
@SuppressWarnings("serial")
public class SimpleNamedBindingAnnotationTestCase {

    @Test public void testNamedBinding() {
        ComponentMonitor cm  = mock(NullComponentMonitor.class, new CallsRealMethods());
        MutablePicoContainer mpc = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(),
                cm, new FieldInjection());
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
        verify(cm, times(4)).invoking(any(PicoContainer.class), any(FieldInjector.class), any(Field.class),
                any(FruitBasket.class), any(Apple.class));
        verify(cm, times(4)).invoked(any(PicoContainer.class), any(FieldInjector.class), any(Field.class),
                any(FruitBasket.class), eq(0L), isNull(), any(Apple.class));


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
    public static String bindKey(final Class type, final String bindingId) {
        return type.getName() + "/" + bindingId;
    }

    public class FieldInjection extends AbstractInjectionType {

        public <T> ComponentAdapter<T> createComponentAdapter(
            final ComponentMonitor monitor, final LifecycleStrategy lifecycle,
            final Properties componentProps, final Object key,
            final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams)
            throws PicoCompositionException {
            boolean useNames = AbstractBehavior.arePropertiesPresent(
                componentProps, Characteristics.USE_NAMES, true);
            return new FieldInjector<T>(key, impl, null, monitor, useNames);
        }
    }

    public static class FieldInjector<T> extends AbstractInjector<T> {

        protected FieldInjector(final Object key, final Class<?> impl, final FieldParameters[] parameters, final ComponentMonitor monitor, final boolean useNames) {
            super(key, impl, monitor, useNames, parameters);
        }

        @Override
        public void verify(final PicoContainer container) throws PicoCompositionException {
            // TODO Auto-generated method stub
        }

        @Override
		public T getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
            final T inst;
            try {
                inst = getComponentImplementation().newInstance();
                Field[] declaredFields = getComponentImplementation().getDeclaredFields();
                for (final Field field : declaredFields) {
                	if (Modifier.isFinal( field.getModifiers())) {
                		continue;
                	}
                    Object value = null;
                    if (!isStatic(field.getModifiers())) {
                        Named bindAnnotation = field.getAnnotation(Named.class);
                        if (bindAnnotation != null) {
                            value = container.getComponent(bindKey(field.getType(), bindAnnotation.value()));
                        } else {
                            value = container.getComponent(field.getType());
                        }
                        field.setAccessible(true);
                        field.set(inst, value);
                    }
                    currentMonitor().invoking(container, this, field, inst, value);
                    long start = System.currentTimeMillis();
                    currentMonitor().invoked(container, this, field, inst, (System.currentTimeMillis() - start), null, value);
                }

            } catch (InstantiationException e) {
                return caughtInstantiationException(currentMonitor(), null, e, container);
            } catch (IllegalAccessException e) {
                return caughtIllegalAccessException(currentMonitor(), null, e, container);
            }
            return inst;
        }

        @Override
		public String getDescriptor() {
            return "FieldInjector";
        }

    }
}
