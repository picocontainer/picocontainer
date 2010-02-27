/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.injectors;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.lifecycle.ReflectionLifecycleStrategy;
import org.picocontainer.monitors.ConsoleComponentMonitor;

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
        ComponentAdapter ca = injectionFactory.createComponentAdapter(cm, new NullLifecycleStrategy(), new Properties(), Map.class, HashMap.class, Parameter.DEFAULT);
        
        XStream xs = new XStream();
        //xs.alias("CCM", ConsoleComponentMonitor.class);
        xs.registerConverter(new Converter() {
            public boolean canConvert(Class aClass) {
                return aClass.getName().equals("org.picocontainer.monitors.ConsoleComponentMonitor") ||
                       aClass.getName().equals("org.picocontainer.lifecycle.ReflectionLifecycleStrategy");

            }

            public void marshal(Object object,
                                HierarchicalStreamWriter hierarchicalStreamWriter,
                                MarshallingContext marshallingContext)
            {
            }

            public Object unmarshal(HierarchicalStreamReader hierarchicalStreamReader,
                                    UnmarshallingContext unmarshallingContext)
            {
                return null;
            }
        });

        String foo = xs.toXML(ca);

        assertEquals("<org.picocontainer.injectors.AnnotatedFieldInjector>\n" +
                     "  <componentKey class=\"java-class\">java.util.Map</componentKey>\n" +
                     "  <componentImplementation>java.util.HashMap</componentImplementation>\n" +
                     "  <componentMonitor class=\"org.picocontainer.monitors.ConsoleComponentMonitor\"/>\n" +
                     "  <useNames>false</useNames>\n" +
                     "  <injectionAnnotation>org.picocontainer.annotations.Inject</injectionAnnotation>\n" +
                     "</org.picocontainer.injectors.AnnotatedFieldInjector>", foo);


    }


}
