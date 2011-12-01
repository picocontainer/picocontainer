/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.web;

import javax.servlet.http.HttpServletRequest;

import org.picocontainer.injectors.ProviderAdapter;

import java.io.Serializable;

/**
 * Use this to make a request level component that pulls information from HTTP
 * request header.  If a header of the supplied name is not available for the current
 * HttpServletRequest, then a NotFound exception will be thrown.
 * <h4>Headers with dashes:</h4>
 * <p>Many standard request headers have hyphens in them, 
 * (see <a href="http://en.wikipedia.org/wiki/List_of_HTTP_headers">Wikipedia List of Headers</a>
 * </p>
 * <p>To handle that, this class translates all hyphens to underscores ('_').  The
 * end result is that you can construct a class that takes the
 * User-Agent as a constructor argument like so:</p>
 * <pre>
 *	public static class Integration {
 *		public Integration(String User_Agent) {
 *			//Does nothing.
 *		}
 *	}  
 * </pre>
 */
public class StringFromHeader extends ProviderAdapter implements Serializable {

	/**
	 * The component key that we use to integrate with
	 * Pico
	 */
	private final String headerKey;


	/**
	 * The header name we're searching for.
	 */
	private final String headerName;

	/**
	 * Constructs a new String From Header
	 */
	public StringFromHeader(String headername) {
		super();
		this.headerName = headername;
		if (headername == null) {
			throw new NullPointerException("headername");
		}

		headerKey = headername.replaceAll("\\-", "_");
	}

    @Override
    public Class getComponentImplementation() {
        return String.class;
    }

    @Override
    public Object getComponentKey() {
        return headerKey;
    }
    
    /** 
     * {@inheritDoc} 
     * <p>Provides the header as specified by the header name.</p>
     **/
    public String provide(final HttpServletRequest request) {
    	
    	String result =  request.getHeader(headerName);
    	if (result == null) {
    		throw new HeaderNotFound(headerName);
    	}
    	
    	return result;
    }

	
	@Override
    public String toString() {
		return "String from header.  Component Key " + headerKey + " Servlet Request Header Name: " + headerName;
    }    
    
    @SuppressWarnings("serial")
    public static class HeaderNotFound extends PicoContainerWebException {
        private HeaderNotFound(String name) {
            super("'" + name + "' not found in header");
        }
    }    

}
