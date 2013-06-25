/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.defaults;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import com.picocontainer.testmodel.DependsOnTouchable;
import com.picocontainer.testmodel.SimpleTouchable;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoContainer;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.io.xml.XppDriver;

/**
 * @author Aslak Helles&oslash;y
 */
public final class XStreamSerialisationTestCase {
    private final XStream xStream = new XStream(new XppDriver());

    @Test public void testShouldBeAbleToSerialiseEmptyPico() {
        if (JVM.is14()) {
            MutablePicoContainer pico = new DefaultPicoContainer();
            String picoXml = xStream.toXML(pico);
            PicoContainer serializedPico = (PicoContainer) xStream.fromXML(picoXml);

            assertEquals(0, serializedPico.getComponents().size());
        }
    }

    @Test public void testShouldBeAbleToSerialisePicoWithUninstantiatedComponents() {
        if (JVM.is14()) {
            MutablePicoContainer pico = new DefaultPicoContainer();
            pico.addComponent(SimpleTouchable.class);
            pico.addComponent(DependsOnTouchable.class);
            String picoXml = xStream.toXML(pico);
            PicoContainer serializedPico = (PicoContainer) xStream.fromXML(picoXml);

            assertEquals(2, serializedPico.getComponents().size());
        }
    }

    @Test public void testShouldBeAbleToSerialisePicoWithInstantiatedComponents() {
        if (JVM.is14()) {
            MutablePicoContainer pico = new DefaultPicoContainer();
            pico.addComponent(SimpleTouchable.class);
            pico.addComponent(DependsOnTouchable.class);
            pico.getComponents();
            String picoXml = xStream.toXML(pico);
            PicoContainer serializedPico = (PicoContainer) xStream.fromXML(picoXml);

            assertEquals(2, serializedPico.getComponents().size());
        }
    }
}