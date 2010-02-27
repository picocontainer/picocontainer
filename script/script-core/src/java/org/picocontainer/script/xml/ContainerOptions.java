/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.script.xml;

import org.w3c.dom.Element;
import static org.picocontainer.script.xml.AttributeUtils.*;
import static org.picocontainer.script.xml.XMLConstants.*;

/**
 * Extensible way to bundle up attributes for the container
 * XML node.
 * @author Mike Rimov
 *
 */
public class ContainerOptions {
	
    private final Element rootElement;

	public ContainerOptions(Element rootElement) {
		this.rootElement = rootElement;
	}
	
	
	public boolean isInheritParentBehaviors() {
        return boolValue(rootElement.getAttribute(INHERIT_BEHAVIORS_ATTRIBUTE), false);
		
	}
	
	public boolean isCaching() {
		return boolValue(rootElement.getAttribute(CACHING_ATTRIBUTE), true);
	}
	
	public String getMonitorName() {
		return rootElement.getAttribute(COMPONENT_MONITOR);
	}
	
	public String getComponentFactoryName() {
		return rootElement.getAttribute(COMPONENT_ADAPTER_FACTORY);		
	}
	
}
