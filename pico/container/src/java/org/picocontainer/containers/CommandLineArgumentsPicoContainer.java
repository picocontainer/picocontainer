/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.containers;

import java.io.IOException;
import java.io.StringReader;

import org.picocontainer.PicoContainer;

/**
 * Compatibility stub. This class was renamed to CommandLinePicoContainer for
 * PicoContainer version 2.4.
 * 
 * @author Michael Rimov
 * @deprecated Use {@link org.picocontainer.containers.CommandLinePicoContainer}
 *             instead.
 */
@Deprecated
@SuppressWarnings("serial")
public final class CommandLineArgumentsPicoContainer extends
		CommandLinePicoContainer {

	/**
	 * @param separator
	 * @param arguments
	 */
	public CommandLineArgumentsPicoContainer(final String separator,
			final String[] arguments) {
		super(separator, arguments);
	}

	/**
	 * @param separator
	 * @param arguments
	 * @param parent
	 */
	public CommandLineArgumentsPicoContainer(final String separator,
			final String[] arguments, final PicoContainer parent) {
		super(separator, arguments, parent);
	}

	/**
	 * @param separator
	 * @param argumentsProps
	 * @throws IOException
	 */
	public CommandLineArgumentsPicoContainer(final String separator,
			final StringReader argumentsProps) throws IOException {
		super(separator, argumentsProps);
	}

	/**
	 * @param separator
	 * @param argumentProperties
	 * @param arguments
	 * @throws IOException
	 */
	public CommandLineArgumentsPicoContainer(final String separator,
			final StringReader argumentProperties, final String[] arguments)
			throws IOException {
		super(separator, argumentProperties, arguments);
	}

	/**
	 * @param separator
	 * @param argumentProperties
	 * @param arguments
	 * @param parent
	 * @throws IOException
	 */
	public CommandLineArgumentsPicoContainer(final String separator,
			final StringReader argumentProperties, final String[] arguments,
			final PicoContainer parent) throws IOException {
		super(separator, argumentProperties, arguments, parent);
	}

	/**
	 * @param arguments
	 */
	public CommandLineArgumentsPicoContainer(final String[] arguments) {
		super(arguments);
	}

	/**
	 * @param arguments
	 * @param parent
	 */
	public CommandLineArgumentsPicoContainer(final String[] arguments,
			final PicoContainer parent) {
		super(arguments, parent);
	}
}
