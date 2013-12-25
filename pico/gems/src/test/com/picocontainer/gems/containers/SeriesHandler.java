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

public class SeriesHandler {
	private final Processor processor;
	private final int multiplier;

	public SeriesHandler(Processor processor) {
		this(processor, 1);
	}

	public SeriesHandler(Processor processor, int multiplier) {
		this.processor = processor;
		this.multiplier = multiplier;
	}

	public void handleSeries(int... elements) {
		processor.startProcessing();
		for(int element : elements) {
			processor.processElement(element);
		}
		processor.terminateProcessing();
	}

	public int getResult() {
		return multiplier * processor.getResult();
	}
}
