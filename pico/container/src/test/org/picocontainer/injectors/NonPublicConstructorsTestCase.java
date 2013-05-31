/*****************************************************************************
 * Copyright (C) PicoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import org.junit.Test;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.injectors.ConstructorInjection.ConstructorInjector;
import org.picocontainer.monitors.NullComponentMonitor;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.fail;

public final class NonPublicConstructorsTestCase {

    @Test
   public void doFirstSampleWithNotPublicConstructor() {
        MutablePicoContainer container = new DefaultPicoContainer();

        ComponentAdapter<DummyNotPublicConstructor> dummyComponentAdapter =
                new ConstructorInjector<DummyNotPublicConstructor>(
                        DummyNotPublicConstructor.class,
                        DummyNotPublicConstructor.class)
                .withNonPublicConstructors();

        container.addAdapter(dummyComponentAdapter);

        DummyNotPublicConstructor dummy = container.getComponent(DummyNotPublicConstructor.class);
        assertNotNull(dummy);
    }

    @Test
    public void doSecondSampleWithNotPublicClass() {
        MutablePicoContainer container = new DefaultPicoContainer();

        ComponentAdapter<DummyNotPublicClass> dummyComponentAdapter =
                new ConstructorInjector<DummyNotPublicClass>(
                        DummyNotPublicClass.class.getCanonicalName(),
                        DummyNotPublicClass.class)
                .withNonPublicConstructors();

        container.addAdapter(dummyComponentAdapter);

        Object item = container.getComponent(DummyNotPublicClass.class);
        assertNotNull(item);
    }

    @Test
    public void doThirdSampleWithProtectedConstructor() {
        MutablePicoContainer container = new DefaultPicoContainer();

        ComponentAdapter<DummyProtectedConstructor> dummyComponentAdapter =
                new ConstructorInjector<DummyProtectedConstructor>(
                        DummyProtectedConstructor.class,
                        DummyProtectedConstructor.class)
                .withNonPublicConstructors();


        container.addAdapter(dummyComponentAdapter);

        DummyProtectedConstructor dummy = container.getComponent(DummyProtectedConstructor.class);
        assertNotNull(dummy);

    }

    public static class DummyProtectedConstructor {
        protected DummyProtectedConstructor() {
        }
    }

    public static class DummyNotPublicConstructor {
        DummyNotPublicConstructor() {
        }
    }

    static class DummyNotPublicClass {
    }

}