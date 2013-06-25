package com.picocontainer.injectors;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.picocontainer.injectors.Jsr330ConstructorInjection;
import com.picocontainer.monitors.NullComponentMonitor;

public class Jsr330ConstructorInjectionTestCase {


	public static class Something {

		public Something() {

		}
	}



	public static class SomethingElse {

		/**
		 * Shouldn't be accessible to ConstructorInjection, SHOULD
		 * be accessible to JSR330 injection.  (WHY JSR, WHY?!?!)
		 */
		private SomethingElse() {

		}

	}

	@Test
	public void testBothPublicAndPrivateConstructorsMayBeAcessed() {
		Jsr330ConstructorInjection.ConstructorInjectorWithForcedPublicCtors<Something> cica =
				new Jsr330ConstructorInjection.ConstructorInjectorWithForcedPublicCtors<Something>(
							false,
							new NullComponentMonitor(),
							false,
							Something.class,
							Something.class,
							null);

		assertNotNull(cica.getComponentInstance(null, null));


		Jsr330ConstructorInjection.ConstructorInjectorWithForcedPublicCtors<SomethingElse> cica2 =
				new Jsr330ConstructorInjection.ConstructorInjectorWithForcedPublicCtors<SomethingElse>(
							false,
							new NullComponentMonitor(),
							false,
							SomethingElse.class,
							SomethingElse.class,
							null);

		assertNotNull(cica2.getComponentInstance(null, null));

	}

}
