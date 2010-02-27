/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/

package org.picocontainer.gems.constraints;

import org.picocontainer.ComponentAdapter;

/**
 * Constraint that accepts an adapter of a specific key.
 *
 * @author Nick Sieger
 */
@SuppressWarnings("serial")
public final class IsKey extends AbstractConstraint {


	private final Object key;

    /**
     * Creates a new <code>IsKey</code> instance.
     *
     * @param key the key to match
     */
    public IsKey(final Object key) {
        this.key = key;
    }

    @Override
	public boolean evaluate(final ComponentAdapter adapter) {
        return key.equals(adapter.getComponentKey());
    }

}
