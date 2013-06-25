package com.picocontainer.defaults.issues;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.Startable;
import com.picocontainer.behaviors.Caching;

public class Issue0353TestCase {

    public static class FooStartable implements Startable {
		public void start() {
			// empty
		}

		public void stop() {
			// empty
		}
	}

	@Test
	public void testIsStartedShouldNotThrowOnNonStartedComponent() {
		DefaultPicoContainer cont = new DefaultPicoContainer();
		cont.as(Characteristics.CACHE).addComponent(FooStartable.class);
		ComponentAdapter<?> adapter = cont.getComponentAdapter(FooStartable.class);
		Caching.Cached cached = adapter.findAdapterOfType(Caching.Cached.class);

               // this line throws - instead of returning false
		assertFalse(cached.isStarted());
	}

}
