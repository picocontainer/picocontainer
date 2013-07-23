package com.picocontainer.web.providers;

import static com.picocontainer.web.ContextParameters.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import javax.servlet.ServletContext;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.behaviors.Guarding.Guarded;
import com.picocontainer.behaviors.Locking;
import com.picocontainer.behaviors.Locking.Locked;
import com.picocontainer.behaviors.Synchronizing;
import com.picocontainer.behaviors.Synchronizing.Synchronized;
import com.picocontainer.containers.EmptyPicoContainer;
import com.picocontainer.gems.jmx.JMXExposing;
import com.picocontainer.gems.jmx.JMXExposing.JMXExposed;
import com.picocontainer.gems.monitors.CommonsLoggingComponentMonitor;
import com.picocontainer.gems.monitors.Slf4jComponentMonitor;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.web.PicoServletContainerListener;
import com.picocontainer.web.ProfilingSecurityManager;
import com.picocontainer.web.ScopedContainers;
import com.picocontainer.web.providers.defaults.CommonsLoggingMonitorProvider;
import com.picocontainer.web.providers.defaults.Slf4jMonitorProvider;
import com.picocontainer.web.providers.defaults.TestPicoProvider;

@RunWith(JMock.class)
public class PicoServletParameterProcessorTestCase {
	
	private Mockery context = new JUnit4Mockery();
	
	private ServletContext servletContext = null;

	@Before
	public void setUp() throws Exception {
		servletContext = context.mock(ServletContext.class);
	}

	@After
	public void tearDown() throws Exception {
		servletContext = null;
	}

	@Test
	public void testNullsStillResultInWorkablePicoContainer() {
	
		addNullStateLessExpectations();
		addNullComponentMonitorExpectations();
		addNullBehaviorExpectations();
		addNullParentPicoContainerExpectations();
		addNullExpectationsOnProfilingJavaSecurity();

		PicoServletParameterProcessor paramProcessor = new PicoServletParameterProcessor();
		AbstractScopedContainerBuilder containerBuilder = paramProcessor.processContextParameters(servletContext);
		assertNotNull(containerBuilder);
		
		ScopedContainers containers =  containerBuilder.makeScopedContainers(paramProcessor.isStateless());
		assertNotNull(containers);
		
		MutablePicoContainer appPico = DefaultScopedContainerBuilderTestCase.getAppContainer(containers);
		assertNotNull(appPico);
		
		appPico.as(Characteristics.GUARD).addComponent(Object.class);
		ComponentAdapter<?> objectAdapter = appPico.getComponentAdapter(Object.class);
		assertNotNull(objectAdapter.findAdapterOfType(Guarded.class));
		assertTrue(appPico.changeMonitor(new NullComponentMonitor()) instanceof NullComponentMonitor);
	
		
		MutablePicoContainer sessionPico = DefaultScopedContainerBuilderTestCase.getSessionContainer(containers);
		assertNotNull(sessionPico);
		assertTrue(sessionPico.changeMonitor(new NullComponentMonitor()) instanceof NullComponentMonitor);
		sessionPico.as(Characteristics.GUARD).addComponent(Object.class);
		objectAdapter = sessionPico.getComponentAdapter(Object.class);
		assertNotNull(objectAdapter.findAdapterOfType(Guarded.class));
		
		MutablePicoContainer requestPico = DefaultScopedContainerBuilderTestCase.getRequestContainer(containers);
		assertNotNull(requestPico);
		assertTrue(requestPico.changeMonitor(new NullComponentMonitor()) instanceof NullComponentMonitor);
		requestPico.as(Characteristics.GUARD).addComponent(Object.class);
		objectAdapter = requestPico.getComponentAdapter(Object.class);
		assertNotNull(objectAdapter.findAdapterOfType(Guarded.class));
	}

