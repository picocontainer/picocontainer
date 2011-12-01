/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/

package org.picocontainer.web;

import org.picocontainer.injectors.ProviderAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import java.io.Serializable;

/**
 * Use this to make a request level component that pulls information from cookie held on
 * the browser.  If a cookie of the supplied name is not available for the current
 * request path, then a NotFound exception will be thrown.
 */
public class StringFromCookie extends ProviderAdapter implements Serializable {

    private final String name;

    public StringFromCookie(String name) {
        this.name = name;
    }

    @Override
    public Class getComponentImplementation() {
        return String.class;
    }

    @Override
    public Object getComponentKey() {
        return name;
    }

    public String provide(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        throw new CookieNotFound(name);
    }

    public static class CookieNotFound extends PicoContainerWebException {
        private CookieNotFound(String name) {
            super("'" + name + "' not found in cookies");
        }
    }

}