/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Aslak Hellesoy and Paul Hammant                          *
 *****************************************************************************/
package com.picocontainer.script.xml;


import java.lang.reflect.Type;

import com.picocontainer.PicoContainer;
import com.picocontainer.adapters.AbstractAdapter;

/**
 * component adapter to test script instantiation.
 */
@SuppressWarnings({ "serial", "unchecked" })
public final class TestAdapter extends AbstractAdapter {

    final String foo;
    final String blurge;
    final int bar;

    public TestAdapter(final String foo, final int bar, final String blurge) {
        super(TestAdapter.class, TestAdapter.class);
        this.foo = foo;
        this.bar = bar;
        this.blurge = blurge;
    }

    public void verify(final PicoContainer pico) {
    }

    public Object getComponentInstance(final PicoContainer pico, final Type into) {
        return null;
    }

    public String getDescriptor() {
        return null;
    }
}




