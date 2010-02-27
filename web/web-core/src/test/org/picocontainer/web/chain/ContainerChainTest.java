package org.picocontainer.web.chain;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.Startable;
import org.picocontainer.web.chain.ContainerChain;

/**
 * test capabilities of container chains
 * @author Konstantin Pribluda
 */
public class ContainerChainTest {

	
	/**
	 * empty chain shall 
	 * @throws Exception
	 */
	@Test public void testEmptyChain()  {
		ContainerChain chain = new ContainerChain();
		assertNull(chain.getLast());
	}
	
	
	@Test public void testAddingContainerSetsItToLast() {
		ContainerChain chain = new ContainerChain();
		PicoContainer container = new DefaultPicoContainer();
		
		chain.addContainer(container);
		
		assertSame(container,chain.getLast());
		
	}
	
	
	@Test public void testStartStopPropagation() {
		ContainerChain chain = new ContainerChain();
		MutablePicoContainer first = new DefaultPicoContainer();
	
		first.addComponent(new MockStartable());
		chain.addContainer(first);
		
		MutablePicoContainer second = new DefaultPicoContainer();
		second.addComponent(new MockStartable());
		
		chain.addContainer(second);
		
		chain.start();
		chain.stop();
		
		
		MockStartable startable = first.getComponent(MockStartable.class);
		assertTrue(startable.isStarted());
		assertTrue(startable.isStopped());
		
		startable = second.getComponent(MockStartable.class);
		assertTrue(startable.isStarted());
		assertTrue(startable.isStopped());
		
	}
	
	class MockStartable implements Startable {
		boolean started = false;
		boolean stopped = false;

		public void start() {
			setStarted(true);
		}

		public void stop() {
			setStopped(true);
		}
		
		/**
		 * @return Returns the started.
		 */
		public boolean isStarted() {
			return started;
		}
		/**
		 * @param started The started to set.
		 */
		public void setStarted(boolean started) {
			this.started = started;
		}
		/**
		 * @return Returns the stopped.
		 */
		public boolean isStopped() {
			return stopped;
		}
		/**
		 * @param stopped The stopped to set.
		 */
		public void setStopped(boolean stopped) {
			this.stopped = stopped;
		}
	}
}
