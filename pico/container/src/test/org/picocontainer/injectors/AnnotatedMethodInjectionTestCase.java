/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/
package org.picocontainer.injectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.picocontainer.ComponentFactory;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.tck.AbstractComponentFactoryTest;
import org.picocontainer.tck.AbstractComponentAdapterTest.RecordingLifecycleStrategy;
import org.picocontainer.testmodel.NullLifecycle;
import org.picocontainer.testmodel.RecordingLifecycle.One;

/**
 * @author J&ouml;rg Schaible
 */
public class AnnotatedMethodInjectionTestCase extends AbstractComponentFactoryTest {
	
	@Before
    public void setUp() throws Exception {
        picoContainer = new DefaultPicoContainer(createComponentFactory());
    }

    protected ComponentFactory createComponentFactory() {
        return new AnnotatedMethodInjection();
    }

    public static interface Bean {
    }

    public static class NamedBean implements Bean {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class NamedBeanWithPossibleDefault extends NamedBean {
        private boolean byDefault;

        public NamedBeanWithPossibleDefault() {
        }

        public NamedBeanWithPossibleDefault(String name) {
            setName(name);
            byDefault = true;
        }

        public boolean getByDefault() {
            return byDefault;
        }
    }

    public static class NoBean extends NamedBean {
        public NoBean(String name) {
            setName(name);
        }
    }

    @Test public void testContainerUsesStandardConstructor() {
        picoContainer.addComponent(Bean.class, NamedBeanWithPossibleDefault.class);
        picoContainer.addComponent("Tom");
        NamedBeanWithPossibleDefault bean = (NamedBeanWithPossibleDefault) picoContainer.getComponent(Bean.class);
        assertFalse(bean.getByDefault());
    }

    @Test public void testContainerUsesOnlyStandardConstructor() {
        picoContainer.addComponent(Bean.class, NoBean.class);
        picoContainer.addComponent("Tom");
        try {
            picoContainer.getComponent(Bean.class);
            fail("Instantiation should have failed.");
        } catch (PicoCompositionException e) {
        }
    }
}