package org.picocontainer.parameters;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import com.googlecode.jtype.Generic;

public class AnnotationCollectionComponentParameterTestCase {

	@Singleton
	public static class A {
		
	}
	
	@Singleton
	public static class B {
		
	}
	
	public static class C {
		
	}
	
	@Singleton
	public static class D {
		
	}
	
	public static class SingletonRegistryWithArray {
		public Object[] singletons;
		public SingletonRegistryWithArray(Object[] allSingletons) {
			this.singletons = allSingletons;
		}
	}
	
	public static class SingletonRegistryWithList {
		public List<Object> singletons;
		public SingletonRegistryWithList(List<Object> allSingletons) {
			this.singletons = allSingletons;
		}
	}
	
	public static class SingletonRegistryWithMap {
		public Map singletons;
		public SingletonRegistryWithMap(Map allSingletons) {
			this.singletons = allSingletons;
		}
	}

	protected MutablePicoContainer buildContainer() {
		MutablePicoContainer pico = new PicoBuilder().build();
		pico.addComponent(A.class)
			.addComponent(B.class)
			.addComponent(C.class);
		
		MutablePicoContainer child = pico.makeChildContainer();
		child.addComponent(D.class)
			 .addComponent(SingletonRegistryWithArray.class, SingletonRegistryWithArray.class, 
					 	new AnnotationCollectionComponentParameter(Singleton.class))
			 .addComponent(SingletonRegistryWithList.class, SingletonRegistryWithList.class,
					 	new AnnotationCollectionComponentParameter(Singleton.class))
			 .addComponent(SingletonRegistryWithMap.class, SingletonRegistryWithMap.class,					
							 new ComponentParameter(new AnnotationCollectionComponentParameter(Singleton.class,
									 Object.class, 
									 Generic.get(Object.class),false))
							 );
		
		return child;
		
	}
	
	@Test
	public void testPrimitiveArrayCollecting() {
		MutablePicoContainer pico = buildContainer();
		SingletonRegistryWithArray test = pico.getComponent(SingletonRegistryWithArray.class);
		Set<String> names = new HashSet<String>();
		for (Object eachItem : test.singletons) {
			names.add(eachItem.getClass().getName());
		}
		assertTrue(names.contains(A.class.getName()));
		assertTrue(names.contains(B.class.getName()));
		assertFalse(names.contains(C.class.getName()));
		assertTrue(names.contains(D.class.getName()));
	}
	
	
	
	@Test
	public void testCollectionCollecting() {
		MutablePicoContainer pico = buildContainer();
		SingletonRegistryWithList test = pico.getComponent(SingletonRegistryWithList.class);
		Set<String> names = new HashSet<String>();
		for (Object eachItem : test.singletons) {
			names.add(eachItem.getClass().getName());
		}
		assertTrue(names.contains(A.class.getName()));
		assertTrue(names.contains(B.class.getName()));
		assertFalse(names.contains(C.class.getName()));
		assertTrue(names.contains(D.class.getName()));
	
	}
	
	@Test
	public void testMapCollecting() {
		MutablePicoContainer pico = buildContainer();
		SingletonRegistryWithMap test = pico.getComponent(SingletonRegistryWithMap.class);
		Set<String> names = new HashSet<String>();
		for (Object eachItem : test.singletons.values()) {
			names.add(eachItem.getClass().getName());
		}
		assertTrue(names.contains(A.class.getName()));
		assertTrue(names.contains(B.class.getName()));
		assertFalse(names.contains(C.class.getName()));
		assertTrue(names.contains(D.class.getName()));
		
	}

}
