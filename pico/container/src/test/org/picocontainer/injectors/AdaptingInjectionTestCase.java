/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
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
import org.picocontainer.PicoCompositionException;
import org.picocontainer.injectors.AnnotatedFieldInjectorTestCase.Helicopter;
import org.picocontainer.injectors.AnnotatedMethodInjectorTestCase.AnnotatedBurp;
import org.picocontainer.injectors.CompositeInjection.CompositeInjector;
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

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
        //xs.alias("Constructor-Injection", ConstructorInjection.ConstructorInjector.class);
        xs.alias("Constructor-Injection", Jsr330ConstructorInjection.ConstructorInjectorWithForcedPublicCtors.class);
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
        
        assertTrue("Got " + ca.toString(), ca.toString().contains("AnnotatedFieldInjector[javax.inject.@Inject,org.picocontainer.annotations.@Inject]"));
        assertTrue("Got " + ca.toString(), ca.toString().contains("org.picocontainer.injectors.AnnotatedFieldInjectorTestCase$Helicopter"));
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
//                     "  <key class=java-class>org.picocontainer.injectors.AnnotatedMethodInjectorTestCase$AnnotatedBurp</key>\n" +
//                     "  <impl>org.picocontainer.injectors.AnnotatedMethodInjectorTestCase$AnnotatedBurp</impl>\n" +
//                     "  <monitor class=CCM/>\n" +
//                     "  <useNames>false</useNames>\n" +
//                     "  <methodNamePrefix></methodNamePrefix>\n" +
//                     "  <injectionAnnotations>\n" +
//                     "    <java-class>org.picocontainer.annotations.Inject</java-class>\n" +
//                     "    <java-class>javax.inject.Inject</java-class>\n" +
//                     "  </injectionAnnotations>\n" +
//                     "</Annotated-Method-Injection>", foo);

        assertTrue("Got " + ca.toString(), ca.toString().contains("AnnotatedMethodInjector[org.picocontainer.annotations.@Inject,javax.inject.@Inject]"));
        assertTrue("Got " + ca.toString(), ca.toString().contains("org.picocontainer.injectors.AnnotatedMethodInjectorTestCase$AnnotatedBurp"));
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
