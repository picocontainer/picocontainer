package org.picocontainer;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Provider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.adapters.SimpleNamedBindingAnnotationTestCase.Apple;
import org.picocontainer.containers.JSRPicoContainerTestCase.ThreeAProvider;
import org.picocontainer.containers.JSRPicoContainerTestCase.ThreeCProvider;
import org.picocontainer.containers.JSRPicoContainerTestCase.C;

import com.googlecode.jtype.Generic;

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
	
	
	
	
	@Test
	public void testIsAssignableWithACompoundTypeAndANormalClass() {
		Generic<?> listType = Generic.get(List.class, String.class);
		assertFalse(JTypeHelper.isAssignableFrom(listType, String.class));
		assertTrue(JTypeHelper.isAssignableFrom(listType, List.class));
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
		public void doSomething(Provider<C> threeCProvider) {
			
		}
	}
	
	@Test
	public void testCombinationOfTypeFromOtherTests() throws NoSuchMethodException, SecurityException {
		Generic<ThreeAProvider> aProvider = Generic.get(ThreeAProvider.class);
		
		assertFalse(JTypeHelper.isAssignableFrom(aProvider, ThreeCProvider.class));
		
		
		Class<?> paramType = TestArg.class.getMethod("doSomething", Provider.class).getParameterTypes()[0];
		Generic<?> argType = Generic.get(paramType);
		
		assertFalse(JTypeHelper.isAssignableFrom(argType, ThreeAProvider.class));
	}


}
