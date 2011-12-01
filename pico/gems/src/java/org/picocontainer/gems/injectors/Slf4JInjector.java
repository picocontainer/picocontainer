/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.gems.injectors;

import org.picocontainer.injectors.FactoryInjector;
import org.picocontainer.injectors.InjectInto;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoCompositionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

/**
 * This will Inject a Slf4J Logger for the injectee's class name
 */
public class Slf4JInjector extends FactoryInjector<Logger> {

    @Override
	public Logger getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
        return LoggerFactory.getLogger((((InjectInto) into).getIntoClass()).getName());
    }
}