/**
 * 
 */
package org.picocontainer.script;

import org.picocontainer.PicoContainer;
import org.picocontainer.Startable;

/**
 * Enhances the container builder by starting the container after it has been composed. 
 * @author Joe Walnes
 * @author Aslak Helles&oslash;y
 * @author Paul Hammant
 * @author Mauro Talevi
 * @author Michael Rimov, Centerline Computers, Inc.
 */
public class AutoStartingContainerBuilder implements ContainerBuilder {
	
	private final ContainerBuilder delegate;

	public AutoStartingContainerBuilder(ContainerBuilder toDecorate) {
		this.delegate = toDecorate;
		
	}

	/**
	 * Decorates calls to ContainerBuilder by starting the constructed PicoContainer (if 
	 * the PicoContainer implements {@link org.picocontainer.Startable Startable}).
	 */
	public PicoContainer buildContainer(PicoContainer parentContainer,
			Object assemblyScope, boolean addChildToParent) {
		PicoContainer container = delegate.buildContainer(parentContainer, assemblyScope, addChildToParent);
        if (container instanceof Startable) {
            ((Startable) container).start();
        }
        return container;
	}


	/**
	 * Stops and Disposes of the container before calling the delegate killContainer(PicoContainer).
	 */
	public void killContainer(PicoContainer container) {
        delegate.killContainer(container);
	}

}
