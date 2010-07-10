/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.injectors;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.annotations.Inject;
import org.picocontainer.monitors.NullComponentMonitor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.Assert.assertNotNull;

public class AnnotatedMethodInjectorTestCase  {

    public static class AnnotatedBurp {

        private Wind wind;

        @Inject
        public void windyWind(Wind wind) {
            this.wind = wind;
        }
    }

    public static class AnnotatedBurp2 {

        private Wind wind;
        private Wind wind2;
        private Wind wind3;

        @Inject
        public void windyWind(Wind wind) {
            this.wind = wind;
        }

        @Inject
        public void windyWindToTheMax(Wind wind2, Wind wind3) {
            this.wind2 = wind2;
            this.wind3 = wind3;
        }
    }

    public static class SetterBurp {

        private Wind wind;

        public void setWind(Wind wind) {
            this.wind = wind;
        }
    }

    public static class Wind {
    }

    @Test public void testSetterMethodInjectionToContrastWithThatBelow() {

        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(new SetterInjection.SetterInjector(SetterBurp.class, SetterBurp.class, new NullComponentMonitor(), "set", false, Parameter.DEFAULT
        ));
        pico.addComponent(Wind.class, new Wind());
        SetterBurp burp = pico.getComponent(SetterBurp.class);
        assertNotNull(burp);
        assertNotNull(burp.wind);
    }

    @Test public void tesMethodInjectionWithInjectionAnnontation() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(new AnnotatedMethodInjection.AnnotatedMethodInjector(AnnotatedBurp.class, AnnotatedBurp.class, Parameter.DEFAULT,
                                               new NullComponentMonitor(), false, Inject.class));
        pico.addComponent(Wind.class, new Wind());
        AnnotatedBurp burp = pico.getComponent(AnnotatedBurp.class);
        assertNotNull(burp);
        assertNotNull(burp.wind);
    }

    @Test public void tesMethodInjectionWithInjectionAnnontationWhereThereIsMoreThanOneInjectMethod() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(new AnnotatedMethodInjection.AnnotatedMethodInjector(AnnotatedBurp2.class, AnnotatedBurp2.class, null,
                                               new NullComponentMonitor(), false, Inject.class));
        pico.addComponent(Wind.class, new Wind());
        AnnotatedBurp2 burp = pico.getComponent(AnnotatedBurp2.class);
        assertNotNull(burp);
        assertNotNull(burp.wind);
        assertNotNull(burp.wind2);
        assertNotNull(burp.wind3);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value={ ElementType.METHOD, ElementType.FIELD})
    public @interface AlternativeInject {
    }

    public static class AnotherAnnotatedBurp {
        private Wind wind;
        @AlternativeInject
        public void windyWind(Wind wind) {
            this.wind = wind;
        }
    }

    
    @Test public void testNonSetterMethodInjectionWithAlternativeAnnotation() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(new AnnotatedMethodInjection.AnnotatedMethodInjector(AnotherAnnotatedBurp.class, AnotherAnnotatedBurp.class, Parameter.DEFAULT,
                                               new NullComponentMonitor(),
                false, AlternativeInject.class));
        pico.addComponent(Wind.class, new Wind());
        AnotherAnnotatedBurp burp = pico.getComponent(AnotherAnnotatedBurp.class);
        assertNotNull(burp);
        assertNotNull(burp.wind);
    }


}
