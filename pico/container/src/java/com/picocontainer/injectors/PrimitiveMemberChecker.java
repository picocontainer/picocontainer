/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 * Original Code By: Centerline Computers, Inc.                              *
 *****************************************************************************/
package com.picocontainer.injectors;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *
 * @author Michael Rimov
 */
public class PrimitiveMemberChecker {

    /**
     * Checks if the target argument is primative.
     * @param member target member instance, may be constructor, field, or method.
     * @param i parameter index.
     * @return true if the target object's "i"th parameter is a primitive (ie, int, float, etc)
     * @throws UnsupportedOperationException if for some reason the member parameter
     * is not a Constructor, Method, or Field.
     * @throws ArrayIndexOutOfBoundsException if 'i' is an inappropriate index for the
     * given parameters.  For example, i should never be anything but zero for a field.
     */
    public static boolean isPrimitiveArgument(final AccessibleObject member, final int i) throws ArrayIndexOutOfBoundsException, UnsupportedOperationException {
        Class[] types;
        if (member instanceof Constructor) {
            types = ((Constructor)member).getParameterTypes();
        } else if (member instanceof Method) {
            types = ((Method)member).getParameterTypes();
        } else if (member instanceof Field) {
            types = new Class[1];
            types[0] = ((Field)member).getType();
        } else {
            //Should be field/constructor/method only.
            throw new UnsupportedOperationException("Unsupported member type: " + member.getClass());
        }

        if (i >= types.length) {
            throw new ArrayIndexOutOfBoundsException("Index i > types array length "
                + types.length + " for member " + member);
        }

        return types[i].isPrimitive();

    }

}
