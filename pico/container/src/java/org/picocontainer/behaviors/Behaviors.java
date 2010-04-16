/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.behaviors;

import org.picocontainer.Behaving;
import org.picocontainer.Behavior;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ObjectReference;

/**
 * Static collection of factory methods for different BehaviourFactory implementations.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class Behaviors {

    /**
     * Prevents instantiation
     */
    private Behaviors(){
        // no-op
    }
    
    public static Behavior implementationHiding() {
        return new ImplementationHiding();
    }

    public static Behavior caching() {
        return new Caching();
    }

    public static Behavior synchronizing() {
        return new Synchronizing();
    }

    public static Behavior locking() {
        return new Locking();
    }

    public static Behavior propertyApplying() {
        return new PropertyApplying();
    }

    public static Behavior automatic() {
        return new Automating();
    }

    public static <T> Behaving<T> cached(ComponentAdapter<T> delegate) {
        return new Cached<T>(delegate);
    }

    public static <T> Behaving<T> cached(ComponentAdapter<T> delegate, ObjectReference instanceReference) {
        return new Cached<T>(delegate, instanceReference);
    }

    public static <T> Behaving<T> decorated(ComponentAdapter<T> delegate, Decorated.Decorator decorator) {
        return new Decorated<T>(delegate, decorator);
    }
}
