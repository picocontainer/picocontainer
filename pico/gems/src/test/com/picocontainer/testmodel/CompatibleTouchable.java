/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaibe                                            *
 *****************************************************************************/

package com.picocontainer.testmodel;

import java.io.Serializable;


/**
 * Method compatible Touchable.
 *
 * @author J&ouml;rg Schaible
 */
@SuppressWarnings("serial")
public class CompatibleTouchable implements Serializable {

	private boolean wasTouched;

    public void touch() {
        wasTouched = true;
    }

    public boolean wasTouched() {
        return wasTouched;
    }
}