/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original Code By Centerline Computers, Inc.                               *
 *****************************************************************************/

package org.picocontainer.gems.containers;

import org.jmock.integration.junit4.JMock;
import org.junit.runner.RunWith;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Michael Rimov 
 * @author Mauro Talevi
 */
@RunWith(JMock.class)
public class CommonsLoggingTracingContainerDecoratorTestCase extends AbstractTracingContainerDecoratorTest {
	
	@Override
	protected MutablePicoContainer createTracingContainerDecorator(
			final MutablePicoContainer picoContainer, final String name) {
		return new CommonsLoggingTracingContainerDecorator(picoContainer, name);
	}
	

}
