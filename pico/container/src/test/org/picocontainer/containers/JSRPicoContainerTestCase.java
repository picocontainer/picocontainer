package org.picocontainer.containers;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.junit.Ignore;
import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentFactory;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoContainer;
import org.picocontainer.parameters.ConstantParameter;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.JSR330ComponentParameter;
import org.picocontainer.parameters.MethodParameters;
import org.picocontainer.tck.AbstractPicoContainerTest;
import org.picocontainer.visitors.TraversalCheckingVisitor;

public class JSRPicoContainerTestCase extends AbstractPicoContainerTest {

	
	/**
	 * The one test that fails the TCK because it uses J2EE5 Lifecycle annotations instead
	 */
	@Override
	@Test
	@Ignore
	public void testContainerCascadesDefaultLifecycle() {
		
	}
	
	
	/**
	 * This one fails because of PICO-398
	 */
	@Test
	@Ignore
	public void testAcceptImplementsBreadthFirstStrategy() {
		
	}


	@Override
	protected MutablePicoContainer createPicoContainer(PicoContainer parent) {
		return new JSRPicoContainer(parent);
	}

	@Override
	protected Properties[] getProperties() {
		return new Properties[0];
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

	
	public static class ParameterTest {
		
		public Integer constructorArg;

		@Inject
		public String fieldArg;
		
		public String methodarg;
		
		@Inject
		public ParameterTest(Integer constructorArg) {
			this.constructorArg = constructorArg;
		}
		
		@Inject
		public void applyMethodArg(String value) {
			this.methodarg = value;
		}
	}
	
    @Test
    public void testConstructorAndFieldParametersGetTheAppropriateParameters() {
		MutablePicoContainer mpc = new JSRPicoContainer(new PicoBuilder().withCaching().withJavaEE5Lifecycle().build());
		mpc.addComponent(ParameterTest.class, ParameterTest.class, 
				new ConstructorParameters(new ConstantParameter(new Integer(3))),
				new FieldParameters[] {
					new FieldParameters("fieldArg", new ConstantParameter("Arg 1"))
				},
				new MethodParameters[] {	
					new MethodParameters("applyMethodArg",new ConstantParameter("Arg 2"))
				});
		
    	
		ParameterTest test = mpc.getComponent(ParameterTest.class);
		assertNotNull(test);
		assertEquals(3, test.constructorArg.intValue());
		assertEquals("Arg 1", test.fieldArg);
		assertEquals("Arg 2", test.methodarg);
    }
    
    @Test
    public void testCachingIsTurnedOffByDefault() {
    	MutablePicoContainer mpc = new JSRPicoContainer();
    	mpc.addComponent(Provider1.class);
    	
    	assertNotSame(mpc.getComponent(Provider1.class), mpc.getComponent(Provider1.class));
    }
    
    @Test
    public void testYouMayOptInCachingWithDefaultContainer() {
    	MutablePicoContainer mpc = new JSRPicoContainer();
    	mpc.as(Characteristics.CACHE).addComponent(Provider1.class);
    	
    	assertSame(mpc.getComponent(Provider1.class), mpc.getComponent(Provider1.class));
    	
    }
    
    
    @Singleton
    public static class TestSingletonAnnotation {
    	
    }
    
    
    @Test
    public void testSingletonAnnotationResultsInCacheProperty() {
    	MutablePicoContainer mpc = new JSRPicoContainer()
    		.addComponent(TestSingletonAnnotation.class);
    	
    	assertSame(mpc.getComponent(TestSingletonAnnotation.class), 
    			mpc.getComponent(TestSingletonAnnotation.class));
    	
    }

    
    @Test
    public void testSingletonWithDefinedPredefinedKey() {
    	MutablePicoContainer mpc = new JSRPicoContainer()
		.addComponent("test",TestSingletonAnnotation.class)
		.addComponent("test2", TestSingletonAnnotation.class);
	
    	assertSame(mpc.getComponent("test"), 
			mpc.getComponent("test"));
    	
    }

    
    
    public static class AdapterFactoryExaminingVisitor extends TraversalCheckingVisitor {

        private final List<Object> list;
        
        int containerCount = 0;

        public AdapterFactoryExaminingVisitor(List<Object> list) {
            this.list = list;
        }
     
        public void visitComponentFactory(ComponentFactory componentFactory) {
            list.add(componentFactory.getClass());
        }

		@Override
		public boolean visitContainer(PicoContainer pico) {
			//Don't hang up on wrapped containers
			if (! (pico instanceof DefaultPicoContainer) ) {
				return CONTINUE_TRAVERSAL;
			}
			
			if (containerCount == 0) {
				containerCount++;
				return CONTINUE_TRAVERSAL;
			}
			
			return ABORT_TRAVERSAL;
		}
        
        

    }
    
    @Test
    public void testMakeChildContainerPropagatesAdapterFactories() {
    	JSRPicoContainer pico = new JSRPicoContainer();
    	MutablePicoContainer child = pico.makeChildContainer();
    	
    	assertTrue(child != null);
    	assertTrue(child instanceof JSRPicoContainer);
    	
    	List<Object> parentList = new ArrayList<Object>();
    	List<Object> childList = new ArrayList<Object>();
    	
    	AdapterFactoryExaminingVisitor visitor = new AdapterFactoryExaminingVisitor(parentList);
    	visitor.traverse(pico);
    	
    	visitor = new AdapterFactoryExaminingVisitor(childList);
    	visitor.traverse(child);
    	
    	assertTrue(parentList.size() > 0);
    	assertEquals(Arrays.deepToString(parentList.toArray()), Arrays.deepToString(childList.toArray()));
    }
    
	
    public static class InjectionOrder2Parent {
    	
    	public static boolean injectSomethingCalled = false;
    	
		public static String injectedValue;
    	
    	@Inject
    	public static void injectSomthing(String injectedValue) {
    		InjectionOrder2Parent.injectedValue = injectedValue;
			assertFalse(InjectionOrder2Child.isInjected());
    		injectSomethingCalled = true;
    	}
    }
    
    
    public static class InjectionOrder2Child extends InjectionOrder2Parent {
    	@Inject
    	private static String something = null;
    	
    	public static boolean isInjected() {
    		return something != null;
    	}
    	
    	
    	@Inject
    	public void injectSomethingElse() {
    		assertNotNull(something);
    		assertNotNull(InjectionOrder2Parent.injectedValue);
    		assertTrue(InjectionOrder2Parent.injectSomethingCalled);
    	}
    }
    
    @Test
    public void testParentStaticJSRMethodsAreInjectedBeforeChildJSRFields() {
    	JSRPicoContainer pico = new JSRPicoContainer();
    	
    	pico.addComponent("Test", "This is a test")
    		.addComponent(InjectionOrder2Child.class);
    	
    	
    	InjectionOrder2Child child = pico.getComponent(InjectionOrder2Child.class);
    	assertNotNull(child);
    	
    	assertNotNull(InjectionOrder2Parent.injectedValue);
    	assertTrue(InjectionOrder2Child.isInjected());
    	
    }    
}
