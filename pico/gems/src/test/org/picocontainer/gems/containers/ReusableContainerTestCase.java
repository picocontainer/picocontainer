/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Mike Rimov                                               *
 *****************************************************************************/
package org.picocontainer.gems.containers;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Storing;
import org.picocontainer.tck.AbstractPicoContainerTest;

/**
 * @author Michael Rimov
 *
 */
public class ReusableContainerTestCase extends AbstractPicoContainerTest {

	@Override
	protected MutablePicoContainer createPicoContainer(final PicoContainer parent) {
		return new ReusablePicoContainer(parent);
	}

	@Override
	protected Properties[] getProperties() {
		return new Properties[0];
	}


	@Test public void testStopFlushesInstances() {
		MutablePicoContainer pico = new PicoBuilder().withCaching().implementedBy(ReusablePicoContainer.class).build();

		pico.addComponent(ArrayList.class, ArrayList.class, Parameter.ZERO);
		pico.addComponent(HashSet.class, HashSet.class, Parameter.ZERO);
		pico.start();
		//Force caching to take place.
		assertNotNull(pico.getComponent(ArrayList.class));
		assertNotNull(pico.getComponent(HashSet.class));
		pico.stop();

		ComponentAdapter<ArrayList> listCa = (ComponentAdapter<ArrayList>) pico.getComponentAdapter(ArrayList.class);
		Storing.Stored<ArrayList> storedValue = listCa.findAdapterOfType(Storing.Stored.class);
		assertNotNull(storedValue);
		assertNull(storedValue.getStoredObject());

		ComponentAdapter<HashSet> setCA = (ComponentAdapter<HashSet>) pico.getComponentAdapter(List.class);
		Storing.Stored<HashSet> setValue = listCa.findAdapterOfType(Storing.Stored.class);
		assertNotNull(setValue);
		assertNull(setValue.getStoredObject());
	
	}
	
	@Test public void testStopRemovesInstanceRegistrations() {
		MutablePicoContainer pico = createPicoContainer(null);
		pico.addComponent(List.class, new ArrayList());
		pico.addComponent(new HashSet());
		pico.start();
		assertNotNull(pico.getComponentAdapter(List.class));
		assertNotNull(pico.getComponentAdapter(HashSet.class));
		pico.stop();
		
		assertNull(pico.getComponentAdapter(List.class));
		assertNull(pico.getComponentAdapter(HashSet.class));
	}
	
	@Override
    @Test public void testContainerCascadesDefaultLifecycle() {
		//FIXME: This test is a problematic test case because for this object, Stop EXPLICITLY removes
		//instances.
    }
	
	private static final int ITERATIONS = 5000;
	
	@Test public void testPerformanceTestReusablePico() {
		long startTime = System.currentTimeMillis();
		MutablePicoContainer pico = new PicoBuilder()
			.implementedBy(ReusablePicoContainer.class)
			.withCaching()
			.build();
        addComponents(pico);
        for (int i = 0; i < ITERATIONS; i++) {
			pico.addComponent("TestKey", "This is a test")
				.addComponent("TestAnother", "This is a test");

			pico.start();			
			assertNotNull(pico.getComponent("TestKey"));
			assertNotNull(pico.getComponent(RuntimeException.class));
			assertNotNull(pico.getComponent(IllegalArgumentException.class));
			assertNotNull(pico.getComponent(IllegalStateException.class));
			assertNotNull(pico.getComponent(NoSuchFieldError.class));
			pico.stop();
		}
		
		pico.dispose();
		
		long endTime = System.currentTimeMillis();
		System.out.println("Completed iterations using ReusablePicoContainer.  Time: " + (endTime - startTime) + "m.s.");
	}

    private void addComponents(final MutablePicoContainer pico) {
        pico.addComponent(ArrayList.class, ArrayList.class, Parameter.ZERO)
            .addComponent(HashSet.class, HashSet.class, Parameter.ZERO)
            .addComponent(RuntimeException.class, RuntimeException.class, Parameter.ZERO)
            .addComponent(IllegalArgumentException.class,IllegalArgumentException.class, Parameter.ZERO)
            .addComponent(IllegalStateException.class, IllegalStateException.class, Parameter.ZERO)
            .addComponent(InstantiationException.class,InstantiationException.class, Parameter.ZERO)
            .addComponent(InterruptedException.class,InterruptedException.class, Parameter.ZERO)
            .addComponent(NegativeArraySizeException.class,NegativeArraySizeException.class, Parameter.ZERO)
            .addComponent(NumberFormatException.class,NumberFormatException.class, Parameter.ZERO)
            .addComponent(SecurityException.class,SecurityException.class, Parameter.ZERO)
            .addComponent(StringIndexOutOfBoundsException.class,StringIndexOutOfBoundsException.class, Parameter.ZERO)
            .addComponent(NoSuchFieldError.class,NoSuchFieldError.class, Parameter.ZERO)
            .addComponent(InternalError.class,InternalError.class, Parameter.ZERO)
            .addComponent(UnsupportedOperationException.class,UnsupportedOperationException.class, Parameter.ZERO)
            .addComponent(AbstractMethodError.class,AbstractMethodError.class, Parameter.ZERO)
            .addComponent(ClassFormatError.class,ClassFormatError.class, Parameter.ZERO)
            .addComponent(ExceptionInInitializerError.class,ExceptionInInitializerError.class, Parameter.ZERO)
            .addComponent(IllegalAccessError.class,IllegalAccessError.class)
        ;
    }

    @Test public void testPerformanceTestDefaultPico() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < ITERATIONS; i++) {
			MutablePicoContainer pico = new PicoBuilder()
			.implementedBy(DefaultPicoContainer.class)
			.withCaching()
			.build();

            addComponents(pico);

            pico.addComponent("TestKey", "This is a test")
                .addComponent("TestAnother", "This is a test");

			pico.start();
			
			assertNotNull(pico.getComponent("TestKey"));
			assertNotNull(pico.getComponent(RuntimeException.class));
			assertNotNull(pico.getComponent(IllegalArgumentException.class));
			assertNotNull(pico.getComponent(IllegalStateException.class));
			assertNotNull(pico.getComponent(NoSuchFieldError.class));
			pico.stop();
			pico.dispose();
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("Completed iterations using DefaultPicoContainer.  Time: " + (endTime - startTime) + "m.s.");
	}

    @Override
	protected void addContainers(final List expectedList) {
        expectedList.add(ReusablePicoContainer.class);
    }

    @Override
	@Test public void testAcceptImplementsBreadthFirstStrategy() {
        super.testAcceptImplementsBreadthFirstStrategy();
    }
}
