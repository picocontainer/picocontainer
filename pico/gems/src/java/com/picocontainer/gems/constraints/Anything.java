/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/

package com.picocontainer.gems.constraints;

import com.picocontainer.ComponentAdapter;

/**
 * A constraint that matches any component adapter.
 *
 * @author Nick Sieger
 */
@SuppressWarnings("serial")
public class Anything extends AbstractConstraint {

	public static final Anything ANYTHING = new Anything();

    public Anything() {
    }

    @Override
	public boolean evaluate(final ComponentAdapter adapter) {
        return true;
    }
}
