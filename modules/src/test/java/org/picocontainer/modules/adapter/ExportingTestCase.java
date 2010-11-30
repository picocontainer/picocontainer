package org.picocontainer.modules.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.Disposable;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.Startable;
import org.picocontainer.behaviors.Caching.Cached;
import org.picocontainer.visitors.VerifyingVisitor;

public class ExportingTestCase {

	private MutablePicoContainer parent;
	
	private MutablePicoContainer child;
	
	@Before
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setUp() throws Exception {
		parent = new PicoBuilder().withCaching().withLifecycle().build();
		child = new PicoBuilder(parent).withCaching().withLifecycle().build();
		
		child.addComponent(A.class)
			.addComponent(LifecycleTest.class);
		
		parent.as(Characteristics.NONE)
			.addAdapter(new Publishing(child, child.getComponentAdapter(LifecycleTest.class)));
	}

	@After
	public void tearDown() throws Exception {
		child = null;
		parent = null;
	}

	@Test
	public void testGetComponentKey() {
		assertEquals(LifecycleTest.class, parent.getComponentAdapter(LifecycleTest.class).getComponentKey());
	}

	@Test
	public void testGetComponentImplementation() {
		assertEquals(LifecycleTest.class, parent.getComponentAdapter(LifecycleTest.class).getComponentImplementation());
	}

	@Test
	public void testGetComponentInstance() {
		LifecycleTest instance = parent.getComponent(LifecycleTest.class);
		assertNotNull(instance);
		
		//Verify cached still works on child.
		LifecycleTest instance2 = parent.getComponent(LifecycleTest.class);
		assertTrue(instance == instance2);
		
		LifecycleTest instance3 = child.getComponent(LifecycleTest.class);
		assertTrue(instance == instance3);
	}

	@Test
	public void testVerify() {
		VerifyingVisitor vv = new VerifyingVisitor();
		vv.traverse(parent);
	}

	@Test
	public void testGetDelegate() {
		assertTrue(parent.getComponentAdapter(LifecycleTest.class).getDelegate() 
				== child.getComponentAdapter(LifecycleTest.class).getDelegate());
	}

	@Test
	public void testFindAdapterOfType() {
		assertTrue(parent.getComponentAdapter(LifecycleTest.class).findAdapterOfType(Cached.class) 
				== child.getComponentAdapter(LifecycleTest.class).findAdapterOfType(Cached.class));
	}

	@Test
	public void testGetDescriptor() {
		assertNotNull(parent.getComponentAdapter(LifecycleTest.class).getDescriptor());
	}
	
	@Test
	public void testLifecycleIntegration() {
		LifecycleTest lt = parent.getComponent(LifecycleTest.class);
		assertFalse(lt.started);
		assertFalse(lt.stopped);
		assertFalse(lt.disposed);

		parent.start();
		assertFalse(lt.started);
		assertFalse(lt.stopped);
		assertFalse(lt.disposed);
		
		child.start();
		assertTrue(lt.started);
		assertFalse(lt.stopped);
		assertFalse(lt.disposed);
		
		parent.stop();
		assertTrue(lt.started);
		assertFalse(lt.stopped);
		assertFalse(lt.disposed);
		
		child.stop();
		assertFalse(lt.started);
		assertTrue(lt.stopped);
		assertFalse(lt.disposed);
		
		parent.dispose();
		assertFalse(lt.started);
		assertTrue(lt.stopped);
		assertFalse(lt.disposed);
		
		child.dispose();
		assertFalse(lt.started);
		assertTrue(lt.stopped);
		assertTrue(lt.disposed);
		
	}
	
	public static class A {
		
	}

	public static class LifecycleTest implements Startable, Disposable {
		public boolean started = false;
		public boolean stopped = false;
		public boolean disposed = false;

		public final A component;

		public LifecycleTest(A component) {
			this.component = component;
		}

		public void dispose() {
			disposed = true;
		}

		public void start() {
			started = true;
			stopped = false;
		}

		public void stop() {
			started = false;
			stopped = true;
		}		
	}
	
}
