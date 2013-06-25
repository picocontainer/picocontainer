/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.injectors;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.injectors.AnnotatedFieldInjection;
import com.picocontainer.lifecycle.NullLifecycleStrategy;
import com.picocontainer.monitors.ConsoleComponentMonitor;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class AnnotatedFieldInjectionTestCase {

    @Test public void testFactoryMakesAnnotationInjector() {

        AnnotatedFieldInjection injectionFactory = new AnnotatedFieldInjection();

        ConsoleComponentMonitor cm = new ConsoleComponentMonitor();
        ComponentAdapter ca = injectionFactory.createComponentAdapter(cm, new NullLifecycleStrategy(), new Properties(), Map.class, HashMap.class, null, null, null);

        XStream xs = new XStream();
        //xs.alias("CCM", ConsoleComponentMonitor.class);
        xs.registerConverter(new Converter() {
            public boolean canConvert(final Class aClass) {
                return aClass.getName().equals("com.picocontainer.monitors.ConsoleComponentMonitor") ||
                       aClass.getName().equals("com.picocontainer.lifecycle.ReflectionLifecycleStrategy");

            }

            public void marshal(final Object object,
                                final HierarchicalStreamWriter hierarchicalStreamWriter,
                                final MarshallingContext marshallingContext) {
            }

            public Object unmarshal(final HierarchicalStreamReader hierarchicalStreamReader,
                                    final UnmarshallingContext unmarshallingContext) {
                return null;
            }
        });

        String foo = xs.toXML(ca);

        assertEquals("<com.picocontainer.injectors.AnnotatedFieldInjection_-AnnotatedFieldInjector>\n" +
                     "  <key class=\"java-class\">java.util.Map</key>\n" +
                     "  <impl>java.util.HashMap</impl>\n" +
                     "  <monitor class=\"com.picocontainer.monitors.ConsoleComponentMonitor\"/>\n" +
                     "  <useNames>false</useNames>\n" +
                     "  <requireConsumptionOfAllParameters>true</requireConsumptionOfAllParameters>\n" +
                     "  <injectionAnnotations>\n" +
                     "    <java-class>javax.inject.Inject</java-class>\n" +
                     "    <java-class>com.picocontainer.annotations.Inject</java-class>\n" +
                     "  </injectionAnnotations>\n" +
                     "</com.picocontainer.injectors.AnnotatedFieldInjection_-AnnotatedFieldInjector>", foo);


    }


}
