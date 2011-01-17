/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/

/**
 * @author Aslak Helles&oslash;y
 */
package foo.bar;

import org.picocontainer.Startable;


public class Zap implements Startable {
    private final String hello;
    private String toString = "Not started";
    private boolean started = false;

    public Zap(String hello) {
        this.hello = hello;
    }

    public void start() {
        if(started) throw new IllegalStateException("Already started");
        toString = hello + " Started";
        System.out.println(toString() + hashCode());
        started = true;
    }

    public void stop() {
        if(!started) throw new IllegalStateException("Not started");
        toString = hello + " Stopped";
        System.out.println(toString() + hashCode());
        started = false;
    }

    public String toString() {
        return toString;
    }
}