/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.injectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Test;
import org.picocontainer.ComponentAdapter.NOTHING;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.annotations.Inject;
import org.picocontainer.injectors.AnnotatedMethodInjection.AnnotatedMethodInjector;
import org.picocontainer.monitors.NullComponentMonitor;

public class AnnotatedMethodInjectorTestCase  {

    public static class AnnotatedBurp {

        private Wind wind;

        @Inject
        public void windyWind(final Wind wind) {
            this.wind = wind;
        }
    }

    public static class AnnotatedBurp2 {

        protected Wind wind;
        protected Wind wind2;
        protected Wind wind3;

        @Inject
        public void windyWind(final Wind wind) {
            this.wind = wind;
        }

        @Inject
        public void windyWindToTheMax(final Wind wind2, final Wind wind3) {
            this.wind2 = wind2;
            this.wind3 = wind3;
        }
    }

    public static class DepplyAnnotatedThing extends AnnotatedBurp2 {

        private Wind wind4;

        @Inject
        public void inCaseNotEnoughWind(final Wind wind4) {
            this.wind4 = wind4;
        }

    }

    public static class SetterBurp {

        private Wind wind;

        public void setWind(final Wind wind) {
            this.wind = wind;
        }
    }

    public static class Wind {
    }

    @Test public void testSetterMethodInjectionToContrastWithThatBelow() {

        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(new SetterInjection.SetterInjector(SetterBurp.class, SetterBurp.class, new NullComponentMonitor(), "set", false, "", false, null
        ));
        pico.addComponent(Wind.class, new Wind());
        SetterBurp burp = pico.getComponent(SetterBurp.class);
        assertNotNull(burp);
        assertNotNull(burp.wind);
    }

