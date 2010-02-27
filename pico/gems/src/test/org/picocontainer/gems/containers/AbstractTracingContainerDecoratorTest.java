/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original Code By Centerline Computers, Inc.                               *
 *****************************************************************************/

package org.picocontainer.gems.containers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentFactory;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.NameBinding;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;
import org.picocontainer.adapters.InstanceAdapter;
import org.picocontainer.injectors.ConstructorInjector;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.parameters.ConstantParameter;

/**
 * @author Michael Rimov 
 * @author Mauro Talevi
 */
@RunWith(JMock.class)
@Deprecated
public abstract class AbstractTracingContainerDecoratorTest  {
	
	private Mockery mockery = mockeryWithCountingNamingScheme();
	
	private final MutablePicoContainer picoContainer = mockery.mock(MutablePicoContainer.class);
	
	private MutablePicoContainer tracingDecorator;
	
	protected Logger log4jLogger;
	
	protected Log log;
	
	private ConsoleAppender consoleAppender;
	
	private StringWriter logOutput = null;

	@Before
	public void setUp() throws Exception {
		//Setup log4j for this test case.
		//Since Commons Logging will use log4j, we set up log4j
		//similarly as to the testing of the log4j tracing container
		//decorator
		//All output will go to a string.
		log4jLogger = Logger.getLogger(AbstractTracingContainerDecoratorTest.class);
		log4jLogger.removeAllAppenders();
		log4jLogger.setAdditivity(false);
		log4jLogger.setLevel(Level.ALL);
		logOutput = new StringWriter();
		consoleAppender = new ConsoleAppender();
		consoleAppender.setName("StringWriter Appender");
		consoleAppender.setLayout(new SimpleLayout());
		consoleAppender.setWriter(logOutput);
		log4jLogger.addAppender(consoleAppender);
		tracingDecorator = createTracingContainerDecorator(picoContainer, AbstractTracingContainerDecoratorTest.class.getName());		

		log = LogFactory.getLog(AbstractTracingContainerDecoratorTest.class);

	}

	protected abstract MutablePicoContainer createTracingContainerDecorator(MutablePicoContainer picoContainer, String name);

	@After
	public void tearDown() throws Exception {
		log = null;
		log4jLogger = null;
		tracingDecorator = null;
	}

	@Test public void testAccept() {
		
		//Dummy test object
		final PicoVisitor visitor = new PicoVisitor() {
			public Object traverse(Object node) {
				throw new UnsupportedOperationException();
			}

			public void visitComponentAdapter(ComponentAdapter componentAdapter) {
				throw new UnsupportedOperationException();
			}

            public void visitComponentFactory(ComponentFactory componentFactory) {
                throw new UnsupportedOperationException();
            }

            public boolean visitContainer(PicoContainer pico) {
				throw new UnsupportedOperationException();
			}

			public void visitParameter(Parameter parameter) {
				throw new UnsupportedOperationException();
			}
			
		};
		
		mockery.checking(new Expectations(){{
			one(picoContainer).accept(with(same(visitor)));
		}});
		
		tracingDecorator.accept(visitor);
		String result = logOutput.toString();
		assertNotNull(result);
		assertTrue(result.contains("Visiting Container "));
	}

	@Test public void testAddChildContainer() {
		final MutablePicoContainer childPico = new DefaultPicoContainer();
		mockery.checking(new Expectations(){{
			one(picoContainer).addChildContainer(with(same(childPico)));
			will(returnValue(tracingDecorator));
		}});

		assertEquals(tracingDecorator, tracingDecorator.addChildContainer(childPico));
		String result = logOutput.toString();
		assertNotNull(result);
		assertTrue(result.contains("Adding child container: "));
	}

	@Test public void testDispose() {
		mockery.checking(new Expectations(){{
			one(picoContainer).dispose();			
		}});

		tracingDecorator.dispose();
		String result = logOutput.toString();
		assertNotNull(result);
		assertTrue(result.contains("Disposing container "));
		
	}

