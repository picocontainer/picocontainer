/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.gems.adapters;

import java.io.IOException;

public class ElephantImpl implements Elephant {
    public String objects(final String one, final String two) throws IOException {
        return one + two;
    }

    public String[] objectsArray(final String[] one, final String[] two) throws IOException {
        return new String[] { one[0] + two[0]};
    }

    public int iint(final int a, final int b) {
        return a + b;
    }

    public long llong(final long a, final long b) {
        return a + b;
    }

    public byte bbyte(final byte a, final byte b, final byte c) {
        return (byte) (a + b + c);
    }

    public float ffloat(final float a, final float b, final float c, final float d) {
        return a + b + c + d;
    }

    public double ddouble(final double a, final double b) {
        return a + b;
    }

    public char cchar(final char a, final char b) {
        return a == 'a' && b == 'b' ? 'c' : '!';
    }

    public short sshort(final short a, final short b) {
        return (short) (a + b);
    }

    public boolean bboolean(final boolean a, final boolean b) {
        return a & b;
    }

    public boolean[] bbooleanArray(final boolean[] a, final boolean b[]) {
        return new boolean[] { a[0] & b[0]};
    }
}