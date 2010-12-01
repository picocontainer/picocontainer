package org.picocontainer.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoContainer;
import org.picocontainer.script.util.MultiException;
import org.picocontainer.visitors.TraversalCheckingVisitor;

@RunWith(JMock.class)
public class AbstractContainerBuilderTestCase {

	private Mockery context = new JUnit4Mockery();
	
	private MutablePicoContainer simpleContainer = null;
	
	private AbstractContainerBuilder toTest = null;
	
	private MutablePicoContainer parentContainer = null;
	
	private ContainerCountingVistor containerCountingVisitor = null;

	
	static class ContainerCountingVistor extends TraversalCheckingVisitor {
		public int count = 0;
		
		
		@Override
		public boolean visitContainer(PicoContainer pico) {
			count++;
			return super.visitContainer(pico);
		}
		
	}
	
	@Before
	public void setUp() throws Exception {
		parentContainer = new PicoBuilder().withCaching().withLifecycle().build();
		simpleContainer = parentContainer.makeChildContainer();
		toTest = new AbstractContainerBuilder() {
			@Override
			protected PicoContainer createContainer(
					PicoContainer parentContainer, Object assemblyScope) {
				return simpleContainer;
			}			
		};
		
				
		containerCountingVisitor = new ContainerCountingVistor();
		
		//Verify
		containerCountingVisitor.traverse(parentContainer);
		assertEquals(2, containerCountingVisitor.count);
		containerCountingVisitor.traverse(simpleContainer);
		containerCountingVisitor.count = 0;
		containerCountingVisitor.traverse(simpleContainer);
		assertEquals(1, containerCountingVisitor.count);
		containerCountingVisitor.count = 0;
	}

	@After
	public void tearDown() throws Exception {
		parentContainer = null;
		simpleContainer = null;
	}

	@Test
	public void testKillContainerRemovesChildFromParentAndStopsChild() {
		parentContainer.start();
		toTest.killContainer(simpleContainer);
		assertTrue(simpleContainer.getLifecycleState().isDisposed());
		assertFalse(parentContainer.getLifecycleState().isStopped());
		assertFalse(parentContainer.getLifecycleState().isDisposed());
		
	}
	
	@Test
	@Ignore	
	/**
	 * FAILURE!  Marked in JIRA under PICO-376
	 */
	public void testKillContainerRemovesChildFromParent() {
		parentContainer.start();
		toTest.killContainer(simpleContainer);

		containerCountingVisitor.traverse(parentContainer);
	
		//Parent only
		assertEquals(1, containerCountingVisitor.count);				
	}
	
	@Test
	public void testKillContainerWithReadOnlyPicoContainerDoesntInvokeLifecycleOrParents() {
		final PicoContainer pico = context.mock(PicoContainer.class);
		context.checking(new Expectations() {{
			oneOf(pico).getParent();
			will(returnValue(parentContainer));
		}});

		toTest.killContainer(pico);
	}
	
	@Test
	public void testKillContainerAttemptsToProceedEvenOnErrorsButThrowsAfterwards() {
		final MutablePicoContainer parentPico = context.mock(MutablePicoContainer.class, "parentPico");
		final MutablePicoContainer mutablePico = context.mock(MutablePicoContainer.class);
		context.checking(new Expectations() {{
			oneOf(mutablePico).getParent();
			will(returnValue(parentPico));
			
			oneOf(mutablePico).stop();
			will(throwException(new IllegalStateException("Epic Fail")));
			
			oneOf(mutablePico).dispose();
			will(throwException(new IllegalStateException("Yup, another failure")));
			
			oneOf(parentPico).removeChildContainer(mutablePico);
			will(throwException(new NullPointerException("mutablePico")));
		}});
		
		try {
			toTest.killContainer(mutablePico);
		} catch (MultiException ex) {
			StringWriter stringWriter = new StringWriter();
			ex.printStackTrace(new PrintWriter(stringWriter));
			String result = stringWriter.toString();
			assertTrue("Got " + result, result.contains("Epic Fail"));
			assertTrue("Got " + result, result.contains("Yup, another failure"));
			assertTrue("Got " + result, result.contains("NullPointerException"));
		}
	}

	@Test
	public void testBuildContainerCallsPostConstructionActions() {
		final PostBuildContainerAction action = context.mock(PostBuildContainerAction.class);
		context.checking(new Expectations() {{
			oneOf(action).onNewContainer(simpleContainer);
		}});
		
		PicoContainer pico = toTest.setPostBuildAction(action)
			.buildContainer(this.parentContainer, "SOME_SCOPE", false);
		
		assertNotNull(pico);
	}
}
