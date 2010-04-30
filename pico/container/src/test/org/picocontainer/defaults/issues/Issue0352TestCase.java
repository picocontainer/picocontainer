package org.picocontainer.defaults.issues;

import org.junit.Test;import static org.junit.Assert.assertFalse;import static org.junit.Assert.assertSame;

import org.picocontainer.behaviors.Caching;
import org.picocontainer.behaviors.Storing;
import org.picocontainer.injectors.ConstructorInjector;
import org.picocontainer.injectors.MultiArgMemberInjector;

public class Issue0352TestCase {

    public static class Foo {
	}

	// This test failed before patch (see revision #5396)
	@Test
	public void testShouldFindSupertypeOfAdapterOnAbstractAdapterDerivative() {
		ConstructorInjector<Foo> injector = new ConstructorInjector<Foo>("key", Foo.class);
		assertSame(injector, injector.findAdapterOfType(MultiArgMemberInjector.class));
	}

	// This test works
	@Test
	public void testShouldFindSupertypeOfAdapterOnAbstractBehaviorDerivative() {
		ConstructorInjector<Foo> injector = new ConstructorInjector<Foo>("key", Foo.class);
		Caching.Cached<Foo> adapter = new Caching.Cached<Foo>(injector);
		assertSame(adapter, adapter.findAdapterOfType(Storing.Stored.class));
		assertSame(injector, adapter.findAdapterOfType(MultiArgMemberInjector.class));
	}

}