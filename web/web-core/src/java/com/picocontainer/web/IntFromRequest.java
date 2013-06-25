/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.web;

import com.picocontainer.MutablePicoContainer;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * Use this to make a request level component that pulls an integer from a named parameter (GET or POST)
 * of the request.  If a parameter of the supplied name is not available for the current
 * request path, then an exception will be thrown. An exception will also be thrown, if the number format is bad.
 */
@SuppressWarnings("serial")
public class IntFromRequest extends StringFromRequest implements Serializable {

    public IntFromRequest(String paramName) {
        super(paramName);
    }

    @Override
    public Class<Integer> getComponentImplementation() {
        return Integer.class;
    }

    @Override
    public Object provide(HttpServletRequest req) {
        String num = (String) super.provide(req);
        try {
            return Integer.parseInt(num);
        } catch (NumberFormatException e) {
            throw new PicoContainerWebException("'" + num + "' cannot be converted to an integer");
        }
    }

    /**
     * Add a number of IntFromRequest adapters to a container.
     * @param toContainer the container to add to
     * @param names the list of names to make adapters from
     */
    public static void addIntegerRequestParameters(MutablePicoContainer toContainer, String... names) {
        for (String name : names) {
            toContainer.addAdapter(new IntFromRequest(name));
        }
    }


}
