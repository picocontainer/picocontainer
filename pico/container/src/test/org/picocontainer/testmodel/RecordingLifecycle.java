/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the license.txt file.                                                    *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/
package org.picocontainer.testmodel;

import static org.junit.Assert.assertNotNull;

import org.picocontainer.Disposable;
import org.picocontainer.PicoContainer;
import org.picocontainer.Startable;


public abstract class RecordingLifecycle implements Startable, Disposable {
    private final StringBuffer recording;

    protected RecordingLifecycle(final StringBuffer recording) {
        this.recording = recording;
    }

    public void start() {
        recording.append("<").append(code());
    }

    public void stop() {
        recording.append(code()).append(">");
    }

    public void dispose() {
        recording.append("!").append(code());
    }

    public String recording() {
        return recording.toString();
    }

    private String code() {
        String name = getClass().getName();
        int idx = Math.max(name.lastIndexOf('$'), name.lastIndexOf('.'));
        return name.substring(idx + 1);
    }

    public interface Recorder extends  Startable, Disposable {
        String recording();
    }

    public static class One extends RecordingLifecycle implements Recorder {
        public One(final StringBuffer sb) {
            super(sb);
        }
    }

    public static class Two extends RecordingLifecycle {
        public Two(final StringBuffer sb, final One one) {
            super(sb);
            assertNotNull(one);
        }
    }

    public static class Three extends RecordingLifecycle {
        public Three(final StringBuffer sb, final One one, final Two two) {
            super(sb);
            assertNotNull(one);
            assertNotNull(two);
        }
    }

    public static class Four extends RecordingLifecycle {
        public Four(final StringBuffer sb, final Two two, final Three three, final One one) {
            super(sb);
            assertNotNull(one);
            assertNotNull(two);
            assertNotNull(three);
        }
    }

    public static class FiveTriesToBeMalicious extends RecordingLifecycle {
        public FiveTriesToBeMalicious(final StringBuffer sb, final PicoContainer pc) {
            super(sb);
            assertNotNull(pc);
            sb.append("Whao! Should not get instantiated!!");
        }
    }

}