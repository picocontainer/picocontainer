/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Leo Simons                                               *
 *****************************************************************************/
package org.picocontainer.script.bsh;

import org.picocontainer.PicoCompositionException;

/**
 * @author <a href="mail at leosimons dot com">Leo Simons</a>
 */
@SuppressWarnings("serial")
public class BeanShellScriptCompositionException
    extends PicoCompositionException
{

    BeanShellScriptCompositionException(final String message) {
        super(message);
    }

    BeanShellScriptCompositionException(final Throwable cause) {
        this("BeanShellScriptCompositionException: " + cause.getClass().getName() + " "
                + cause.getMessage());
    }
}