	@Test public void testGetComponentAdapter() {
		final ConstructorInjector testAdapter = new ConstructorInjector(String.class, String.class, null, new NullComponentMonitor(), false);
		mockery.checking(new Expectations(){{
			one(picoContainer).getComponentAdapter(with(same(String.class)), with(aNull(NameBinding.class)));
			will(returnValue(testAdapter));
			one(picoContainer).getComponentAdapter(with(same(Map.class)), with(aNull(NameBinding.class)));
			will(returnValue(null));
		}});
		ComponentAdapter ca = tracingDecorator.getComponentAdapter(String.class, (NameBinding) null);
		assertNotNull(ca);

		verifyLog("Locating component adapter with type ");

		ca = tracingDecorator.getComponentAdapter(Map.class, (NameBinding) null);
		assertNull(ca);

		verifyKeyNotFound();
	}
	
	private void verifyKeyNotFound() {
		verifyLog("Could not find component ");
	}
	
	private void verifyLog(final String valueToExpect) {
		String result = logOutput.toString();
		assertNotNull("Log output was null", result);
		assertTrue("Could not find '" + valueToExpect + "' in log output.  Instead got '"
				+ result + "'"
				,result.contains(valueToExpect));
	}

	@Test public void testGetComponentAdapterOfType() {
		final ConstructorInjector testAdapter = new ConstructorInjector(String.class, String.class, null, new NullComponentMonitor(), false);
		mockery.checking(new Expectations(){{
			one(picoContainer).getComponentAdapter(with(same(String.class)), with(aNull(NameBinding.class)));
			will(returnValue(testAdapter));
			one(picoContainer).getComponentAdapter(with(same(Map.class)), with(aNull(NameBinding.class)));
			will(returnValue(null));
		}});		
		ComponentAdapter ca = tracingDecorator.getComponentAdapter(String.class, (NameBinding) null);
		assertNotNull(ca);

		verifyLog("Locating component adapter with type ");

		ca = tracingDecorator.getComponentAdapter(Map.class, (NameBinding) null);
		assertNull(ca);

		verifyKeyNotFound();
	}

	@Test public void testGetComponentAdapters() {
		final List adapters = Collections.EMPTY_LIST;
		mockery.checking(new Expectations(){{
			one(picoContainer).getComponentAdapters();
			will(returnValue(adapters));
		}});
		
		Collection returnedAdapters = tracingDecorator.getComponentAdapters();
		assertEquals(adapters,returnedAdapters);
		
		verifyLog("Grabbing all component adapters for container: ");
	}

	@Test public void testGetComponentAdaptersOfType() {
		final List adapters = Collections.EMPTY_LIST;
		mockery.checking(new Expectations(){{
			one(picoContainer).getComponentAdapters(with(same(String.class)));
			will(returnValue(adapters));
		}});
		
		List returnedAdapters = tracingDecorator.getComponentAdapters(String.class);
		assertEquals(adapters,returnedAdapters);
		
		verifyLog("Grabbing all component adapters for container: ");
	}

	@Test public void testGetComponentInstance() {
		final String test = "This is a test";
		mockery.checking(new Expectations(){{
			one(picoContainer).getComponent(with(equal("foo")));
			will(returnValue(test));
			one(picoContainer).getComponent(with(equal("bar")));
			will(returnValue(null));
		}});		
		
		Object result = tracingDecorator.getComponent("foo");
		assertEquals(test, result);
		verifyLog("Attempting to load component instance with key: ");
		
		assertNull(tracingDecorator.getComponent("bar"));
		this.verifyKeyNotFound();
	}

	//FIXME @Test - fails for unknown reasons as other similar test pass
	public void testGetComponentInstanceOfType() {
		final String test = "This is a test";
		mockery.checking(new Expectations(){{
			one(picoContainer).getComponent(with(same(String.class)));
			will(returnValue(test));
			one(picoContainer).getComponent(with(same(Map.class)));
			will(returnValue(null));
		}});		

		Object result = tracingDecorator.getComponent(String.class);
		assertEquals(test, result);
		verifyLog("Attempting to load component instance with type: ");
		
		assertNull(tracingDecorator.getComponent(Map.class));
		verifyLog("Could not find component " + Map.class.getName());
	}