	private void addNullBehaviorExpectations() {
		context.checking(new Expectations() {{
			oneOf(servletContext).getInitParameter(with(same(REQUEST_BEHAVIORS)));
			will((returnValue(null)));
			
			oneOf(servletContext).getInitParameter(with(same(SESSION_BEHAVIORS)));
			will((returnValue(null)));
			
			oneOf(servletContext).getInitParameter(with(same(APP_BEHAVIORS)));
			will((returnValue(null)));
			
			oneOf(servletContext).getInitParameter(with(same(LIFECYCLE_STRATEGY)));
			will((returnValue(null)));

		}});
	}
	
	private void addNullParentPicoContainerExpectations() {
		context.checking(new Expectations() {{
			oneOf(servletContext).getInitParameter(with(same(PARENT_PICO)));
			will(returnValue(null));
		}});
	}
	
	@SuppressWarnings("deprecation")
	private void addNullStateLessExpectations() {
		context.checking(new Expectations() {{
			oneOf(servletContext).getInitParameter(with(same(PicoServletContainerListener.STATELESS_WEBAPP)));
			will((returnValue(null)));	
			
			oneOf(servletContext).getInitParameter(with(same(STATELESS_WEBAPP)));
			will((returnValue(null)));			
		}});
			
	}
	
	private void addNullComponentMonitorExpectations() {
		context.checking(new Expectations() {{
			oneOf(servletContext).getInitParameter(with(same(REQUEST_COMPONENT_MONITORS)));
			will((returnValue(null)));
		
			oneOf(servletContext).getInitParameter(with(same(SESSION_COMPONENT_MONITORS)));
			will((returnValue(null)));
		
			
			oneOf(servletContext).getInitParameter(with(same(APP_COMPONENT_MONITORS)));
			will((returnValue(null)));
		}});
	}
	
	private void addNullExpectationsOnProfilingJavaSecurity() {
		context.checking(new Expectations() {{
			oneOf(servletContext).getInitParameter(with(same(SECURITY_PROFILING)));
			will(returnValue(null));
		}});

	}

	
	@Test
	@SuppressWarnings("deprecation")
	public void testStatelessWebappUsingNewParameter() {
		context.checking(new Expectations() {{
			oneOf(servletContext).getInitParameter(with(same(PicoServletContainerListener.STATELESS_WEBAPP)));
			will((returnValue(null)));	
			
			oneOf(servletContext).getInitParameter(with(same(STATELESS_WEBAPP)));
			will((returnValue("true")));

		}});
		
		addNullComponentMonitorExpectations();
		addNullBehaviorExpectations();
		addNullParentPicoContainerExpectations();
		addNullExpectationsOnProfilingJavaSecurity();
		
		PicoServletParameterProcessor paramProcessor = new PicoServletParameterProcessor();
		AbstractScopedContainerBuilder containerBuilder = paramProcessor.processContextParameters(servletContext);
		ScopedContainers containers =  containerBuilder.makeScopedContainers(paramProcessor.isStateless());
		
		MutablePicoContainer appPico = DefaultScopedContainerBuilderTestCase.getAppContainer(containers);
		assertNotNull(appPico);		
		
		MutablePicoContainer sessionPico = DefaultScopedContainerBuilderTestCase.getSessionContainer(containers);
		assertNull(sessionPico);		
		
		MutablePicoContainer requestPico = DefaultScopedContainerBuilderTestCase.getRequestContainer(containers);
		assertNotNull(requestPico);		
	}
	
	
	@Test
	@SuppressWarnings("deprecation")
	public void testStatelessWebappUsingDeprecatedParameter() {
		context.checking(new Expectations() {{
			oneOf(servletContext).getInitParameter(with(same(PicoServletContainerListener.STATELESS_WEBAPP)));
			will((returnValue("true")));	
		}});
		
		addNullComponentMonitorExpectations();
		addNullBehaviorExpectations();
		addNullParentPicoContainerExpectations();
		addNullExpectationsOnProfilingJavaSecurity();

		
		PicoServletParameterProcessor paramProcessor = new PicoServletParameterProcessor();
		AbstractScopedContainerBuilder containerBuilder = paramProcessor.processContextParameters(servletContext);
		ScopedContainers containers =  containerBuilder.makeScopedContainers(paramProcessor.isStateless());
		
		MutablePicoContainer appPico = DefaultScopedContainerBuilderTestCase.getAppContainer(containers);
		assertNotNull(appPico);		
		
		MutablePicoContainer sessionPico = DefaultScopedContainerBuilderTestCase.getSessionContainer(containers);
		assertNull(sessionPico);		
		
		MutablePicoContainer requestPico = DefaultScopedContainerBuilderTestCase.getRequestContainer(containers);
		assertNotNull(requestPico);		
	}
	
