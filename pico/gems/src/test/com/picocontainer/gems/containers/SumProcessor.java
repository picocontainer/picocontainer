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

public class SumProcessor implements Processor {
	private int result;

	@Override
	public void startProcessing() {
		result = 0;
	}

	@Override
	public void processElement(int element) {
		result += element;
	}

	@Override
	public int getResult() {
		return result;
	}

	@Override
	public void terminateProcessing() {
		// Empty implementation
	}

}
