/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.script.xml;

public final class TestBeanComposer {

    private final TestBean bean1;
    private final TestBean bean2;

    public TestBeanComposer(final TestBean bean1, final TestBean bean2) {
        this.bean1 = bean1;
        this.bean2 = bean2;
    }

    public TestBean getBean1() {
        return bean1;
    }

    public TestBean getBean2() {
        return bean2;
    }
}
