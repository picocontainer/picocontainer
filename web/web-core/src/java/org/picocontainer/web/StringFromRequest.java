/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/

package org.picocontainer.web;

import org.picocontainer.injectors.ProviderAdapter;
import org.picocontainer.MutablePicoContainer;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * Use this to make a request level component that pulls an string from a named parameter (GET or POST)
 * of the request.  If a parameter of the supplied name is not available for the current
 * request path, then an exception will be thrown.
 */
public class StringFromRequest extends ProviderAdapter implements Serializable {
    private final String paramName;

    public StringFromRequest(String paramName) {
        this.paramName = paramName;
    }

    @Override
    public Class getComponentImplementation() {
        return String.class;
    }

    @Override
    public Object getComponentKey() {
        return paramName;
    }

    public Object provide(HttpServletRequest req) {
        String parameter = req.getParameter(paramName);
        if (parameter == null) {
            throw new ParameterNotFound(paramName);
        }
        return parameter;
    }

    /**
     * Add a number of StringFromRequest adapters to a container.
     * @param toContainer the container to add to
     * @param names the list of names to make adapters from
     */
    public static void addStringRequestParameters(MutablePicoContainer toContainer, String... names) {
        for (String name : names) {
            toContainer.addAdapter(new StringFromRequest(name));
        }
    }

    @SuppressWarnings("serial")
    public static class ParameterNotFound extends PicoContainerWebException {
        private ParameterNotFound(String name) {
            super(name + " not found in request parameters");
        }
    }


}
