/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.gems.behaviors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentFactory;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.adapters.InstanceAdapter;
import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.gems.adapters.Elephant;
import org.picocontainer.gems.adapters.ElephantImpl;
import org.picocontainer.gems.adapters.ElephantProxy;
import org.picocontainer.injectors.AdaptingInjection;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.injectors.ConstructorInjector;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.tck.AbstractComponentFactoryTest;


public final class AsmImplementationHidingTestCase extends AbstractComponentFactoryTest {

    private final ComponentFactory implementationHidingComponentFactory = new AsmImplementationHiding().wrap(new AdaptingInjection());

    @Test
    public void testAddComponentUsesImplementationHidingBehavior() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new AsmImplementationHiding().wrap(new ConstructorInjection()));
        pico.addComponent("foo", String.class);
        ComponentAdapter<?> foo = pico.getComponentAdapter("foo");
        assertEquals(AsmHiddenImplementation.class, foo.getClass());
        assertEquals(ConstructorInjector.class, ((AbstractBehavior) foo).getDelegate().getClass());
    }

    @Test
    public void testAddComponentUsesImplementationHidingBehaviorWithRedundantHideImplProperty() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new AsmImplementationHiding().wrap(new ConstructorInjection()));
        pico.change(Characteristics.HIDE_IMPL).addComponent("foo", String.class);
        ComponentAdapter<?> foo = pico.getComponentAdapter("foo");
        assertEquals(AsmHiddenImplementation.class, foo.getClass());
        assertEquals(ConstructorInjector.class, ((AbstractBehavior) foo).getDelegate().getClass());
    }

    @Test
    public void testAddComponentNoesNotUseImplementationHidingBehaviorWhenNoCachePropertyIsSpecified() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new AsmImplementationHiding().wrap(new ConstructorInjection()));
        pico.change(Characteristics.NO_HIDE_IMPL).addComponent("foo", String.class);
        ComponentAdapter<?> foo = pico.getComponentAdapter("foo");
        assertEquals(ConstructorInjector.class, foo.getClass());
    }
    
    @Test
    public void testAddAdapterUsesImplementationHidingBehavior() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new AsmImplementationHiding().wrap(new ConstructorInjection()));
        pico.addAdapter(new InstanceAdapter<String>("foo", "bar", new NullLifecycleStrategy(), new NullComponentMonitor()));
        ComponentAdapter<?> foo = pico.getComponentAdapter("foo");
        assertEquals(AsmHiddenImplementation.class, foo.getClass());
        assertEquals(InstanceAdapter.class, ((AbstractBehavior) foo).getDelegate().getClass());

    }

    @Test
    public void testAddAdapterUsesImplementationHidingBehaviorWithRedundantHideImplProperty() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new AsmImplementationHiding().wrap(new ConstructorInjection()));
        pico.change(Characteristics.HIDE_IMPL).addAdapter(new InstanceAdapter("foo", "bar", new NullLifecycleStrategy(), new NullComponentMonitor()));
        ComponentAdapter<?> foo = pico.getComponentAdapter("foo");
        assertEquals(AsmHiddenImplementation.class, foo.getClass());
        assertEquals(InstanceAdapter.class, ((AbstractBehavior) foo).getDelegate().getClass());
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