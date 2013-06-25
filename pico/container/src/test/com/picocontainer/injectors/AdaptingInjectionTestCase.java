/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Stacy Curl                        *
 *****************************************************************************/

package com.picocontainer.injectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import com.picocontainer.injectors.AnnotatedFieldInjectorTestCase.Helicopter;
import com.picocontainer.injectors.AnnotatedMethodInjectorTestCase.AnnotatedBurp;
import com.picocontainer.tck.AbstractComponentFactoryTest;
import com.picocontainer.testmodel.SimpleTouchable;
import com.picocontainer.testmodel.Touchable;

import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentFactory;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.injectors.AdaptingInjection;
import com.picocontainer.injectors.AnnotatedFieldInjection;
import com.picocontainer.injectors.AnnotatedMethodInjection;
import com.picocontainer.injectors.Jsr330ConstructorInjection;
import com.picocontainer.injectors.CompositeInjection.CompositeInjector;
import com.picocontainer.lifecycle.NullLifecycleStrategy;
import com.picocontainer.lifecycle.ReflectionLifecycleStrategy;
import com.picocontainer.monitors.ConsoleComponentMonitor;
import com.picocontainer.monitors.NullComponentMonitor;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class AdaptingInjectionTestCase extends AbstractComponentFactoryTest {

    XStream xs;

    @Override
	@Before
    public void setUp() throws Exception {
        super.setUp();
        xs = new XStream();
        xs.alias("RLS", ReflectionLifecycleStrategy.class);
        xs.alias("CCM", ConsoleComponentMonitor.class);
        xs.alias("Annotated-Method-Injection", AnnotatedMethodInjection.AnnotatedMethodInjector.class);
        xs.alias("Annotated-Field-Injection", AnnotatedFieldInjection.AnnotatedFieldInjector.class);
        //xs.alias("Constructor-Injection", ConstructorInjection.ConstructorInjector.class);
        xs.alias("Constructor-Injection", Jsr330ConstructorInjection.ConstructorInjectorWithForcedPublicCtors.class);
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

    }

    @Override
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
                                                            null, null, null);

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
                                                                                            null, null, null);
        Object component = componentAdapter.getComponentInstance(new DefaultPicoContainer(), ComponentAdapter.NOTHING.class);
        assertNotNull(component);
    }


    @Test public void testFactoryMakesJSR330ConstructorInjector() {

        ComponentFactory cf = createComponentFactory();

        ConsoleComponentMonitor cm = new ConsoleComponentMonitor();
        ComponentAdapter<HashMap> ca = cf.createComponentAdapter(cm, new NullLifecycleStrategy(), new Properties(),
                                                        Map.class, HashMap.class, null, null, null);

        assertNotNull(ca);
        String foo = xs.toXML(ca).replace("\"", "");

        assertTrue("Got " + foo,  foo.contains("<Constructor-Injection>"));


    }

    @Test public void testFactoryMakesFieldAnnotationInjector() {

        ComponentFactory cf = createComponentFactory();

        ConsoleComponentMonitor cm = new ConsoleComponentMonitor();
        ComponentAdapter<Helicopter> ca = cf.createComponentAdapter(cm,
                                                        new NullLifecycleStrategy(),
                                                        new Properties(),
                                                        AnnotatedFieldInjectorTestCase.Helicopter.class,
                                                        AnnotatedFieldInjectorTestCase.Helicopter.class,
                                                        null, null, null);

        assertNotNull(ca);
        String foo = xs.toXML(ca).replace("\"", "");
        assertTrue("Got " + foo, foo.contains("<Annotated-Field-Injection>"));

        assertTrue("Got " + ca.toString(), ca.toString().contains("AnnotatedFieldInjector[javax.inject.@Inject,com.picocontainer.annotations.@Inject]"));
        assertTrue("Got " + ca.toString(), ca.toString().contains("com.picocontainer.injectors.AnnotatedFieldInjectorTestCase$Helicopter"));
    }

    @Test public void testFactoryMakesMethodAnnotationInjector() {

        ComponentFactory cf = createComponentFactory();

        ConsoleComponentMonitor cm = new ConsoleComponentMonitor();
        ComponentAdapter<AnnotatedBurp> ca = cf.createComponentAdapter(cm,
                                                        new NullLifecycleStrategy(),
                                                        new Properties(),
                                                        AnnotatedMethodInjectorTestCase.AnnotatedBurp.class,
                                                        AnnotatedMethodInjectorTestCase.AnnotatedBurp.class,
                                                        null, null, null);

        assertNotNull(ca);
        String foo = xs.toXML(ca).replace("\"", "");
        assertTrue("Got " + foo, foo.contains("<Annotated-Method-Injection>"));
//        assertEquals("<Annotated-Method-Injection>\n" +
//                     "  <key class=java-class>com.picocontainer.injectors.AnnotatedMethodInjectorTestCase$AnnotatedBurp</key>\n" +
//                     "  <impl>com.picocontainer.injectors.AnnotatedMethodInjectorTestCase$AnnotatedBurp</impl>\n" +
//                     "  <monitor class=CCM/>\n" +
//                     "  <useNames>false</useNames>\n" +
//                     "  <methodNamePrefix></methodNamePrefix>\n" +
//                     "  <injectionAnnotations>\n" +
//                     "    <java-class>com.picocontainer.annotations.Inject</java-class>\n" +
//                     "    <java-class>javax.inject.Inject</java-class>\n" +
//                     "  </injectionAnnotations>\n" +
//                     "</Annotated-Method-Injection>", foo);

        assertTrue("Got " + ca.toString(), ca.toString().contains("AnnotatedMethodInjector[com.picocontainer.annotations.@Inject,javax.inject.@Inject]"));
        assertTrue("Got " + ca.toString(), ca.toString().contains("com.picocontainer.injectors.AnnotatedMethodInjectorTestCase$AnnotatedBurp"));
    }

    @Test
    public void testFailedParameterNames() {
        ComponentFactory cf = createComponentFactory();

        ConsoleComponentMonitor cm = new ConsoleComponentMonitor();
        try {
			ComponentAdapter<AnnotatedBurp> ca = cf.createComponentAdapter(cm,
			                                                new NullLifecycleStrategy(),
			                                                new Properties(),
			                                                AnnotatedMethodInjectorTestCase.AnnotatedBurp.class,
			                                                AnnotatedMethodInjectorTestCase.AnnotatedBurp.class,
			                                                null, null, null);
		} catch (PicoCompositionException e) {
			String message = e.getMessage();

			assertTrue("Got " + message, message.contains("test"));
			assertFalse("Got " + message, message.contains("fred"));
			assertTrue("Got " + message, message.contains(AnnotatedMethodInjectorTestCase.AnnotatedBurp.class.getName()));
		}

    }


    /**
     * Both types of injection are present to trigger both times of component adapters
     */
    public static class InjectionOrderTest {

    	@Inject
    	private String something;

    	@Inject
    	public void injectSomething() {

    	}

    }

    @Test
    public void testJSRFieldsAreInjectedBeforeJSRMethods() {
        ComponentFactory cf = createComponentFactory();
		ComponentAdapter<InjectionOrderTest> ca = cf.createComponentAdapter(new NullComponentMonitor(),
                new NullLifecycleStrategy(),
                new Properties(),
                InjectionOrderTest.class,
                InjectionOrderTest.class,
                null, null, null);

		CompositeInjector<?> ci = ca.findAdapterOfType(CompositeInjector.class);
		assertNotNull(ci);

		String result = ci.getDescriptor();
		assertNotNull(result);

		int methodInjectionLocation = result.indexOf("AnnotatedMethodInjector");
		int fieldInjectionLocation = result.indexOf("AnnotatedFieldInjector");

		assertTrue(fieldInjectionLocation < methodInjectionLocation);


    }




}
