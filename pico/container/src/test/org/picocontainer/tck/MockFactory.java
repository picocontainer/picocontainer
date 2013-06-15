/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Mauro Talevi                                             *
 *****************************************************************************/
package org.picocontainer.tck;

import org.jmock.Mockery;
import org.jmock.lib.CamelCaseNamingScheme;

public class MockFactory {

	/**
	 * Returns a Mockery instance with a counting naming scheme.
	 * From jMock 2.4, default behaviour does not allow more than one mock with same name.
	 * This can be over-restrictive. A workaround is to append a counting integer.
	 *
	 * @return A Mockery instance
	 */
	public static Mockery mockeryWithCountingNamingScheme() {
		return new Mockery() {
			{
				setNamingScheme(new CamelCaseNamingScheme() {
					private int count;

					@Override
					public String defaultNameFor(final Class<?> typeToMock) {
						count++;
						return super.defaultNameFor(typeToMock) + count;
					}
				});
			}
		};
	}
}
