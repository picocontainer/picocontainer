package org.picocontainer.containers;

import static org.junit.Assert.*;

import javax.inject.Named;
import javax.inject.Provider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.parameters.JSR330ComponentParameter;

public class JSRPicoContainerTestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	public static class A {
		
	}
	
	@Named("test")
	public static class B {
		public int number = 0;
		
	}
	
	@SomeQualifier
	public static class C {
		
	}
	
	@Test
	public void testJSR330KeyDetermination() {
		MutablePicoContainer mpc = new JSRPicoContainer(new PicoBuilder().withCaching().withJavaEE5Lifecycle().build());
		
		mpc.addComponent(A.class)
			.addComponent(B.class)
			.addComponent(C.class);
		
		assertNotNull(mpc.getComponentAdapter(A.class));
		assertNotNull(mpc.getComponentAdapter("test"));
		assertNull(mpc.getComponentAdapter(B.class));
		assertNull(mpc.getComponentAdapter(C.class));
		assertNotNull(mpc.getComponentAdapter(SomeQualifier.class.getName()));
	}
	
	
	
	public static class ProviderTest {
		public ProviderTest(
				@Named("test") Provider<B> b, 
				@SomeQualifier Provider<C> c) {
			
		}
		
		public ProviderTest() {
			fail("Shouldn't be called");
		}
	}
	
	
	@Test
	public void testAddProvidersWithNonAnnotatedKeys() {
		
		Provider<B> provider1 = new Provider<B>() {
			 public B get() {
				 B returnValue =  new B();
				 returnValue.number = 2;
				 return returnValue;
			 }
		};
		
		Provider<B> provider2 = new Provider<B>() {

			public B get() {
				 B returnValue =  new B();
				 returnValue.number = 42;
				 return returnValue;
			}
			
		};
		
		MutablePicoContainer mpc = new JSRPicoContainer(new PicoBuilder().withCaching().withJavaEE5Lifecycle().build());
		mpc.addProvider("provider1", provider1);
		mpc.addProvider("provider2", provider2);
		assertTrue(mpc.getComponentAdapter("provider1") != null);
		assertTrue(mpc.getComponentAdapter("provider2") != null);
	}
	
	
	
	@Named("provider1")
	public static class Provider1 implements javax.inject.Provider<B> {
		 public B get() {
			 B returnValue =  new B();
			 returnValue.number = 2;
			 return returnValue;
		 }
	}
	
	@Named("provider2")
	public static class Provider2 implements javax.inject.Provider<B> {
		public B get() {
			 B returnValue =  new B();
			 returnValue.number = 42;
			 return returnValue;
		}
	}

	@Test
	public void testAddProvidersWithAnnotatedKeys() {
		
		Provider<B> provider1 = new Provider1();
		Provider<B> provider2 = new Provider2();
		
		
		MutablePicoContainer mpc = new JSRPicoContainer(new PicoBuilder().withCaching().withJavaEE5Lifecycle().build());
		mpc.addProvider(provider1);
		mpc.addProvider(provider2);
		assertTrue(mpc.getComponentAdapter("provider1") != null);
		assertTrue(mpc.getComponentAdapter("provider2") != null);
	}
	
	
	public static class ProviderTestTwo {
		public ProviderTestTwo(A a, 
				@Named("test") Provider<B> b, 
				@Named("test2") Provider<B> c,  
				@SomeQualifier Provider<C> d,
				Provider<C> e) {
			
		}
		
		public ProviderTestTwo() {
			fail("Shouldn't be called");
		}
	}
	
	
	public static class RegistrationTypeProvider1 implements Provider<B> {
		 public B get() {
			 B returnValue =  new B();
			 returnValue.number = 2;
			 return returnValue;
		 }
	}

	@Named("test2")
	public static class RegistrationTypeProvider2 implements Provider<B> {
		public B get() {
			 B returnValue =  new B();
			 returnValue.number = 42;
			 return returnValue;
		}
		
	}	
	
	@SomeQualifier
	public static class CProvider implements Provider<C> {

		public C get() {
			C returnValue = (C) new JSRPicoContainerTestCase.C();
			return returnValue;
		}
		
	}
	
	public static class C2Provider implements Provider<C> {
		public C get() {
			C returnValue = new C();
			return returnValue;
		}		
	}

	
	@Test
	public void testConstructorInjectionAndMixedProviders() {
		
		MutablePicoContainer mpc = new JSRPicoContainer(new PicoBuilder().withCaching().withJavaEE5Lifecycle().build());
	
		
		 mpc
		  						//Mix of normal components and Providers
		 	.addComponent(A.class)
			.addComponent(ProviderTestTwo.class, ProviderTestTwo.class, 
						new JSR330ComponentParameter(), 
						new JSR330ComponentParameter(), 
						new JSR330ComponentParameter(), 
						new JSR330ComponentParameter(),
						new JSR330ComponentParameter())  //The Test
			.addProvider("test",  new RegistrationTypeProvider1())	//Manual key name "test"
			.addProvider(new RegistrationTypeProvider2()) //@Named "test2"
			.addProvider(new CProvider())  //@SomeQualifier 
			.addProvider(new C2Provider()) //No Qualifier: In case of ambiguity, this is the one that's chosen.
			;
		
		ProviderTestTwo testObject = mpc.getComponent(ProviderTestTwo.class);
		assertTrue(testObject != null);
	}
	
	
	public static class ProvderTestThree {
		public ProvderTestThree(Provider<C> arg) {
			assertNotNull(arg);
		}
	}
	
	public static class ThreeCProvider implements Provider<C> {

		public C get() {
			return new C();
		}
		
	}
	
	public static class ThreeAProvider implements Provider<A> {

		public A get() {
			return new A();
		}
		
	}
	
	@Test
	public void testConstructorInjectionCanDifferentiateDifferentGenericTypesOnProviders() {
		MutablePicoContainer mpc = new JSRPicoContainer(new PicoBuilder().withCaching().withJavaEE5Lifecycle().build());
		
		
		 mpc.addComponent(ProvderTestThree.class, ProvderTestThree.class, 
						new JSR330ComponentParameter())  //The Test
			.addProvider(new ThreeCProvider())  //No Qualifier
			.addProvider(new ThreeAProvider()) //No Qualifier  Generic type provided should short it out
			;
		 
		 ProvderTestThree testThree = mpc.getComponent(ProvderTestThree.class);
		 assertNotNull(testThree);

	}

}
