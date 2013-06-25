/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.injectors;

/**
 * Constructor Injection where 'which constructor to use?' is re-calculated each time an
 * instance is asked to construct a component.
 */
@SuppressWarnings("serial")
public class ForgetfulConstructorInjection extends ConstructorInjection {

    public ForgetfulConstructorInjection() {
        super(false);
    }
}
