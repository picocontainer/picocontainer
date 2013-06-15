package org.picocontainer.jetty;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class DependencyInjectionTestFilter2 implements Filter {

    public DependencyInjectionTestFilter2(final StringBuffer sb) {
        sb.append("-Filter");
    }

    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request, response);
    }

    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }
}