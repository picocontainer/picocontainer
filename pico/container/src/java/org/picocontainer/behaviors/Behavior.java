package org.picocontainer.behaviors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ObjectReference;

/**
 * static collection of  factory methods for easier behavior creation
 *
 * @author Konstantin Pribluda
 */
public class Behavior {

    public static final org.picocontainer.Behavior cached(ComponentAdapter delegate) {
        return new Cached(delegate);
    }

    public static final org.picocontainer.Behavior cached(ComponentAdapter delegate, ObjectReference instanceReference) {
        return new Cached(delegate,instanceReference);
    }


    public static final  org.picocontainer.Behavior decorated(ComponentAdapter delegate, Decorated.Decorator decorator) {
        return new Decorated(delegate,decorator);
    }
}
