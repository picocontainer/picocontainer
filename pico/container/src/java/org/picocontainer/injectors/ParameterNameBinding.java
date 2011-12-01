/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import org.picocontainer.NameBinding;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Member;

import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

public class ParameterNameBinding implements NameBinding {
    private final AccessibleObject member;
    private final int index;
    private final Paranamer paranamer;

    private String name;

    public ParameterNameBinding(Paranamer paranamer, AccessibleObject member, int index) {
        this.member = member;
        this.paranamer = paranamer;
        this.index = index;
    }

    public String getName() {
        if (name != null) {
            return name;
        }
        String[] strings = paranamer.lookupParameterNames(member, false);
        name = strings.length == 0 ? "" : strings[index];
        return name;
    }
}