	@Test public void testGetComponentInstances() {
		final List test = Collections.EMPTY_LIST;
		mockery.checking(new Expectations(){{
			one(picoContainer).getComponents();
			will(returnValue(test));
		}});
		
		Object result = tracingDecorator.getComponents();
		assertEquals(test, result);
		verifyLog("Retrieving all component instances for container ");
	}

	@Test public void testGetComponentInstancesOfType() {
		final List test = Collections.EMPTY_LIST;
		final List stringTest = new ArrayList();
		stringTest.add("doe");
		stringTest.add("ray");
		stringTest.add("me");
		mockery.checking(new Expectations(){{
			one(picoContainer).getComponents(with(same(Map.class)));
			will(returnValue(test));
			one(picoContainer).getComponents(with(same(String.class)));
			will(returnValue(stringTest));
		}});
		
		Object result = tracingDecorator.getComponents(String.class);
		assertEquals(stringTest, result);
		verifyLog("Loading all component instances of type ");
		
		result = tracingDecorator.getComponents(Map.class);
		assertEquals(test, result);
		verifyLog("Could not find any components  ");
		
	}

	@Test public void testGetParent() {
		mockery.checking(new Expectations(){{
			one(picoContainer).getParent();
			will(returnValue(new DefaultPicoContainer()));
		}});
		
		Object result = tracingDecorator.getParent();
		assertNotNull(result);
		
		verifyLog("Retrieving the parent for container");
		
	}

	@Test public void testMakeChildContainer() {
		mockery.checking(new Expectations(){{
			one(picoContainer).makeChildContainer();
			will(returnValue(new DefaultPicoContainer()));
		}});
		MutablePicoContainer result = tracingDecorator.makeChildContainer();
		assertTrue(result instanceof Log4jTracingContainerDecorator);
		verifyLog("Making child container for container ");
	}

	@Test public void testRegisterComponent() {
		final ConstructorInjector testAdapter = new ConstructorInjector(String.class, String.class, null, new NullComponentMonitor(), false);
		mockery.checking(new Expectations(){{
			one(picoContainer).addAdapter(with(same(testAdapter)));
			will(returnValue(picoContainer));
			one(picoContainer).getComponentAdapter(with(same(String.class)));
			will(returnValue(testAdapter));
		}});
		ComponentAdapter result = tracingDecorator.addAdapter(testAdapter).getComponentAdapter(testAdapter.getComponentKey());
		assertEquals(testAdapter, result);
		verifyLog("Registering component adapter ");
	}

	@Test public void testRegisterComponentImplementationClass() {
		final ConstructorInjector testAdapter = new ConstructorInjector(String.class, String.class, null, new NullComponentMonitor(), false);
		mockery.checking(new Expectations(){{
			one(picoContainer).addComponent(with(same(String.class)));
			will(returnValue(picoContainer));
			one(picoContainer).getComponentAdapter(with(same(String.class)), with(aNull(NameBinding.class)));
			will(returnValue(testAdapter));
		}});
        ComponentAdapter result = tracingDecorator.addComponent(String.class).getComponentAdapter(String.class, (NameBinding) null);
		assertEquals(testAdapter, result);
		verifyLog("Registering component impl or instance ");
	}

	@Test public void testRegisterComponentImplementationWithKeyAndClass() {
		final ConstructorInjector testAdapter = new ConstructorInjector(String.class, String.class, null, new NullComponentMonitor(), false);
		mockery.checking(new Expectations(){{
			one(picoContainer).addComponent(with(same(String.class)), with(same(String.class)), with(equal(Parameter.ZERO)));
			will(returnValue(picoContainer));
			one(picoContainer).getComponentAdapter(with(same(String.class)), with(aNull(NameBinding.class)));
			will(returnValue(testAdapter));
		}});
        ComponentAdapter result = tracingDecorator.addComponent(String.class, String.class, Parameter.ZERO).getComponentAdapter(String.class,
                                                                                                                                (NameBinding) null);
		assertEquals(testAdapter, result);
		verifyLog("Registering component implementation ");
	}

