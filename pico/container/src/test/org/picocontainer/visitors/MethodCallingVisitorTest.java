/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.visitors;

import static org.junit.Assert.assertEquals;
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoVisitor;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.testmodel.Touchable;


/**
 * @author J&ouml;rg Schaible
 * @author Mauro Talevi
 */
@RunWith(JMock.class)
public class MethodCallingVisitorTest {

	private Mockery mockery = mockeryWithCountingNamingScheme();
	
    private Method add;
    private Method touch;

    @Before
    public void setUp() throws Exception {
        add = List.class.getMethod("add", Object.class);
        touch = Touchable.class.getMethod("touch", (Class[])null);
    }

    @Test public void testVisitorWillTraverseAndCall() throws Exception {
        MutablePicoContainer parent = new DefaultPicoContainer(new Caching());
        MutablePicoContainer child = new DefaultPicoContainer(new Caching());
        parent.addChildContainer(child);
        parent.addComponent(List.class, LinkedList.class);
        child.addComponent(List.class, LinkedList.class);
        List parentList = parent.getComponent(List.class);
        List childList = child.getComponent(List.class);

        assertEquals(0, parentList.size());
        assertEquals(0, childList.size());

        PicoVisitor visitor = new MethodCallingVisitor(add, List.class, new Object[]{Boolean.TRUE});
        visitor.traverse(parent);

        assertEquals(1, parentList.size());
        assertEquals(1, childList.size());
    }

    @Test public void testVisitsInInstantiationOrder() throws Exception {
    	final Touchable touchable1 = mockery.mock(Touchable.class);
    	final Touchable touchable2 = mockery.mock(Touchable.class);
    	
    	final Sequence sequence = mockery.sequence("touching");
        mockery.checking(new Expectations() {{
            one(touchable1).touch(); inSequence(sequence);
            one(touchable2).touch(); inSequence(sequence);
        }});
    	
        MutablePicoContainer parent = new DefaultPicoContainer();
        MutablePicoContainer child = new DefaultPicoContainer();
        parent.addChildContainer(child);
        parent.addComponent(touchable1);
        child.addComponent(touchable2);

        PicoVisitor visitor = new MethodCallingVisitor(touch, Touchable.class, null);
        visitor.traverse(parent);
    }

    @Test public void testVisitsInReverseInstantiationOrder() throws Exception {
    	final Touchable touchable1 = mockery.mock(Touchable.class);
    	final Touchable touchable2 = mockery.mock(Touchable.class);
    	
    	final Sequence sequence = mockery.sequence("touching");
        mockery.checking(new Expectations() {{
            one(touchable2).touch(); inSequence(sequence);
            one(touchable1).touch(); inSequence(sequence);
        }});

        MutablePicoContainer parent = new DefaultPicoContainer();
        MutablePicoContainer child = new DefaultPicoContainer();
        parent.addChildContainer(child);
        parent.addComponent(touchable1);
        child.addComponent(touchable2);

        PicoVisitor visitor = new MethodCallingVisitor(touch, Touchable.class, null, false);
        visitor.traverse(parent);
    }
}