	@Test
	public void testSettingOfMonitors() {
		context.checking(new Expectations() {{
			oneOf(servletContext).getInitParameter(with(same(REQUEST_COMPONENT_MONITORS)));
			will((returnValue(Slf4jMonitorProvider.class.getName())));
		
			oneOf(servletContext).getInitParameter(with(same(SESSION_COMPONENT_MONITORS)));
			will((returnValue(CommonsLoggingMonitorProvider.class.getName())));
		
			
			oneOf(servletContext).getInitParameter(with(same(APP_COMPONENT_MONITORS)));
			will((returnValue(Slf4jMonitorProvider.class.getName())));		
		}});
		
		addNullStateLessExpectations();
		addNullBehaviorExpectations();		
		addNullParentPicoContainerExpectations();
		addNullExpectationsOnProfilingJavaSecurity();
	
		PicoServletParameterProcessor paramProcessor = new PicoServletParameterProcessor();
		AbstractScopedContainerBuilder containerBuilder = paramProcessor.processContextParameters(servletContext);
		ScopedContainers containers =  containerBuilder.makeScopedContainers(paramProcessor.isStateless());
		
		MutablePicoContainer appPico = DefaultScopedContainerBuilderTestCase.getAppContainer(containers);
		assertTrue(appPico.changeMonitor(new NullComponentMonitor()) instanceof Slf4jComponentMonitor);
		assertNotNull(appPico);		
		
		MutablePicoContainer sessionPico = DefaultScopedContainerBuilderTestCase.getSessionContainer(containers);
		assertNotNull(sessionPico);		
		assertTrue(sessionPico.changeMonitor(new NullComponentMonitor()) instanceof CommonsLoggingComponentMonitor);
		
		MutablePicoContainer requestPico = DefaultScopedContainerBuilderTestCase.getRequestContainer(containers);
		assertTrue(requestPico.changeMonitor(new NullComponentMonitor()) instanceof Slf4jComponentMonitor);
		assertNotNull(requestPico);				
	}		
	
