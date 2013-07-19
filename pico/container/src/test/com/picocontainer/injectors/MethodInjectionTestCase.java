/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.injectors;

import static com.picocontainer.Characteristics.USE_NAMES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.Properties;

import org.junit.Test;
import org.mockito.internal.stubbing.answers.CallsRealMethods;
import com.picocontainer.injectors.ConstructorInjectionTestCase.ClassAsConstructor;

import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.PicoContainer;
import com.picocontainer.annotations.Nullable;
import com.picocontainer.containers.EmptyPicoContainer;
import com.picocontainer.injectors.MethodInjection;
import com.picocontainer.injectors.MultiArgMemberInjector;
import com.picocontainer.lifecycle.NullLifecycleStrategy;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.parameters.ConstantParameter;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;

public class MethodInjectionTestCase {

    public static interface IFoo {
        void inject(Bar bar, Integer num);
    }

    public static class Foo implements IFoo {
        private Bar bar;
        private Integer num;

        public void inject(final Bar bar, final Integer num) {
            this.bar = bar;
            this.num = num;
        }
    }

    public static class Bar {
        public Bar() {
        }
    }

    @Test public void testMethodInjection() {
        ComponentMonitor cm  = mock(NullComponentMonitor.class, new CallsRealMethods());
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), cm, new MethodInjection());
        pico.addComponent(123);
        pico.addComponent(Foo.class);
        pico.addComponent(Bar.class);
        Foo foo = pico.getComponent(Foo.class);
        assertNotNull(foo.bar);
        assertNotNull(foo.num);
        assertEquals("MethodInjector[inject]-class com.picocontainer.injectors.MethodInjectionTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
        verify(cm).invoking(any(PicoContainer.class), any(MethodInjection.MethodInjector.class), any(Method.class), any(Foo.class), any(Bar.class), same(123));
        //Can't quite test the arguments passed yet since we're using a variable arg array.
        //Have to have any value because debug container might end up injecting a millisecond into
        //the invocation from time to time and cause
        //spurius test failures.
        verify(cm).invoked(any(PicoContainer.class), any(MethodInjection.MethodInjector.class), any(Method.class), any(Foo.class), any(Long.class), isNull(), anyVararg() );
    }

    @Test public void testMethodInjectionViaMethodDef() {
        Method mthd = Foo.class.getMethods()[0];
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new MethodInjection(mthd));
        pico.addComponent(123);
        pico.addComponent(Foo.class);
        pico.addComponent(new Bar());
        Foo foo = pico.getComponent(Foo.class);
        assertNotNull(foo.bar);
        assertNotNull(foo.num);
        assertEquals("SpecificReflectionMethodInjector[com.picocontainer.injectors.MethodInjectionTestCase$Foo.inject]-class com.picocontainer.injectors.MethodInjectionTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testMethodInjectionViaMethodDefViaInterface() {
        Method mthd = IFoo.class.getMethods()[0];
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new MethodInjection(mthd));
        pico.addComponent(123);
        pico.addComponent(Foo.class);
        pico.addComponent(new Bar());
        Foo foo = pico.getComponent(Foo.class);
        assertNotNull(foo.bar);
        assertNotNull(foo.num);
        ComponentAdapter<?> adapter = pico.getComponentAdapter(Foo.class);
        String foo2 = adapter.toString();
        assertEquals("SpecificReflectionMethodInjector[com.picocontainer.injectors.MethodInjectionTestCase$IFoo.inject]-class com.picocontainer.injectors.MethodInjectionTestCase$Foo", foo2);
    }


    @Test public void testMethodInjectionViaCharacteristics() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy());
        pico.addComponent(123);
        pico.as(Characteristics.METHOD_INJECTION).addComponent(Foo.class);
        pico.addComponent(Bar.class);
        Foo foo = pico.getComponent(Foo.class);
        assertNotNull(foo.bar);
        assertNotNull(foo.num);
        assertEquals("CompositeInjector(ConstructorInjector+MethodInjector[inject])-class com.picocontainer.injectors.MethodInjectionTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testMethodInjectionViaAdapter() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new MethodInjection());
        pico.addComponent(123);
        pico.addAdapter(new MethodInjection.MethodInjector<Foo>(Foo.class, Foo.class, new NullComponentMonitor(), "inject", false, true, null));
        pico.addComponent(Bar.class);
        Foo foo = pico.getComponent(Foo.class);
        assertNotNull(foo.bar);
        assertNotNull(foo.num);
        assertEquals("MethodInjector[inject]-class com.picocontainer.injectors.MethodInjectionTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testMethodInjectionByBuilder() {
        MutablePicoContainer pico = new PicoBuilder().withMethodInjection().build();
        pico.addComponent(123);
        pico.addComponent(Foo.class);
        pico.addComponent(Bar.class);
        Foo foo = pico.getComponent(Foo.class);
        assertNotNull(foo.bar);
        assertNotNull(foo.num);
        assertEquals("MethodInjector[inject]-class com.picocontainer.injectors.MethodInjectionTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

    public static class Foo2 implements IFoo {
        private Bar bar;
        private Integer num;

        public void inject(final Bar bar, @Nullable final Integer num) {
            this.bar = bar;
            this.num = num;
        }
    }

    public static class Foo3 implements IFoo {
        private Bar bar;
        private Integer num;
        private Bar bar2;
        private Bar bar3;

        public void inject(final Bar bar, @Nullable final Integer num) {
            this.bar = bar;
            this.num = num;
        }

        public void injectSomethingElse(final Bar bar2, final Bar bar3) {
            this.bar2 = bar2;
            this.bar3 = bar3;
        }
    }

    @Test public void testMethodInjectionWithAllowedNullableParam() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new MethodInjection()
       );
        pico.addComponent(Foo2.class);
        pico.addComponent(Bar.class);
        Foo2 foo = pico.getComponent(Foo2.class);
        assertNotNull(foo.bar);
        assertTrue(foo.num == null);
        assertEquals("MethodInjector[inject]-class com.picocontainer.injectors.MethodInjectionTestCase$Foo2", pico.getComponentAdapter(Foo2.class).toString());
    }

    @Test public void aComponentWithMoreThanOneInjectMethodAndMoreThanOneParam() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new MethodInjection()
       );
        pico.addComponent(Foo3.class);
        pico.addComponent(Bar.class);
        Foo3 foo3 = pico.getComponent(Foo3.class);
        assertNotNull(foo3.bar);
        assertNotNull(foo3.bar2);
        assertNotNull(foo3.bar3);
        assertTrue(foo3.num == null);
        ComponentAdapter<?> adapter = pico.getComponentAdapter(Foo3.class);
        String result = adapter.toString();

        //Allow for undefined method return order.
        //This seems prevalent in JDK >= 1.7
        result = result.replace("[injectSomethingElse,inject]", "[inject,injectSomethingElse]");
        assertEquals("MethodInjector[inject,injectSomethingElse]-class com.picocontainer.injectors.MethodInjectionTestCase$Foo3",
        		result);
    }

    @Test public void testMethodInjectionWithDisallowedNullableParam() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new MethodInjection());
        pico.addComponent(Foo.class);
        pico.addComponent(Bar.class);
        try {
            Foo foo = pico.getComponent(Foo.class);
            fail("should have barfed");
        } catch (MultiArgMemberInjector.ParameterCannotBeNullException e) {
            assertEquals("num", e.getParameterName());
            assertTrue(e.getMessage().indexOf("Parameter 1") != -1);
            assertTrue(e.getMessage().indexOf(Foo.class.getMethods()[0].toString()) != -1);
        }
    }

    @Test public void testMethodInjectionWithIntegerParamCanBeconvertedFromString() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new MethodInjection());
        pico.as(USE_NAMES).addComponent(Foo.class);
        pico.addComponent(Bar.class);
        pico.addComponent("num", "123");
        Foo foo = pico.getComponent(Foo.class);
        assertNotNull(foo.bar);
        assertNotNull(foo.num);
        assertEquals(123, (int)foo.num);
        assertEquals("MethodInjector[inject]-class com.picocontainer.injectors.MethodInjectionTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

	@Test
	public void testOnlyMethodParametersAreUsed() {
		MethodInjection componentFactory = new MethodInjection();

        MethodInjection.MethodInjector injector =  (MethodInjection.MethodInjector)
        componentFactory.createComponentAdapter(new NullComponentMonitor(), new NullLifecycleStrategy(),
        		new Properties(), ClassAsConstructor.class, ClassAsConstructor.class,
        		new ConstructorParameters(new ConstantParameter("Test")),
        		new FieldParameters[] {new FieldParameters("joe", new ConstantParameter("Test"))},
        		new MethodParameters[]{new MethodParameters("", new ConstantParameter("Value")) } );

        assertTrue(injector.parameters.length == 1);
        assertEquals(1, injector.parameters.length);
        assertEquals(1, injector.parameters[0].getParams().length);
        assertEquals("Value",  ((ConstantParameter)injector.parameters[0].getParams()[0]).getValue());


	}


}