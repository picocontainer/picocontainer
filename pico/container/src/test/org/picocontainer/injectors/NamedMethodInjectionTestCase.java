/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.lifecycle.NullLifecycleStrategy;

/**
 * @author Paul Hammant
 */
public class NamedMethodInjectionTestCase {

    public static class Bean {
        private String something;

        public void setSomething(final String blah) {
            this.something = blah;
        }
    }

    @Test
    public void containerShouldMakeUsableNamedMethodInjector() {
        DefaultPicoContainer picoContainer = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new NamedMethodInjection());
        picoContainer.addComponent(Bean.class);
        picoContainer.addConfig("something", "hello there");
        assertTrue(picoContainer.getComponentAdapter(Bean.class) instanceof NamedMethodInjection.NamedMethodInjector);
        assertEquals("hello there", picoContainer.getComponent(Bean.class).something);
    }

    @Test
    public void containerShouldMakeNamedMethodInjectorThatIsOptionalInUse() {
        DefaultPicoContainer picoContainer = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new NamedMethodInjection(true));
        picoContainer.addComponent(Bean.class);
        assertTrue(picoContainer.getComponentAdapter(Bean.class) instanceof NamedMethodInjection.NamedMethodInjector);
        assertNull(picoContainer.getComponent(Bean.class).something);
    }

    public static class MyConnectionPoolDataSource {
        private String serverName;
        private String databaseName;
        private String user;

        public void setServerName(final String a0) {
            this.serverName = a0;
        }
        public void setDatabaseName(final String a0) {
            this.databaseName = a0;
        }
        public void setUser(final String a0) {
            this.user = a0;
        }
    }

    @Test
    public void vincentVanBeverensExample() {
        MyConnectionPoolDataSource windmill = new DefaultPicoContainer(new NamedMethodInjection())
                .addComponent(MyConnectionPoolDataSource.class)
                .addConfig("user", "fred")
                .addConfig("serverName", "example.com")
                .addConfig("databaseName", "big://table")
                .getComponent(MyConnectionPoolDataSource.class);
        assertNotNull(windmill);
        assertNotNull(windmill.serverName);
        assertEquals("example.com", windmill.serverName);
        assertNotNull(windmill.user);
        assertEquals("fred", windmill.user);
        assertNotNull(windmill.databaseName);
        assertEquals("big://table", windmill.databaseName);
    }

}