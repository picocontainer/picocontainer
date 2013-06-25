/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.web;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 
 * @author Gr&eacute;gory Joseph
 */
public final class FooFilter implements Filter {
    private static int initCounter;
    private final Foo foo;

    public FooFilter(Foo foo) {
        this.foo = foo;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        initCounter++;
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
        PrintWriter w = res.getWriter();
        w.write(foo != null ? foo.peek() : "foo was null for peek");
        filterChain.doFilter(req, res);
        w.write(foo != null ? foo.poke() : "foo was null for poke");
    }

    public void destroy() {
    }

    public static void resetInitCounter() {
        initCounter = 0;
    }

    public static int getInitCounter() {
        return initCounter;
    }

}