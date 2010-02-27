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

import org.picocontainer.BehaviorFactory;

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
    
    public static BehaviorFactory implementationHiding() {
        return new ImplementationHiding();
    }

    public static BehaviorFactory caching() {
        return new Caching();
    }

    public static BehaviorFactory synchronizing() {
        return new Synchronizing();
    }

    public static BehaviorFactory locking() {
        return new Locking();
    }

    public static BehaviorFactory propertyApplying() {
        return new PropertyApplying();
    }

    public static BehaviorFactory automatic() {
        return new Automating();
    }

}
