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

/**
 * Aggregates multiple constraints together using boolean AND logic.
 * Constraints are short-circuited as in java.
 *
 * @author Nick Sieger
 */
@SuppressWarnings("serial")
public final class And extends AbstractConstraint {
	private final Constraint[] children;

    public And(final Constraint c1, final Constraint c2) {
        children = new Constraint[2];
        children[0] = c1;
        children[1] = c2;
    }

    public And(final Constraint c1, final Constraint c2, final Constraint c3) {
        children = new Constraint[3];
        children[0] = c1;
        children[1] = c2;
        children[2] = c3;
    }

    public And(final Constraint[] cc) {
        children = new Constraint[cc.length];
        System.arraycopy(cc, 0, children, 0, cc.length);
    }

    @Override
	public boolean evaluate(final ComponentAdapter adapter) {
        for (Constraint aChildren : children) {
            if (!aChildren.evaluate(adapter)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
	public void accept(final PicoVisitor visitor) {
        super.accept(visitor);
        for (Constraint aChildren : children) {
            aChildren.accept(visitor);
        }
    }
}
