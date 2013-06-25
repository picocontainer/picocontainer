/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package com.picocontainer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static com.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.PicoVisitor;
import com.picocontainer.visitors.AbstractPicoVisitor;
import com.picocontainer.visitors.VerifyingVisitor;


/**
 * Test general PicoVisitor behaviour.
 * @author J&ouml;rg Schaible
 * @author Mauro Talevi
 */
@RunWith(JMock.class)
public class PicoVisitorTestCase {

	private final Mockery mockery = mockeryWithCountingNamingScheme();

    @Test public void testVisitorThatMustBeInvokedUsingTraverse() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        try {
            pico.accept(new VerifyingVisitor());
            fail("PicoVisitorTraversalException expected");
        } catch (AbstractPicoVisitor.PicoVisitorTraversalException e) {
            assertTrue(e.getMessage().indexOf(VerifyingVisitor.class.getName()) >= 0);
        }
    }

    public static class UnusualNode {
        boolean visited;

        public void accept(final PicoVisitor visit) {
            visited = true;
        }
    }

    @Test public void testUnusualTraverseNode() {
        UnusualNode node = new UnusualNode();
        new VerifyingVisitor().traverse(node);
        assertTrue(node.visited);
    }

    @Test public void testIllegalTraverseNode() {
        try {
            new VerifyingVisitor().traverse("Gosh!");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().indexOf(String.class.getName()) >= 0);
        }
    }

    @Test public void testThrownRuntimeExceptionIsUnwrapped() {
    	final PicoContainer pico = mockery.mock(PicoContainer.class);
        final PicoVisitor visitor = new VerifyingVisitor();
        final Error exception = new Error("junit");
        mockery.checking(new Expectations() {{
            one(pico).accept(with(same(visitor)));
            will(throwException(new PicoCompositionException("message", exception)));
        }});
        try {
            visitor.traverse(pico);
            fail("PicoCompositionException expected");
        } catch (RuntimeException e) {
            assertEquals("message", e.getMessage());
            assertSame(exception, e.getCause());
        }
    }

    @Test public void testThrownErrorIsUnwrapped() {
    	final PicoContainer pico = mockery.mock(PicoContainer.class);
        final PicoVisitor visitor = new VerifyingVisitor();
        final Error error = new InternalError("junit");
        final Sequence sequence = mockery.sequence("accepting");
        mockery.checking(new Expectations() {{
            one(pico).accept(with(same(visitor))); inSequence(sequence);
            one(pico).accept(with(same(visitor))); inSequence(sequence);
            will(throwException(error));
        }});
        visitor.traverse(pico);
        try {
            visitor.traverse(pico);
            fail("UndeclaredThrowableException expected");
        } catch(InternalError e) {
            assertEquals("junit", e.getMessage());
        }
    }
}
