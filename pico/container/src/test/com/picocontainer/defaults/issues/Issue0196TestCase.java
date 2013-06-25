/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.defaults.issues;

import static org.junit.Assert.assertNotNull;
import junit.framework.Assert;

import org.junit.Test;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;

public class Issue0196TestCase {
    public static class Descriptor {
        public static class DescriptorData {
        }
    }
    public static class DescriptorDep {
        public DescriptorDep(final Descriptor.DescriptorData[] datas) {
            Assert.assertEquals(3, datas.length);
            Assert.assertNull(datas[0]);
            Assert.assertNull(datas[1]);
            Assert.assertNull(datas[2]);
        }
    }

    @Test public void testShouldAllowRegistrationOfArrayAsInstance() {
        MutablePicoContainer pico = new DefaultPicoContainer();

        Descriptor.DescriptorData[] datas = new Descriptor.DescriptorData[3];

        pico.addComponent(datas);
        pico.addComponent(DescriptorDep.class);

        DescriptorDep descriptorDep = pico.getComponent(DescriptorDep.class);

        assertNotNull(descriptorDep);
    }
}
