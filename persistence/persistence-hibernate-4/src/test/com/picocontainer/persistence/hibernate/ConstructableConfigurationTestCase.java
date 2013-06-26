/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file. 
 ******************************************************************************/

package com.picocontainer.persistence.hibernate;

import org.junit.Test;

public class ConstructableConfigurationTestCase extends AbstractConfigurationTestCase {

    @Test
    public void canLoadDefaultConfiguration() throws Exception {
        Pojo pojo = new Pojo();
        pojo.setFoo("Foo!");
        assertPojoCanBeSaved(new ConfigurableSessionFactory(new ConstructableConfiguration()), pojo);
    }

    @Test
    public void canLoadCustomConstruction() throws Exception {
        Pojo pojo = new Pojo();
        pojo.setFoo("Foo!");
        assertPojoCanBeSaved(new ConfigurableSessionFactory(new ConstructableConfiguration("/hibernate.cfg.xml")), pojo);
    }


}
