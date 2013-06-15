/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.testmodel;

import java.beans.PropertyEditorSupport;

/**
 *
 * @author greg
 */
public class CoupleBeanEditor extends PropertyEditorSupport {
    private static final String PREFIX_A = "a's name:";
    private static final String PREFIX_B = "b's name:";
    private static final String SEPARATOR = ";";

    public CoupleBeanEditor() {
        super();
    }

    @Override
	public void setAsText(final String s) throws IllegalArgumentException {
        int startA = s.indexOf(PREFIX_A);
        int stopA = s.indexOf(SEPARATOR, startA+PREFIX_A.length());
        int startB = s.indexOf(PREFIX_B, stopA + SEPARATOR.length());
        int stopB = s.indexOf(SEPARATOR, startB+ PREFIX_B.length());
        if (startA < 0 || stopA < 0 || startB < 0 || stopB < 0) {
            throw new IllegalArgumentException("Can't parse " + s + " into a CoupleBean");
        }
        String nameA = s.substring(startA + PREFIX_A.length(), stopA);
        String nameB = s.substring(startB + PREFIX_B.length(), stopB);

        PersonBean a = new PersonBean();
        a.setName(nameA);
        PersonBean b = new PersonBean();
        b.setName(nameB);
        setValue(new CoupleBean(a, b));
    }
}
