/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by James Strachan                                           *
 *****************************************************************************/

package com.picocontainer.script;

import com.picocontainer.PicoException;

/**
 * Exception thrown when invalid markup is encountered when assembling {@link com.picocontainer.classname.ClassLoadingPicoContainer ScriptedPicoContainer}s.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Aslak Helles&oslash;y
 */
@SuppressWarnings("serial")
public class ScriptedPicoContainerMarkupException extends PicoException {

    public ScriptedPicoContainerMarkupException(final String message) {
        super(message);
    }

    public ScriptedPicoContainerMarkupException(final String message, final Throwable e) {
        super(message, e);
    }

    public ScriptedPicoContainerMarkupException(final Throwable e) {
        super(e);
    }
}