    @Test public void tesMethodInjectionWithInjectionAnnontation() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(new AnnotatedMethodInjection.AnnotatedMethodInjector(AnnotatedBurp.class, AnnotatedBurp.class, null,
                                               new NullComponentMonitor(), false, true, Inject.class));
        pico.addComponent(Wind.class, new Wind());
        AnnotatedBurp burp = pico.getComponent(AnnotatedBurp.class);
        assertNotNull(burp);
        assertNotNull(burp.wind);
    }

    @Test public void tesMethodInjectionWithInjectionAnnontationWhereThereIsMoreThanOneInjectMethod() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(new AnnotatedMethodInjection.AnnotatedMethodInjector(DepplyAnnotatedThing.class, DepplyAnnotatedThing.class, null,
                                               new NullComponentMonitor(), false, true, Inject.class));
        pico.addComponent(Wind.class, new Wind());
        DepplyAnnotatedThing burp = pico.getComponent(DepplyAnnotatedThing.class);
        assertNotNull(burp);
        assertNotNull(burp.wind);
        assertNotNull(burp.wind2);
        assertNotNull(burp.wind3);
        assertNotNull(burp.wind4);
    }

    @Test public void tesMethodInjectionWithInjectionAnnontationWhereThereIsMoreThanOneInjectMethodAndSubClassesNeedingInjectionToo() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(new AnnotatedMethodInjection.AnnotatedMethodInjector(AnnotatedBurp2.class, AnnotatedBurp2.class, null,
                                               new NullComponentMonitor(), false, true, Inject.class));
        pico.addComponent(Wind.class, new Wind());
        AnnotatedBurp2 burp = pico.getComponent(AnnotatedBurp2.class);
        assertNotNull(burp);
        assertNotNull(burp.wind);
        assertNotNull(burp.wind2);
        assertNotNull(burp.wind3);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value={ ElementType.METHOD, ElementType.FIELD})
    public @interface AlternativeInject {
    }

    public static class AnotherAnnotatedBurp {
        private Wind wind;
        @AlternativeInject
        public void windyWind(final Wind wind) {
            this.wind = wind;
        }
    }


    @Test public void testNonSetterMethodInjectionWithAlternativeAnnotation() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(new AnnotatedMethodInjection.AnnotatedMethodInjector(AnotherAnnotatedBurp.class, AnotherAnnotatedBurp.class, null,
                new NullComponentMonitor(),
                false, true, AlternativeInject.class));
        pico.addComponent(Wind.class, new Wind());
        AnotherAnnotatedBurp burp = pico.getComponent(AnotherAnnotatedBurp.class);
        assertNotNull(burp);
        assertNotNull(burp.wind);
    }


    public static class PackageTestParent {
    	boolean injected = false;
    	boolean otherInjected = false;

    	@Inject
    	void doSomething(final String something) {
    		injected = true;
    	}

    	@Inject
    	void doSomethingElse(final String somethingElse) {
    		otherInjected = true;
    	}
    }

    public static class PackageTestChild extends PackageTestParent {
    	@Override
    	void doSomething(final String something) {
    		injected = true;
    	}
    }

    @Test
    public void testPackagePrivateChildCanIgnoreInjectionIfOverridingAnnotationOmitted() {
        MutablePicoContainer picoContainer = new DefaultPicoContainer(new AnnotatedMethodInjection());
    	picoContainer.addComponent(String.class, "Test")
    				.addComponent(PackageTestChild.class);

    	System.out.println(picoContainer.getComponentAdapter(PackageTestChild.class).toString());

    	PackageTestChild testChild = picoContainer.getComponent(PackageTestChild.class);
    	assertNotNull(testChild);
    	assertTrue(testChild.otherInjected);
    	assertFalse(testChild.injected);

    }

    public static class PublicTestParent {
    	boolean injected = false;
    	boolean otherInjected = false;

    	@Inject
    	public void doSomething(final String something) {
    		injected = true;
    	}

    	@Inject
    	public void doSomethingElse(final String somethingElse) {
    		otherInjected = true;
    	}
    }

    public static class PublicTestChild extends PublicTestParent {
    	@Override
    	public void doSomething(final String something) {
    		injected = true;
    	}
    }


    @Test
    public void testPublicInheritedChildCanIgnoreInjectionIfOvrridingAnnoationOmitted() {
        MutablePicoContainer picoContainer = new DefaultPicoContainer(new AnnotatedMethodInjection());
    	picoContainer.addComponent(String.class, "Test")
				.addComponent(PublicTestChild.class);

		System.out.println(picoContainer.getComponentAdapter(PublicTestChild.class).toString());

		PublicTestChild testChild = picoContainer.getComponent(PublicTestChild.class);
		assertNotNull(testChild);
		assertTrue(testChild.otherInjected);
		assertFalse(testChild.injected);
    }


    public static class PrivateMethodInjectionTest {
    	public boolean injected;

    	@Inject
    	private void doSomething() {
    		injected = true;
    	}
    }

    @Test
    public void testInjectionWorksOnPrivateMethodsToo() {
        MutablePicoContainer picoContainer = new DefaultPicoContainer(new AnnotatedMethodInjection())
        		.addComponent(PrivateMethodInjectionTest.class);

        PrivateMethodInjectionTest result = picoContainer.getComponent(PrivateMethodInjectionTest.class);
        assertNotNull(result);
        assertTrue(result.injected);
    }


    public static class DecorationTestBase {

    	public boolean baseInjected = false;

		@javax.inject.Inject
    	public void injectBase() {
    		baseInjected = true;
    	}

    }

    public static class DecorationTestDerived extends DecorationTestBase {

    	public boolean childInjected = false;

		@javax.inject.Inject
    	public void injectChild() {
    		childInjected  = true;
    	}
    }

	@Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testPartialDecorationOnBaseClassDoesntPropagateToChildren() {
    	DefaultPicoContainer pico = new DefaultPicoContainer();
    	AnnotatedMethodInjector injector = new AnnotatedMethodInjector(DecorationTestDerived.class,
    				DecorationTestDerived.class, null, new NullComponentMonitor(),
    				false, false, javax.inject.Inject.class);

    	DecorationTestDerived derived = new DecorationTestDerived();

    	injector.partiallyDecorateComponentInstance(pico, NOTHING.class, derived, DecorationTestBase.class);

    	assertTrue(derived.baseInjected);
    	assertFalse(derived.childInjected);
    }



    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testPartialDecorationOnChildClassDoesntPropagateToParent() {
    	DefaultPicoContainer pico = new DefaultPicoContainer();
    	AnnotatedMethodInjector injector = new AnnotatedMethodInjector(DecorationTestDerived.class,
    				DecorationTestDerived.class, null, new NullComponentMonitor(),
    				false, false, javax.inject.Inject.class);

    	DecorationTestDerived derived = new DecorationTestDerived();

    	injector.partiallyDecorateComponentInstance(pico, NOTHING.class, derived, DecorationTestDerived.class);

    	assertFalse(derived.baseInjected);
    	assertTrue(derived.childInjected);

    }

}
