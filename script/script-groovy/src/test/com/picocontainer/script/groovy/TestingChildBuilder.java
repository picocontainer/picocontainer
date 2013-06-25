/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Aslak Hellesoy and Paul Hammant                          *
 *****************************************************************************/

package com.picocontainer.script.groovy;

import groovy.util.NodeBuilder;

import java.util.Map;

import com.picocontainer.MutablePicoContainer;

/**
 * @author Paul Hammant
 */
public final class TestingChildBuilder extends NodeBuilder {

    final MutablePicoContainer toOperateOn;

    public TestingChildBuilder(final MutablePicoContainer toOperateOn) {
        this.toOperateOn = toOperateOn;
    }

    @Override
	@SuppressWarnings("unchecked")
    protected Object createNode(final Object name, final Map map) {
        if (name.equals("component")) {
            return toOperateOn.addComponent(map.remove("key"), map.remove("class"));
        } else {
            return null;
        }
    }

}
