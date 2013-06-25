package com.picocontainer.defaults.issues;

import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.picocontainer.behaviors.Caching;
import com.picocontainer.behaviors.Storing;
import com.picocontainer.injectors.ConstructorInjection;
import com.picocontainer.injectors.MultiArgMemberInjector;

public class Issue0352TestCase {

    public static class Foo {
	}

	// This test failed before patch (see revision #5396)
	@Test
	public void testShouldFindSupertypeOfAdapterOnAbstractAdapterDerivative() {
		ConstructorInjection.ConstructorInjector<Foo> injector = new ConstructorInjection.ConstructorInjector<Foo>("key", Foo.class);
		assertSame(injector, injector.findAdapterOfType(MultiArgMemberInjector.class));
	}

	// This test works
	@Test
	public void testShouldFindSupertypeOfAdapterOnAbstractBehaviorDerivative() {
		ConstructorInjection.ConstructorInjector<Foo> injector = new ConstructorInjection.ConstructorInjector<Foo>("key", Foo.class);
		Caching.Cached<Foo> adapter = new Caching.Cached<Foo>(injector);
		assertSame(adapter, adapter.findAdapterOfType(Storing.Stored.class));
		assertSame(injector, adapter.findAdapterOfType(MultiArgMemberInjector.class));
	}

}