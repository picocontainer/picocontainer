package com.picocontainer;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import org.junit.Test;
import com.picocontainer.adapters.SimpleNamedBindingAnnotationTestCase.Apple;
import com.picocontainer.containers.JSRPicoContainerTestCase.A;
import com.picocontainer.containers.JSRPicoContainerTestCase.C;
import com.picocontainer.containers.JSRPicoContainerTestCase.ThreeAProvider;
import com.picocontainer.containers.JSRPicoContainerTestCase.ThreeCProvider;
import com.picocontainer.defaults.issues.Issue0382TestCase.AcceptsParameterized;
import com.picocontainer.defaults.issues.Issue0382TestCase.StringParameterized;

import com.googlecode.jtype.Generic;
import com.picocontainer.JTypeHelper;

public class JTypeHelperTestCase {

	public interface HttpServletRequest {

	}

	@Test
	public void testIsAssignableFromForParameterizedInterfaces() {
		//Used to cause NPE.
		assertFalse(JTypeHelper.isAssignableFrom(new Generic<List<HttpServletRequest>>() {}, Apple.class));
	}

	@Test
	public void testIsAssignableFromWithTwoClasses() {
		Generic<List> listType = Generic.get(List.class);
		assertTrue(JTypeHelper.isAssignableFrom(listType, List.class));
	}


	@SuppressWarnings({ "serial", "rawtypes" })
	public static class RawTest extends ArrayList {

	}

	@SuppressWarnings({ "serial", "rawtypes" })
	public static class RawTestTwo extends ArrayList<String> {

	}

	@SuppressWarnings("rawtypes")
	public static class UntypedProvider implements Provider {

		public Object get() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	@Test
	public void testIsRawType() {
		assertTrue(JTypeHelper.isRawType(List.class));
		assertFalse(JTypeHelper.isRawType(ThreeAProvider.class));

		assertTrue(JTypeHelper.isRawType(UntypedProvider.class));
	}


	@Test
	public void testIsAssignableWithACompoundTypeAndANormalClass() {
		Generic<?> listType = Generic.get(List.class, String.class); //Testing:  List<String>

		assertFalse(JTypeHelper.isAssignableFrom(listType, String.class)); //Testing: List<String> = String



		assertTrue(JTypeHelper.isAssignableFrom(listType, List.class)); //List<String> = List

		//Proof that the above type should pass.
		@SuppressWarnings({ "rawtypes", "unchecked" })
		List<String> test = new ArrayList();
	}


	public static class SomeType implements Provider<String> {

		public String get() {
			return "Test";
		}

	}

	@Test
	public void testProviderType() {
		Generic<SomeType> providerType = Generic.get(SomeType.class);
		Generic<String> stringType = Generic.get(String.class);

		assertTrue(JTypeHelper.isAssignableFrom(stringType, String.class));
		assertFalse(JTypeHelper.isAssignableFrom(providerType, String.class));
		assertFalse(JTypeHelper.isAssignableFrom(stringType, SomeType.class));
		assertTrue(JTypeHelper.isAssignableFrom(providerType, SomeType.class));
	}


	public static class TestArg {
		public void doSomething(final Provider<C> threeCProvider) {

		}

		public void doSomethingElse(final Provider<A> threeAProvider) {

		}
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testCombinationOfTypeFromOtherTests() throws NoSuchMethodException, SecurityException {
		Generic<ThreeAProvider> aProvider = Generic.get(ThreeAProvider.class);
		Generic<ThreeCProvider> cProvider = Generic.get(ThreeCProvider.class);

		//Test reflection.
		assertTrue(JTypeHelper.isAssignableFrom(aProvider,  ThreeAProvider.class));
		assertTrue(JTypeHelper.isAssignableFrom(cProvider, ThreeCProvider.class));

		//Test as long as we're dealing standalone types.
		assertFalse(JTypeHelper.isAssignableFrom(aProvider, ThreeCProvider.class));
		assertFalse(JTypeHelper.isAssignableFrom(cProvider, ThreeAProvider.class));


		//Check if we get the type from a parameter.
		Type paramType = TestArg.class.getMethod("doSomething", Provider.class).getGenericParameterTypes()[0];

		assertTrue(paramType instanceof ParameterizedType);
		Generic<?> argType = Generic.get(paramType);

		Type paramTypeTwo = TestArg.class.getMethod("doSomethingElse", Provider.class).getGenericParameterTypes()[0];
		Generic<?> argTypeTwo = Generic.get(paramTypeTwo);

		//Check reflection
		assertTrue(JTypeHelper.isAssignableFrom(argTypeTwo, ThreeAProvider.class));
		assertTrue(JTypeHelper.isAssignableFrom(argType,ThreeCProvider.class));

		//Test opposite
		assertFalse(JTypeHelper.isAssignableFrom(argType, ThreeAProvider.class));
		assertFalse(JTypeHelper.isAssignableFrom(argTypeTwo, ThreeCProvider.class));

		//Test raw type CAN be assigned
		//Below is compiler proof that this test should pass.
		assertTrue(JTypeHelper.isAssignableFrom(argTypeTwo,  Provider.class));
		@SuppressWarnings("unchecked")
		Provider<C> c = new Provider() {

			public Object get() {
				// TODO Auto-generated method stub
				return null;
			}

		};
	}


	@Test
	public void testWildcardAssignment() {
		Type parameterType = AcceptsParameterized.class.getConstructors()[0].getGenericParameterTypes()[0];
		Generic<?> generic = Generic.get(parameterType);

		assertTrue(JTypeHelper.isAssignableFrom(generic, StringParameterized.class));
	}

}
