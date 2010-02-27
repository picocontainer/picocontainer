/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/

package org.picocontainer.persistence.hibernate.annotations;

import org.junit.Test;
import org.picocontainer.persistence.hibernate.AbstractConfigurationTestCase;
import org.picocontainer.persistence.hibernate.ConfigurableSessionFactory;

/**
 * @author Michael Rimov
 * @author Jose Peleteiro
 * @author Mauro Talevi
 */
public class ConstructableAnnotationConfigurationTestCase extends AbstractConfigurationTestCase {

    @Test
    public void canLoadConfigurationWithAnnotatedEntity() throws Exception {
        AnnotatedPojo pojo = new AnnotatedPojo();
        pojo.setFoo("Foo!");
        assertPojoCanBeSaved(new ConfigurableSessionFactory(new ConstructableAnnotationConfiguration(
                "/hibernate-annotations.cfg.xml")), pojo);
    }

}
