/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.testmodel;

/**
 * @author Thomas Heller
 */
public class DecoratedTouchable implements Touchable {
    private final Touchable delegate;

    public DecoratedTouchable(final Touchable delegate) {
        this.delegate = delegate;
    }

    public void touch() {
        delegate.touch();
    }
}
