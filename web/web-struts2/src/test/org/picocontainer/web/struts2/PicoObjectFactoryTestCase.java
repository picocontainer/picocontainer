/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.web.struts2;

import java.util.Collections;

import org.junit.Test;
import org.picocontainer.web.struts2.PicoObjectFactory;

/**
 * @author Mauro Talevi
 * @author Konstantin Pribluda
 */
public final class PicoObjectFactoryTestCase {

    @SuppressWarnings("unchecked")
	@Test(expected = ClassNotFoundException.class)
    public void testActionInstantiationWithInvalidClassName() throws Exception {
        PicoObjectFactory factory = new PicoObjectFactory();
        factory.buildBean("invalidAction", Collections.EMPTY_MAP);
    }

}
