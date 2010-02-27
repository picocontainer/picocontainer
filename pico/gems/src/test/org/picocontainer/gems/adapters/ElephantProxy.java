/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.gems.adapters;

import java.io.IOException;

public final class ElephantProxy implements Elephant {
    private final transient Elephant delegate;

    public ElephantProxy(final Elephant delegate) {
        this.delegate = delegate;
    }

    public String objects(final String one, final String two) throws IOException {
        return delegate.objects(one, two);
    }

    public String[] objectsArray(final String[] one, final String[] two) throws IOException {
        return delegate.objectsArray(one, two);
    }

    public int iint(final int a, final int b) {
        return delegate.iint(a, b);
    }

    public long llong(final long a, final long b) {
        return delegate.llong(a, b);
    }

    public byte bbyte(final byte a, final byte b, final byte c) {
        return delegate.bbyte(a, b, c);
    }

    public float ffloat(final float a, final float b, final float c, final float d) {
        return delegate.ffloat(a, b, c, d);
    }

    public double ddouble(final double a, final double b) {
        return delegate.ddouble(a, b);
    }

    public char cchar(final char a, final char b) {
        return delegate.cchar(a, b);
    }

    public short sshort(final short a, final short b) {
        return delegate.sshort(a, b);
    }

    public boolean bboolean(final boolean a, final boolean b) {
        return delegate.bboolean(a, b);
    }

    public boolean[] bbooleanArray(final boolean[] a, final boolean b[]) {
        return delegate.bbooleanArray(a, b);
    }
}
