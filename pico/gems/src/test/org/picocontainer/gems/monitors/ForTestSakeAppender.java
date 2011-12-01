/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.gems.monitors;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author Juze Peleteiro
 */
public class ForTestSakeAppender implements Appender {

	public static String CONTENT = "";

	private String name;

	private Layout layout;

	private ErrorHandler errorHandler;

	public void addFilter(final Filter filter) {
	}
	
	public ForTestSakeAppender() {
		CONTENT = "";
	}
	
	/**
	 * @see org.apache.log4j.Appender#getFilter()
	 */
	public Filter getFilter() {
		return null;
	}

	/**
	 * @see org.apache.log4j.Appender#clearFilters()
	 */
	public void clearFilters() {
	}

	/**
	 * @see org.apache.log4j.Appender#close()
	 */
	public void close() {
		CONTENT = "";
	}

	/**
	 * @see org.apache.log4j.Appender#doAppend(org.apache.log4j.spi.LoggingEvent)
	 */
	public void doAppend(final LoggingEvent e) {
		CONTENT += "\n" + layout.format(e);
	}

	/**
	 * @see org.apache.log4j.Appender#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see org.apache.log4j.Appender#setErrorHandler(org.apache.log4j.spi.ErrorHandler)
	 */
	public void setErrorHandler(final ErrorHandler value) {
		errorHandler = value;
	}

	/**
	 * @see org.apache.log4j.Appender#getErrorHandler()
	 */
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	/**
	 * @see org.apache.log4j.Appender#setLayout(org.apache.log4j.Layout)
	 */
	public void setLayout(final Layout value) {
		layout = value;
	}

	/**
	 * @see org.apache.log4j.Appender#getLayout()
	 */
	public Layout getLayout() {
		return layout;
	}

	/**
	 * @see org.apache.log4j.Appender#setName(java.lang.String)
	 */
	public void setName(final String value) {
		name = value;
	}

	/**
	 * @see org.apache.log4j.Appender#requiresLayout()
	 */
	public boolean requiresLayout() {
		return true;
	}

}
