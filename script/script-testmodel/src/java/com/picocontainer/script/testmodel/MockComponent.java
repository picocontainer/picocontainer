/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Mike Hogan                                               *
 *****************************************************************************/


package com.picocontainer.script.testmodel;

public interface MockComponent {
    int getPort();

    void setPort(int port);

    String getServer();

    void setServer(String server);

    void addRegister(Integer i);

    int getNumRegisters();

    boolean hasRegister(int i);
}
