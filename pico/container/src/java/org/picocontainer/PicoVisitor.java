/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer;

/**
 * Interface realizing a visitor pattern for {@link PicoContainer} as described in the GoF.
 * The visitor should visit the container, its children, all registered {@link ComponentAdapter}
 * instances and all instantiated components.
 * 
 * @author Aslak Helles&oslash;y
 * @author J&ouml;rg Schaible
 */
public interface PicoVisitor {
	
	
	/**
	 * Constant that indicates that the traversal should continue after the 
	 * visit*() method has been called.
	 */
	boolean CONTINUE_TRAVERSAL = true;
	
	/**
	 * Constant that indicates that the traversal should abort after the 
	 * visit*() method has been called.
	 */
	boolean ABORT_TRAVERSAL = false;
	
    /**
     * Entry point for the PicoVisitor traversal. The given node is the first object, that is 
     * asked for acceptance. Only objects of type {@link PicoContainer}, {@link ComponentAdapter},
     * or {@link Parameter} are valid.
     * 
     * @param node the start node of the traversal.
     * @return a visitor-specific value.
     * @throws IllegalArgumentException in case of an argument of invalid type. 
     */
    Object traverse(Object node);

    /**
     * Visit a {@link PicoContainer} that has to accept the visitor.
     * 
     * @param pico the visited container.
     * @return CONTINUE_TRAVERSAL if the traversal should continue.  
     * Any visitor callback that returns ABORT_TRAVERSAL indicates
     * the desire to abort any further traversal.
     */
    boolean visitContainer(PicoContainer pico);

    /**
     * Visit a {@link ComponentAdapter} that has to accept the visitor.
     * 
     * @param componentAdapter the visited ComponentAdapter.
     */
    void visitComponentAdapter(ComponentAdapter<?> componentAdapter);

    /**
     * Visit a {@link ComponentAdapter} that has to accept the visitor.
     *
     * @param componentAdapter the visited ComponentAdapter.
     */
    void visitComponentFactory(ComponentFactory componentFactory);

    /**
     * Visit a {@link Parameter} that has to accept the visitor.
     * 
     * @param parameter the visited Parameter.
     */
    void visitParameter(Parameter parameter);
}
