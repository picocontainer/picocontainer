/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package com.picocontainer.gems.behaviors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.Test;
import com.picocontainer.gems.adapters.Elephant;
import com.picocontainer.gems.adapters.ElephantImpl;
import com.picocontainer.gems.adapters.ElephantProxy;
import com.picocontainer.tck.AbstractComponentFactoryTest;

import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentFactory;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.adapters.InstanceAdapter;
import com.picocontainer.behaviors.AbstractBehavior;
import com.picocontainer.containers.EmptyPicoContainer;
import com.picocontainer.injectors.AdaptingInjection;
import com.picocontainer.injectors.ConstructorInjection;
import com.picocontainer.lifecycle.NullLifecycleStrategy;
import com.picocontainer.monitors.NullComponentMonitor;


public final class AsmImplementationHidingTestCase extends AbstractComponentFactoryTest {

    private final ComponentFactory implementationHidingComponentFactory = new AsmImplementationHiding().wrap(new AdaptingInjection());

    @Test
    public void testAddComponentUsesImplementationHidingBehavior() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new AsmImplementationHiding().wrap(new ConstructorInjection()));
        pico.addComponent("foo", String.class);
        ComponentAdapter<?> foo = pico.getComponentAdapter("foo");
        assertEquals(AsmImplementationHiding.AsmHiddenImplementation.class, foo.getClass());
        assertEquals(ConstructorInjection.ConstructorInjector.class, ((AbstractBehavior.AbstractChangedBehavior) foo).getDelegate().getClass());
    }

    @Test
    public void testAddComponentUsesImplementationHidingBehaviorWithRedundantHideImplProperty() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new AsmImplementationHiding().wrap(new ConstructorInjection()));
        pico.change(Characteristics.HIDE_IMPL).addComponent("foo", String.class);
        ComponentAdapter<?> foo = pico.getComponentAdapter("foo");
        assertEquals(AsmImplementationHiding.AsmHiddenImplementation.class, foo.getClass());
        assertEquals(ConstructorInjection.ConstructorInjector.class, ((AbstractBehavior.AbstractChangedBehavior) foo).getDelegate().getClass());
    }

    @Test
    public void testAddComponentNoesNotUseImplementationHidingBehaviorWhenNoCachePropertyIsSpecified() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new AsmImplementationHiding().wrap(new ConstructorInjection()));
        pico.change(Characteristics.NO_HIDE_IMPL).addComponent("foo", String.class);
        ComponentAdapter<?> foo = pico.getComponentAdapter("foo");
        assertEquals(ConstructorInjection.ConstructorInjector.class, foo.getClass());
    }

    @Test
    public void testAddAdapterUsesImplementationHidingBehavior() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new AsmImplementationHiding().wrap(new ConstructorInjection()));
        pico.addAdapter(new InstanceAdapter<String>("foo", "bar", new NullLifecycleStrategy(), new NullComponentMonitor()));
        ComponentAdapter<?> foo = pico.getComponentAdapter("foo");
        assertEquals(AsmImplementationHiding.AsmHiddenImplementation.class, foo.getClass());
        assertEquals(InstanceAdapter.class, ((AbstractBehavior.AbstractChangedBehavior) foo).getDelegate().getClass());

    }

    @Test
    public void testAddAdapterUsesImplementationHidingBehaviorWithRedundantHideImplProperty() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new AsmImplementationHiding().wrap(new ConstructorInjection()));
        pico.change(Characteristics.HIDE_IMPL).addAdapter(new InstanceAdapter("foo", "bar", new NullLifecycleStrategy(), new NullComponentMonitor()));
        ComponentAdapter<?> foo = pico.getComponentAdapter("foo");
        assertEquals(AsmImplementationHiding.AsmHiddenImplementation.class, foo.getClass());
        assertEquals(InstanceAdapter.class, ((AbstractBehavior.AbstractChangedBehavior) foo).getDelegate().getClass());
    }

    @Test
    public void testAddAdapterNoesNotUseImplementationHidingBehaviorWhenNoCachePropertyIsSpecified() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new AsmImplementationHiding().wrap(new ConstructorInjection()));
        pico.change(Characteristics.NO_HIDE_IMPL).addAdapter(new InstanceAdapter("foo", "bar", new NullLifecycleStrategy(), new NullComponentMonitor()));
        ComponentAdapter<?> foo = pico.getComponentAdapter("foo");
        assertEquals(InstanceAdapter.class, foo.getClass());
    }

    @Override
	protected ComponentFactory createComponentFactory() {
        return implementationHidingComponentFactory;
    }

    @Test
    public void testElephantWithoutAsmProxy() throws IOException {
        elephantAssertions(new ElephantProxy(new ElephantImpl()));
    }

    @Test
    public void testElephantWithAsmProxy() throws IOException {
        MutablePicoContainer pico = new DefaultPicoContainer(new AsmImplementationHiding());
        Elephant elephant = pico.addComponent(Elephant.class, ElephantImpl.class).getComponent(Elephant.class);
        assertFalse(elephant instanceof ElephantImpl);
        elephantAssertions(elephant);

    }

    private void elephantAssertions(final Elephant elephant) throws IOException {
        assertEquals("onetwo", elephant.objects("one", "two"));
        assertEquals("onetwo", elephant.objectsArray(new String[]{"one"}, new String[]{"two"})[0]);
        assertEquals(3, elephant.iint(1, 2));
        assertEquals(3, elephant.llong(1, 2));
        assertEquals(6, elephant.bbyte((byte) 1, (byte) 2, (byte) 3));
        assertEquals(10, elephant.ffloat(1, 2, 3, 4), .1);
        assertEquals(3, elephant.ddouble(1, 2), .1);
        assertEquals('c', elephant.cchar('a', 'b'));
        assertEquals(3, elephant.sshort((short) 1, (short) 2));
        assertEquals(true, elephant.bboolean(true, true));
        assertEquals(true, elephant.bbooleanArray(new boolean[]{true}, new boolean[]{true})[0]);
    }


}