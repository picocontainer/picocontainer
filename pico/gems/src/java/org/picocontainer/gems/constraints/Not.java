/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/

package org.picocontainer.gems.constraints;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoVisitor;

/**
 * Inverts the logical sense of the given constraint.
 *
 * @author Nick Sieger
 */
@SuppressWarnings("serial")
public final class Not extends AbstractConstraint {

	private final Constraint constraint;

    /**
     * Creates a new <code>Not</code> instance.
     * @param con a <code>Constraint</code> value
     */
    public Not(final Constraint con) {
        this.constraint = con;
    }

    @Override
	public boolean evaluate(final ComponentAdapter comp) {
        return ! constraint.evaluate(comp);
    }

    @Override
	public void accept(final PicoVisitor visitor) {
        super.accept(visitor);
        constraint.accept(visitor);
    }
}
