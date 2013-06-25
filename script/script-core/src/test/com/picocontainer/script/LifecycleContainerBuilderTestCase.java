/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.script;

import static org.junit.Assert.assertNotSame;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.picocontainer.tck.MockFactory;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoContainer;
import com.picocontainer.Startable;

/**
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 */
@RunWith(JMock.class)
public class LifecycleContainerBuilderTestCase {

    private final Mockery mockery = MockFactory.mockeryWithCountingNamingScheme();

    @Test
    public void testBuildContainerCreatesANewChildContainerAndStartsItButNotTheParent() {
        final Startable childStartable = mockery.mock(Startable.class);
        mockery.checking(new Expectations() {
            {
                one(childStartable).start();
                one(childStartable).stop();
            }
        });

        ContainerBuilder builder = new LifecycleContainerBuilder(childStartable);

        MutablePicoContainer parent = new DefaultPicoContainer();

        final Startable parentStartable = mockery.mock(Startable.class);
        parent.addComponent(parentStartable);

        PicoContainer childContainer = builder.buildContainer(parent, null, true);
        // PicoContainer.getParent() is now ImmutablePicoContainer
        assertNotSame(parent, childContainer.getParent());

        builder.killContainer(childContainer);
    }

    static class LifecycleContainerBuilder extends DefaultContainerBuilder {

        private final Startable childStartable;

        public LifecycleContainerBuilder(final Startable childStartable) {
            this.childStartable = childStartable;
        }

        @Override
        protected MutablePicoContainer createContainer(final PicoContainer parentContainer, final Object assemblyScope) {
            MutablePicoContainer container = (MutablePicoContainer) super.createContainer(parentContainer, assemblyScope);
            container.addComponent(childStartable);
            return container;
        }

    }
}
