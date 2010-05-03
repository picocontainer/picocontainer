/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Stacy Curl                        *
 *****************************************************************************/

package org.picocontainer.injectors;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentFactory;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.lifecycle.ReflectionLifecycleStrategy;
import org.picocontainer.monitors.ConsoleComponentMonitor;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.tck.AbstractComponentFactoryTest;
import org.picocontainer.testmodel.SimpleTouchable;
import org.picocontainer.testmodel.Touchable;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AdaptingInjectionTestCase extends AbstractComponentFactoryTest {

    XStream xs;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        xs = new XStream();
        xs.alias("RLS", ReflectionLifecycleStrategy.class);
        xs.alias("CCM", ConsoleComponentMonitor.class);
        xs.alias("Annotated-Method-Injection", AnnotatedMethodInjection.AnnotatedMethodInjector.class);
        xs.alias("Annotated-Field-Injection", AnnotatedFieldInjection.AnnotatedFieldInjector.class);
        xs.alias("Constructor-Injection", ConstructorInjection.ConstructorInjector.class);
        //xs.alias("CCM", ConsoleComponentMonitor.class);
        xs.registerConverter(new Converter() {
            public boolean canConvert(Class aClass) {
                return aClass.getName().equals("org.picocontainer.monitors.ConsoleComponentMonitor") ||
                       aClass.getName().equals("org.picocontainer.lifecycle.ReflectionLifecycleStrategy");

            }

            public void marshal(Object object,
                                HierarchicalStreamWriter hierarchicalStreamWriter,
                                MarshallingContext marshallingContext) {
            }

            public Object unmarshal(HierarchicalStreamReader hierarchicalStreamReader,
                                    UnmarshallingContext unmarshallingContext) {
                return null;
            }
        });

    }

    protected ComponentFactory createComponentFactory() {
        return new AdaptingInjection();
    }

    @Test public void testInstantiateComponentWithNoDependencies() throws PicoCompositionException {
        ComponentAdapter componentAdapter =
            createComponentFactory().createComponentAdapter(new NullComponentMonitor(),
                                                            new NullLifecycleStrategy(),
                                                            new Properties(Characteristics.CDI),
                                                            Touchable.class,
                                                            SimpleTouchable.class,
                                                            (Parameter[])null);

        Object comp = componentAdapter.getComponentInstance(new DefaultPicoContainer(), ComponentAdapter.NOTHING.class);
        assertNotNull(comp);
        assertTrue(comp instanceof SimpleTouchable);                               
    }

    @Test public void testSingleUsecanBeInstantiatedByDefaultComponentAdapter() {
        ComponentAdapter componentAdapter = createComponentFactory().createComponentAdapter(new NullComponentMonitor(),
                                                                                            new NullLifecycleStrategy(),
                                                                                            new Properties(
                                                                                                Characteristics.CDI),
                                                                                            "o",
                                                                                            Object.class,
                                                                                            (Parameter[])null);
        Object component = componentAdapter.getComponentInstance(new DefaultPicoContainer(), ComponentAdapter.NOTHING.class);
        assertNotNull(component);
    }


    @Test public void testFactoryMakesConstructorInjector() {

        ComponentFactory cf = createComponentFactory();

        ConsoleComponentMonitor cm = new ConsoleComponentMonitor();
        ComponentAdapter ca = cf.createComponentAdapter(cm, new NullLifecycleStrategy(), new Properties(),
                                                        Map.class, HashMap.class, Parameter.DEFAULT);

        String foo = xs.toXML(ca).replace("\"", "");

        assertEquals("<Constructor-Injection>\n" +
                     "  <key class=java-class>java.util.Map</key>\n" +
                     "  <impl>java.util.HashMap</impl>\n" +
                     "  <monitor class=CCM/>\n" +
                     "  <useNames>false</useNames>\n" +
                     "  <rememberChosenConstructor>true</rememberChosenConstructor>\n" +
                     "  <enableEmjection>false</enableEmjection>\n" +
                     "</Constructor-Injection>", foo);


    }

    @Test public void testFactoryMakesFieldAnnotationInjector() {

        ComponentFactory cf = createComponentFactory();

        ConsoleComponentMonitor cm = new ConsoleComponentMonitor();
        ComponentAdapter ca = cf.createComponentAdapter(cm,
                                                        new NullLifecycleStrategy(),
                                                        new Properties(),
                                                        AnnotatedFieldInjectorTestCase.Helicopter.class,
                                                        AnnotatedFieldInjectorTestCase.Helicopter.class,
                                                        Parameter.DEFAULT);

        String foo = xs.toXML(ca).replace("\"", "");

        assertEquals("<Annotated-Field-Injection>\n" +
                     "  <key class=java-class>org.picocontainer.injectors.AnnotatedFieldInjectorTestCase$Helicopter</key>\n" +
                     "  <impl>org.picocontainer.injectors.AnnotatedFieldInjectorTestCase$Helicopter</impl>\n" +
                     "  <monitor class=CCM/>\n" +
                     "  <useNames>false</useNames>\n" +
                     "  <injectionAnnotations>\n" +
                     "    <java-class>org.picocontainer.annotations.Inject</java-class>\n" +
                     "    <java-class>javax.inject.Inject</java-class>\n" +
                     "  </injectionAnnotations>\n" +
                     "</Annotated-Field-Injection>", foo);

        assertEquals("AnnotatedFieldInjector[org.picocontainer.annotations.@Inject,javax.inject.@Inject]-class org.picocontainer.injectors.AnnotatedFieldInjectorTestCase$Helicopter",
                ca.toString());

    }

    @Test public void testFactoryMakesMethodAnnotationInjector() {

        ComponentFactory cf = createComponentFactory();

        ConsoleComponentMonitor cm = new ConsoleComponentMonitor();
        ComponentAdapter ca = cf.createComponentAdapter(cm,
                                                        new NullLifecycleStrategy(),
                                                        new Properties(),
                                                        AnnotatedMethodInjectorTestCase.AnnotatedBurp.class,
                                                        AnnotatedMethodInjectorTestCase.AnnotatedBurp.class,
                                                        Parameter.DEFAULT);

        String foo = xs.toXML(ca).replace("\"", "");

        assertEquals("<Annotated-Method-Injection>\n" +
                     "  <key class=java-class>org.picocontainer.injectors.AnnotatedMethodInjectorTestCase$AnnotatedBurp</key>\n" +
                     "  <impl>org.picocontainer.injectors.AnnotatedMethodInjectorTestCase$AnnotatedBurp</impl>\n" +
                     "  <monitor class=CCM/>\n" +
                     "  <useNames>false</useNames>\n" +
                     "  <methodNamePrefix></methodNamePrefix>\n" +
                     "  <injectionAnnotations>\n" +
                     "    <java-class>org.picocontainer.annotations.Inject</java-class>\n" +
                     "    <java-class>javax.inject.Inject</java-class>\n" +
                     "  </injectionAnnotations>\n" +
                     "</Annotated-Method-Injection>", foo);

        assertEquals("AnnotatedMethodInjector[org.picocontainer.annotations.@Inject,javax.inject.@Inject]-class org.picocontainer.injectors.AnnotatedMethodInjectorTestCase$AnnotatedBurp",
                ca.toString());

    }


}
