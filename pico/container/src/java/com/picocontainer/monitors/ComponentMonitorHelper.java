/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Paul Hammaant                                            *
 *****************************************************************************/

package com.picocontainer.monitors;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.text.MessageFormat;

import com.picocontainer.ComponentMonitor;

/**
 * An abstract {@link ComponentMonitor} which supports all the message formats.
 *
 * @author Mauro Talevi
 */
public final class ComponentMonitorHelper  {

    public final static String INSTANTIATING = "PicoContainer: instantiating {0}";
    public final static String INSTANTIATED = "PicoContainer: instantiated {0} [{1} ms], component {2}, injected [{3}]";
    public final static String INSTANTIATION_FAILED = "PicoContainer: instantiation failed: {0}, reason: {1}";
    public final static String INVOKING = "PicoContainer: invoking {0} on {1}";
    public final static String INVOKED = "PicoContainer: invoked {0} on {1} [{2} ms]";
    public final static String INVOCATION_FAILED = "PicoContainer: invocation failed: {0} on {1}, reason: {2}";
    public final static String LIFECYCLE_INVOCATION_FAILED = "PicoContainer: lifecycle invocation failed: {0} on {1}, reason: {2}";
    public final static String NO_COMPONENT = "PicoContainer: No component for key: {0}";

    public static String format(final String template, final Object... arguments) {
        return MessageFormat.format(template, arguments);
    }

    public static String parmsToString(final Object[] injected) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < injected.length; i++) {
            String s = injected[i].getClass().getName();
            sb.append(s);
            if (i < injected.length-1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public static String ctorToString(final Constructor constructor) {
        Class[] params = constructor.getParameterTypes();
        StringBuffer sb = new StringBuffer(constructor.getName());
        sb.append("(");
        for (int i = 0; i < params.length; i++) {
            String s = params[i].getName();
            sb.append(s);
            if (i < params.length-1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static String methodToString(final Member member) {
    	StringBuilder sb = new StringBuilder(member.getName());

        if (member instanceof Method) {
            Class<?>[] params = ((Method) member).getParameterTypes();
            sb.append("(");
            for (int i = 0; i < params.length; i++) {
                String s = params[i].getName();
                sb.append(s);
                if (i < params.length-1) {
                    sb.append(", ");
                }
            }
            sb.append(")");
        }
        return sb.toString();
    }

    public static String getDeclaringTypeString(final Member m) {
    	if (m == null) {
    		return " null ";
    	}


    	return m.getDeclaringClass().getName();
    }

    public static String memberToString(final Member m) {
        if (m instanceof Field) {
            return getDeclaringTypeString(m) + "." +  toString((Field) m);
        } else {
            return getDeclaringTypeString(m) + "." +  methodToString(m);
        }
    }

    public static String toString(final Field field) {
        StringBuffer sb = new StringBuffer(field.getName());
        sb.append("(").append(field.getName()).append(")");
        return sb.toString();
    }

}
