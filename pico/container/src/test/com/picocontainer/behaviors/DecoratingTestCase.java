/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package com.picocontainer.behaviors;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;

import org.junit.Test;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.annotations.Inject;
import com.picocontainer.behaviors.Decorating;
import com.picocontainer.behaviors.FieldDecorating;
import com.picocontainer.injectors.ConstructorInjection;

public class DecoratingTestCase {

    public static interface Swede {
    }

    public static class Turnip {
        @Inject
        Swede swede;
        private final String foo;

        public Turnip(final String foo) {
            this.foo = foo;
        }

        public Swede getSwede() {
            return swede;
        }

        public String getFoo() {
            return foo;
        }
    }


    @Test
    public void testThatComponentCanHaveAProvidedDependencyViaDecoratorBehavior() {
        MutablePicoContainer container = new DefaultPicoContainer(new SwedeDecorating().wrap(new ConstructorInjection()));
        container.addComponent(String.class, "foo");
        container.addComponent(Turnip.class);
        Turnip t = container.getComponent(Turnip.class);
        assertNotNull(t);
        assertNotNull(t.getSwede());
        assertEquals("Swede:" + Turnip.class.getName(), t.getSwede().toString());
        assertEquals("foo", t.getFoo());

    }

    @Test
    public void testThatComponentCanHaveAProvidedDependencyViaFieldDecoratorBehavior() {
        MutablePicoContainer container = new DefaultPicoContainer(
                new FieldDecorating(Swede.class) {
                    public Object decorate(final Object instance) {
                        return new Swede() {
                            @Override
							public String toString() {
                                return "Swede:" + instance.getClass().getName();
                            }
                        };
                    }
                }.wrap(new ConstructorInjection()));
        container.addComponent(String.class, "foo");
        container.addComponent(Turnip.class);
        Turnip t = container.getComponent(Turnip.class);
        assertNotNull(t);
        assertNotNull(t.getSwede());
        assertEquals("Swede:" + Turnip.class.getName(), t.getSwede().toString());
        assertEquals("foo", t.getFoo());

    }

    private static class SwedeDecorating extends Decorating {
        public Object decorate(final Object instance) {
            Field[] fields = instance.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getType() == Swede.class) {
                    Swede value = new Swede() {
                        @Override
						public String toString() {
                            return "Swede:" + instance.getClass().getName();
                        }
                    };
                    field.setAccessible(true);
                    try {
                        field.set(instance, value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
            return null;
        }
    }

}
