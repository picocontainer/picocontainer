/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/

package org.picocontainer.gems.constraints;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoVisitor;

/**
 * Test the <code>And, Or, Not</code> constraints.
 *
 * @author Nick Sieger
 * @author J&ouml;rg Schaible
 * @author Mauro Talevi
 */
@RunWith(JMock.class)
public final class AndOrNotTestCase  {

	private Mockery mockery = mockeryWithCountingNamingScheme();
	
    final ComponentAdapter adapter = mockery.mock(ComponentAdapter.class);
    final PicoVisitor      visitor =  mockery.mock(PicoVisitor.class);

    final Constraint       c1 = mockery.mock(Constraint.class, "constraint 1");
    final Constraint       c2 = mockery.mock(Constraint.class, "constraint 2");
    final Constraint       c3 = mockery.mock(Constraint.class, "constraint 3");

    @Test public void testAndAllChildrenConstraintsTrueGivesTrue() {
        Constraint c = new And(c1, c2, c3);
        final Sequence sequence = mockery.sequence("contraints");
        mockery.checking(new Expectations(){{
        	one(c1).evaluate(with(same(adapter)));
        	will(returnValue(true)); inSequence(sequence);
        	one(c2).evaluate(with(same(adapter)));
        	will(returnValue(true)); inSequence(sequence);
        	one(c3).evaluate(with(same(adapter)));
        	will(returnValue(true)); inSequence(sequence);
        }});        

        assertTrue(c.evaluate(adapter));
    }
    
    @Test public void testAndAllChildrenAreVisitedBreadthFirst() {
        final Constraint c = new And(c1, c2, c3);
        
        final Sequence sequence = mockery.sequence("contraints");
        mockery.checking(new Expectations(){{
        	one(visitor).visitParameter(with(same(c))); inSequence(sequence);
        	one(c1).accept(with(same(visitor))); inSequence(sequence);
        	one(c2).accept(with(same(visitor))); inSequence(sequence);
        	one(c3).accept(with(same(visitor))); inSequence(sequence);
        }});        
        
        c.accept(visitor);
    }

    @Test public void testAndAllChildrenConstraintsTrueGivesTrueUsingAlternateConstructor() {
        Constraint c = new And(new Constraint[] {c1, c2, c3});

        final Sequence sequence = mockery.sequence("contraints");
        mockery.checking(new Expectations(){{
        	one(c1).evaluate(with(same(adapter)));
        	will(returnValue(true)); inSequence(sequence);
        	one(c2).evaluate(with(same(adapter)));
        	will(returnValue(true)); inSequence(sequence);
        	one(c3).evaluate(with(same(adapter)));
        	will(returnValue(true)); inSequence(sequence);
        }});        

        assertTrue(c.evaluate(adapter));
    }

    @Test public void testAndShortCircuitGivesFalse() {
        Constraint c = new And(c1, c2, c3);

        final Sequence sequence = mockery.sequence("contraints");
        mockery.checking(new Expectations(){{
        	one(c1).evaluate(with(same(adapter)));
        	will(returnValue(true)); inSequence(sequence);
        	one(c2).evaluate(with(same(adapter)));
        	will(returnValue(false)); inSequence(sequence);
        	never(c3).evaluate(with(same(adapter)));
        }});        
        
        assertFalse(c.evaluate(adapter));
    }

    @Test public void testOrAllChildrenConstraintsFalseGivesFalse() {
        Constraint c = new Or(c1, c2, c3);

        final Sequence sequence = mockery.sequence("contraints");
        mockery.checking(new Expectations(){{
        	one(c1).evaluate(with(same(adapter)));
        	will(returnValue(false)); inSequence(sequence);
        	one(c2).evaluate(with(same(adapter)));
        	will(returnValue(false)); inSequence(sequence);
        	one(c3).evaluate(with(same(adapter)));
        	will(returnValue(false)); inSequence(sequence);
        }});        

        assertFalse(c.evaluate(adapter));
    }
    
    @Test public void testOrAllChildrenAreVisitedBreadthFirst() {
        final Constraint c = new Or(c1, c2, c3);
        
        final Sequence sequence = mockery.sequence("contraints");
        mockery.checking(new Expectations(){{
        	one(visitor).visitParameter(with(same(c))); inSequence(sequence);
        	one(c1).accept(with(same(visitor))); inSequence(sequence);
        	one(c2).accept(with(same(visitor))); inSequence(sequence);
        	one(c3).accept(with(same(visitor))); inSequence(sequence);
        }});        
        
        c.accept(visitor);
    }

    @Test public void testMixingOrAndNot() {
        Constraint c = new Or(c1, new Not(c2), c3);

        final Sequence sequence = mockery.sequence("contraints");
        mockery.checking(new Expectations(){{
        	one(c1).evaluate(with(same(adapter)));
        	will(returnValue(false)); inSequence(sequence);
        	one(c2).evaluate(with(same(adapter)));
        	will(returnValue(false)); inSequence(sequence);
        	never(c3).evaluate(with(same(adapter)));
        }});        

        assertTrue(c.evaluate(adapter));
    }
    
    @Test public void testNotChildIdVisitedBreadthFirst() {
        final Constraint c = new Not(c1);
        
        final Sequence sequence = mockery.sequence("contraints");
        mockery.checking(new Expectations(){{
        	one(visitor).visitParameter(with(same(c))); inSequence(sequence);
        	one(c1).accept(with(same(visitor))); inSequence(sequence);
        }});        
        
        c.accept(visitor);
    }
}