	@Test
	public void testSettingOfComponentFactories() {
		context.checking(new Expectations() {{
			oneOf(servletContext).getInitParameter(with(same(REQUEST_BEHAVIORS)));
			will((returnValue(Locking.class.getName())));
			
			oneOf(servletContext).getInitParameter(with(same(SESSION_BEHAVIORS)));
			will((returnValue(Synchronizing.class.getName())));
			
			oneOf(servletContext).getInitParameter(with(same(APP_BEHAVIORS)));
			will((returnValue(Locking.class.getName() + "," + JMXExposing.class.getName())));
			
			oneOf(servletContext).getInitParameter(with(same(LIFECYCLE_STRATEGY)));
			will((returnValue(null)));			
		}});
		
		addNullStateLessExpectations();
		
		addNullComponentMonitorExpectations();
		addNullParentPicoContainerExpectations();
		addNullExpectationsOnProfilingJavaSecurity();

		PicoServletParameterProcessor paramProcessor = new PicoServletParameterProcessor();
		AbstractScopedContainerBuilder containerBuilder = paramProcessor.processContextParameters(servletContext);
		ScopedContainers containers =  containerBuilder.makeScopedContainers(paramProcessor.isStateless());		
		
		MutablePicoContainer appPico = DefaultScopedContainerBuilderTestCase.getAppContainer(containers);
		assertNotNull(appPico);
		appPico.addComponent(Object.class);
		ComponentAdapter<?> objectAdapter = appPico.getComponentAdapter(Object.class);
		assertNotNull(objectAdapter.findAdapterOfType(JMXExposed.class));
		
		
		MutablePicoContainer sessionPico = DefaultScopedContainerBuilderTestCase.getSessionContainer(containers);
		assertNotNull(sessionPico);
		sessionPico.addComponent(Object.class);
		objectAdapter = sessionPico.getComponentAdapter(Object.class);
		assertNotNull(objectAdapter.findAdapterOfType(Synchronized.class));	
		
		
		MutablePicoContainer requestPico = DefaultScopedContainerBuilderTestCase.getRequestContainer(containers);
		assertNotNull(requestPico);
		requestPico.addComponent(Object.class);
		objectAdapter = requestPico.getComponentAdapter(Object.class);
		assertNotNull(objectAdapter.findAdapterOfType(Locked.class));			
	}
	
	@Test
	public void testDefaultParentPicoIsEmptyPicoContainer() {
		addNullStateLessExpectations();
		addNullComponentMonitorExpectations();
		addNullParentPicoContainerExpectations();
		addNullBehaviorExpectations();			
		addNullExpectationsOnProfilingJavaSecurity();

		PicoServletParameterProcessor paramProcessor = new PicoServletParameterProcessor();
		AbstractScopedContainerBuilder containerBuilder = paramProcessor.processContextParameters(servletContext);
		assertNotNull(containerBuilder);
		assertTrue(containerBuilder.getParentContainer() instanceof EmptyPicoContainer);
	}
	
	@Test
	public void testDifferentParentPicocontainerIsProcessed() {
		context.checking(new Expectations() {{
			oneOf(servletContext).getInitParameter(with(same(PARENT_PICO)));
			will(returnValue(TestPicoProvider.class.getName()));
		}});
		addNullStateLessExpectations();
		addNullComponentMonitorExpectations();	
		addNullBehaviorExpectations();	
		addNullExpectationsOnProfilingJavaSecurity();

		PicoServletParameterProcessor paramProcessor = new PicoServletParameterProcessor();
		AbstractScopedContainerBuilder containerBuilder = paramProcessor.processContextParameters(servletContext);
		assertSame(TestPicoProvider.INSTANCE, containerBuilder.getParentContainer());
	}
	
	@Test
	public void testSecurityProfilingTurnedOffByDefault() {
		addNullStateLessExpectations();
		addNullComponentMonitorExpectations();	
		addNullBehaviorExpectations();	
		addNullExpectationsOnProfilingJavaSecurity();
		addNullParentPicoContainerExpectations();

		PicoServletParameterProcessor paramProcessor = new PicoServletParameterProcessor();
		paramProcessor.processContextParameters(servletContext);
		assertNull(System.getSecurityManager());
	}
	
	@Test
	public void testTurningOnSecurityProfiling() {
		context.checking(new Expectations() {{
			oneOf(servletContext).getInitParameter(with(same(SECURITY_PROFILING)));
			will(returnValue("true"));
		}});	
		
		try {
			addNullStateLessExpectations();
			addNullComponentMonitorExpectations();	
			addNullBehaviorExpectations();	
			addNullParentPicoContainerExpectations();

			PicoServletParameterProcessor paramProcessor = new PicoServletParameterProcessor();
			paramProcessor.processContextParameters(servletContext);
			assertTrue(System.getSecurityManager() instanceof ProfilingSecurityManager);
		} finally {
			System.setSecurityManager(null);
		}	
		
	}
	

}
