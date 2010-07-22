/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/

package org.picocontainer.gems.constraints;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoVisitor;
import org.picocontainer.TypeOf;
import org.picocontainer.parameters.CollectionComponentParameter;

/**
 * Constraint that collects/aggregates dependencies to as many components
 * that satisfy the given constraint.
 *
 * @author Nick Sieger
 * @author J&ouml;rg Schaible
 */
@SuppressWarnings("serial")
public final class CollectionConstraint extends CollectionComponentParameter implements Constraint {

	protected final Constraint constraint;

    public CollectionConstraint(final Constraint constraint) {
        this(constraint, false);
    }

    public CollectionConstraint(final Constraint constraint, final boolean emptyCollection) {
        super(TypeOf.fromClass(Object.class), emptyCollection);
        this.constraint = constraint;
    }

    @Override
	public boolean evaluate(final ComponentAdapter adapter) {
        return constraint.evaluate(adapter);
    }

    @Override
	public void accept(final PicoVisitor visitor) {
        super.accept(visitor);
        constraint.accept(visitor);
    }
}
