/*****************************************************************************
 * Copyright (C) 2003-2013 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Serban Iordache                                          *
 *****************************************************************************/
package com.picocontainer.gems.containers;

public class AbsProcessor implements Processor {
	private final Processor delegate;

	public AbsProcessor(Processor delegate) {
		this.delegate = delegate;
	}

	@Override
	public void startProcessing() {
		delegate.startProcessing();
	}

	@Override
	public void processElement(int element) {
		delegate.processElement(Math.abs(element));
	}

	@Override
	public void terminateProcessing() {
		delegate.terminateProcessing();
	}

	@Override
	public int getResult() {
		return delegate.getResult();
	}

}
