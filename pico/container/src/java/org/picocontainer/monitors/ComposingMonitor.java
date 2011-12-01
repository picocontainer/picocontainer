/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.monitors;

import org.picocontainer.ComponentMonitor;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

/**
 * The first of a list of composers passed in that responds with an instance for a missing component will
 * be used.
 */
public class ComposingMonitor extends AbstractComponentMonitor {
    private Composer[] composers;

    public ComposingMonitor(ComponentMonitor delegate, Composer... composers) {
        super(delegate);
        this.composers = composers;
    }

    public ComposingMonitor(Composer... composers) {
        this.composers = composers;
    }

    @Override
    public Object noComponentFound(MutablePicoContainer container, Object key) {
        for (Composer composer : composers) {
            Object retVal = composer.compose(container, key);
            if (retVal != null) {
                return retVal;
            }
        }
        return super.noComponentFound(container, key);
    }

    /**
     * A Composer can be used to make components that are otherwise missing.
     */
    public static interface Composer {
        public Object compose(PicoContainer container, Object key);
    }


}
