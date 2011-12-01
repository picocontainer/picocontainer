/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Konstantin Pribluda                                      *
 *****************************************************************************/
package org.picocontainer.web.chain;

import java.util.ArrayList;
import java.util.List;

import org.picocontainer.PicoContainer;
import org.picocontainer.Startable;

/**
 * Represents chain of containers, which can be started and stopped at once. 
 * 
 * @author Konstantin Pribluda
 */
public final class ContainerChain implements Startable {

	private final List chain = new ArrayList();

	private PicoContainer last;

	/**
	 * Returns last container in chain.
	 * 
	 * @return last container in chain or null
	 */
	public PicoContainer getLast() {
		return last;
	}

	/**
	 * add new container to the end of chain.
	 * 
	 * @param container
	 */
	public void addContainer(PicoContainer container) {
		chain.add(container);
		last = container;
	}

	/**
	 * start each container in the chain
     */
	public void start() {
        for (Object aChain : chain) {
            ((Startable)aChain).start();
        }
    }

	/**
     * stop each container in the chain
	 */
	public void stop() {
		for (int i = chain.size() - 1; i >= 0; i--) {
			((Startable) chain.get(i)).stop();
		}
	}

}