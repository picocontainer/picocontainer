package com.picocontainer.examples.security;

import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.security.SecurityWrappingPicoContainer;

public class Main {


	public static void main(String[] args) {
		MutablePicoContainer pico = new PicoBuilder().withCaching().withLifecycle().build();
		pico.addComponent(A.class)
			.addComponent(B.class);

		SecurityWrappingPicoContainer noPermissionPico = new SecurityWrappingPicoContainer("", pico);
		
		SecurityWrappingPicoContainer readOnlyPermissionPico = new SecurityWrappingPicoContainer("readOnly", pico);
		
		SecurityWrappingPicoContainer readWritePermissionPico = new SecurityWrappingPicoContainer("readwriteScope", pico);
		
		testNoPermissionPico(noPermissionPico);
		testReadOnlyPico(readOnlyPermissionPico);
		testReadWritePermissionPico(readWritePermissionPico);
		System.out.println("----------------------");
		System.out.println("\tSuccess!");
		System.out.println("----------------------");
		
	}
	
	

	private static void testNoPermissionPico(SecurityWrappingPicoContainer pico) {
		System.out.println("--- Testing No Permission Pico ---");
		try {
			//Should be a failure.
			pico.getComponent(A.class);
			throw new RuntimeException("Failed to have error thrown on addComponent for no permission sealed container");
		} catch (SecurityException e) {
			assert e.getMessage() != null;
		}
					
		
		try {
			//Should be a failure.
			pico.addComponent(C.class);
			throw new RuntimeException("Failed to have error thrown on addComponent for no permission sealed container");
		} catch (SecurityException e) {
			assert e.getMessage() != null;
		}
				
	}



	private static void testReadWritePermissionPico(SecurityWrappingPicoContainer pico) {
		System.out.println("--- Testing ReadWrite Permission Pico ---");
		A aComponent = pico.getComponent(A.class);
		if (aComponent == null) {
			throw new NullPointerException("aComponent");
		}
	
		pico.addComponent(C.class);
		
		C cComponent = pico.getComponent(C.class);
		if (cComponent == null) {
			throw new NullPointerException("cComponent");
		}
		
	}

	private static void testReadOnlyPico(SecurityWrappingPicoContainer pico) {
		System.out.println("--- Testing ReadOnly Permission Pico ---");

		//Should be ok
		A aComponent = pico.getComponent(A.class);
		if (aComponent == null) {
			throw new NullPointerException("aComponent");
		}
		
		try {
			//Should be a failure.
			pico.addComponent(C.class);
			throw new RuntimeException("Failed to have error thrown on addComponent for read only sealed container");
		} catch (SecurityException e) {
			assert e.getMessage() != null;
		}
		
	}

}
