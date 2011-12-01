/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.injectors;

import org.picocontainer.InjectionType;

import java.lang.annotation.Annotation;

public class Injectors {

    public static InjectionType adaptiveDI() {
        return new AdaptingInjection();
    }

    public static InjectionType annotatedMethodDI(Class<? extends Annotation> injectionAnnotation) {
        return new AnnotatedMethodInjection(injectionAnnotation, false);
    }

    public static InjectionType SDI() {
        return new SetterInjection();
    }

    public static InjectionType CDI() {
        return new ConstructorInjection();
    }

    public static InjectionType namedMethod() {
        return new NamedMethodInjection();
    }

    public static InjectionType namedField() {
        return new NamedFieldInjection();
    }

    public static InjectionType annotatedMethodDI() {
        return new AnnotatedMethodInjection();
    }

    public static InjectionType annotatedFieldDI(Class<? extends Annotation> injectionAnnotation) {
        return new AnnotatedFieldInjection(injectionAnnotation);
    }


    public static InjectionType annotatedFieldDI() {
        return new AnnotatedFieldInjection();
    }

    public static InjectionType typedFieldDI() {
        return new TypedFieldInjection();
    }

}
