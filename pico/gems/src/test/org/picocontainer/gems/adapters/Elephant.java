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

public interface Elephant {

    String objects(String one, String two) throws IOException;

    String[] objectsArray(String[] one, String[] two) throws IOException;

    int iint(int a, int b);

    long llong(long a, long b);

    byte bbyte(byte a, byte b, byte c);

    float ffloat(float a, float b, float c, float d);

    double ddouble(double a, double b);

    char cchar(char a, char b);

    short sshort(short a, short b);

    boolean bboolean(boolean a, boolean b);

    boolean[] bbooleanArray(boolean[] a, boolean b[]);


}