package org.picocontainer.defaults.issues;

import org.junit.Test;import static org.junit.Assert.assertFalse;import static org.junit.Assert.assertSame;

import org.picocontainer.injectors.ConstructorInjector;
import org.picocontainer.injectors.MultiArgMemberInjector;
import org.picocontainer.behaviors.Cached;
import org.picocontainer.behaviors.Stored;

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
		Cached<Foo> adapter = new Cached<Foo>(injector);
		assertSame(adapter, adapter.findAdapterOfType(Stored.class));
		assertSame(injector, adapter.findAdapterOfType(MultiArgMemberInjector.class));
	}

}