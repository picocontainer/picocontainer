/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.injectors;

import org.picocontainer.ComponentMonitor;
import org.picocontainer.Parameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@SuppressWarnings("serial")
public class AnnotatedMethodInjector extends MethodInjector {

 	private final Class<? extends Annotation>[] injectionAnnotations;
    private String injectionAnnotationNames;

    public AnnotatedMethodInjector(Object key,
                                   Class<?> impl,
                                   Parameter[] parameters,
                                   ComponentMonitor monitor,
                                   boolean useNames, Class<? extends Annotation>... injectionAnnotations) {
        super(key, impl, parameters, monitor, "", useNames);
        this.injectionAnnotations = injectionAnnotations;
    }

    @Override
    protected final boolean isInjectorMethod(Method method) {
        for (Class<? extends Annotation> injectionAnnotation : injectionAnnotations) {
            if (method.getAnnotation(injectionAnnotation) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDescriptor() {
        if (injectionAnnotationNames == null) {
            injectionAnnotationNames = makeAnnotationNames(injectionAnnotations);
        }
        return "AnnotatedMethodInjector[" + injectionAnnotationNames + "]-";
    }

    static String makeAnnotationNames(Class<? extends Annotation>[] injectionAnnotations) {
        StringBuilder sb = new StringBuilder();
        for (Class<? extends Annotation> injectionAnnotation : injectionAnnotations) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            String name = injectionAnnotation.getName();
            sb.append(name.substring(0, name.lastIndexOf(".")+1)).append("@").append(name.substring(name.lastIndexOf(".")+1));
        }
        return sb.toString();
    }
}
