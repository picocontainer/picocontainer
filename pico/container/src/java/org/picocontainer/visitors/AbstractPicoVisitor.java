/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.visitors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.picocontainer.PicoException;
import org.picocontainer.PicoVisitor;

/**
 * Abstract PicoVisitor implementation. A generic traverse method is implemented, that
 * accepts any object with a method named &quot;accept&quot;, that takes a
 * {@link PicoVisitor}  as argument and and invokes it. Additionally it provides the
 * {@link #checkTraversal()} method, that throws a {@link PicoVisitorTraversalException},
 * if currently no traversal is running.
 *
 * @author J&ouml;rg Schaible
 */
@SuppressWarnings("serial")
public abstract class AbstractPicoVisitor implements PicoVisitor {
    private boolean traversal;

    public Object traverse(final Object node) {
        traversal = true;
        Object retval =
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        try {
                            return node.getClass().getMethod("accept", PicoVisitor.class);
                        } catch (NoSuchMethodException e) {
                            return e;
                        }
                    }
                });
        try {
            if (retval instanceof NoSuchMethodException) {
                throw (NoSuchMethodException) retval;
            }
            Method accept = (Method) retval;
            accept.invoke(node, this);
            return Void.TYPE;
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException)cause;
            } else if (cause instanceof Error) {
                throw (Error)cause;
            }
        } finally {
            traversal = false;
        }
        throw new IllegalArgumentException(node.getClass().getName() + " is not a valid type for traversal");
    }

    /**
     * Checks the traversal flag, indicating a currently running traversal of the visitor.
     * @throws PicoVisitorTraversalException if no traversal is active.
     */
    protected void checkTraversal() {
        if (!traversal) {
            throw new PicoVisitorTraversalException(this);
        }
    }

    /**
     * Exception for a PicoVisitor, that is dependent on a defined starting point of the traversal.
     * If the traversal is not initiated with a call of {@link PicoVisitor#traverse}
     *
     * @author joehni
     */
    public static class PicoVisitorTraversalException
            extends PicoException {

        /**
         * Construct the PicoVisitorTraversalException.
         *
         * @param visitor The visitor casing the exception.
         */
        public PicoVisitorTraversalException(final PicoVisitor visitor) {
            super("Traversal for PicoVisitor of type " + visitor.getClass().getName() + " must start with the visitor's traverse method");
        }
    }

}