	@Test public void testRegisterComponentInstanceWithKey() {
		final String testString = "This is a test.";
		final ComponentAdapter testAdapter = new InstanceAdapter(String.class, testString, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor());
		mockery.checking(new Expectations(){{
			one(picoContainer).addComponent(with(same(String.class)), with(same(testString)), with(equal(Parameter.ZERO)));
			will(returnValue(picoContainer));
			one(picoContainer).getComponentAdapter(with(same(String.class)), with(aNull(NameBinding.class)));
			will(returnValue(testAdapter));
		}});
		ComponentAdapter result = tracingDecorator.addComponent(String.class, testString, Parameter.ZERO).getComponentAdapter(String.class,
                                                                                                                              (NameBinding) null);

		assertTrue(result instanceof InstanceAdapter);
		verifyLog("Registering component instance with key ");
	}


	@Test public void testRegisterComponentImplementationObjectClassParameterArray() {
		final Parameter params[] = new Parameter []{new ConstantParameter("test")};
		final ConstructorInjector testAdapter = new ConstructorInjector(String.class, String.class, params, new NullComponentMonitor(), false);
		mockery.checking(new Expectations(){{
			one(picoContainer).addComponent(with(same(String.class)), with(same(String.class)), with(same(params)));
			will(returnValue(picoContainer));
			one(picoContainer).getComponentAdapter(with(same(String.class)), with(aNull(NameBinding.class)));
			will(returnValue(testAdapter));
		}});
        ComponentAdapter result = tracingDecorator.addComponent(String.class, String.class, params).getComponentAdapter(String.class,
                                                                                                                        (NameBinding) null);
		assertEquals(testAdapter, result);
		
		verifyLog("Registering component implementation with key ");
		verifyLog(" using parameters ");
		
	}

	@Test public void testRemoveChildContainer() {
		final MutablePicoContainer testPico = new DefaultPicoContainer();
		mockery.checking(new Expectations(){{
			one(picoContainer).removeChildContainer(with(same(testPico)));
			will(returnValue(true));
		}});
		boolean result = tracingDecorator.removeChildContainer(testPico);

		assertTrue(result);
		verifyLog("Removing child container: ");
	}

	@Test public void testStart() {
		mockery.checking(new Expectations(){{
			one(picoContainer).start();
		}});
		tracingDecorator.start();
		String result = logOutput.toString();
		assertNotNull(result);
		assertTrue(result.contains("Starting Container "));
	}

	@Test public void testStop() {
		mockery.checking(new Expectations(){{
			one(picoContainer).stop();
		}});
		tracingDecorator.stop();
		String result = logOutput.toString();
		assertNotNull(result);
		assertTrue(result.contains("Stopping Container "));
	}

	@Test public void testUnregisterComponent() {
		final ConstructorInjector testAdapter = new ConstructorInjector(String.class, String.class, null, new NullComponentMonitor(), false);
		mockery.checking(new Expectations(){{
			one(picoContainer).removeComponent(with(same(String.class)));
			will(returnValue(testAdapter));			
		}});		
		
		ComponentAdapter result = tracingDecorator.removeComponent(String.class);
		assertEquals(testAdapter, result);
		verifyLog("Unregistering component ");
	}

	@Test public void testUnregisterComponentByInstance() {
		final String testString = "This is a test.";
		final ComponentAdapter testAdapter = new InstanceAdapter(String.class, testString, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor());
		mockery.checking(new Expectations(){{
			one(picoContainer).removeComponentByInstance(with(same(testString)));
			will(returnValue(testAdapter));			
		}});
		ComponentAdapter result = tracingDecorator.removeComponentByInstance(testString);
		
		assertEquals(testAdapter, result);
		verifyLog("Unregistering component by instance (");
	}
	
	@Test public void testDecoratorIsSerializable() throws IOException, ClassNotFoundException {
		String logCategory = "this.is.a.test";
		Log4jTracingContainerDecorator decorator = new Log4jTracingContainerDecorator(new DefaultPicoContainer(), Logger.getLogger(logCategory));
		
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(decorator);
		oos.close();
		
		byte[] savedStream = os.toByteArray();
		
		ByteArrayInputStream bis = new ByteArrayInputStream(savedStream);
		ObjectInputStream ois = new ObjectInputStream(bis);
		
		Log4jTracingContainerDecorator result = (Log4jTracingContainerDecorator) ois.readObject();
		assertNotNull(result);
		assertEquals(logCategory, result.getLoggerUsed().getName());
	}

}
