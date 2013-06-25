/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.containers;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.List;


import com.googlecode.jtype.Generic;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;

/**
 * CommandLineArgumentsPicoContainer configured itself from array of strings
 * which are most likely coming in as command line arguments
 *
 */
@SuppressWarnings("serial")
public class CommandLinePicoContainer extends AbstractDelegatingPicoContainer {
    public CommandLinePicoContainer(final char separator, final String... arguments) {
    	this(separator, (PicoContainer) null, arguments);
    }

    public CommandLinePicoContainer(final char separator, final PicoContainer parent, final String... arguments) {
    	super(new DefaultPicoContainer(parent));
        for (String argument : arguments) {
            processArgument(argument, separator);
        }
    }
    public CommandLinePicoContainer(final char separator, final StringReader argumentsProps) throws IOException {
        this(separator, argumentsProps, new String[0]);
    }

    public CommandLinePicoContainer(final char separator, final StringReader argumentProperties, final String... arguments) throws IOException{
    	this(separator, argumentProperties, null, arguments);
    }

    public CommandLinePicoContainer(final char separator, final StringReader argumentProperties, final PicoContainer parent, final String... arguments)
        throws IOException {
    	super(new DefaultPicoContainer(parent));

        LineNumberReader lnr = new LineNumberReader(argumentProperties);
        String line = lnr.readLine();
        while (line != null) {
            processArgument(line, separator);
            line = lnr.readLine();
        }
        for (String argument : arguments) {
            processArgument(argument, separator);
        }
    }

    public CommandLinePicoContainer(final String... arguments) {
        this('=', arguments);
    }

    public CommandLinePicoContainer(final PicoContainer parent, final String... arguments) {
    	this('=', parent, arguments);
    }

    private void addConfig(final String key, final Object val) {
        if (getDelegate().getComponentInto(key, ComponentAdapter.NOTHING.class) != null) {
            getDelegate().removeComponent(key);
        }
        getDelegate().addConfig(key, val);
    }

    @Override
	public <T> T getComponentInto(final Class<T> componentType, final Type into) {
        return null;
    }

    @Override
	public <T> T getComponentInto(final Generic<T> componentType, final Type into) {
        return null;
    }

    @Override
	public <T> List<ComponentAdapter<T>> getComponentAdapters(final Generic<T> componentType) {
        return null;
    }

    @Override
	public <T> List<ComponentAdapter<T>> getComponentAdapters(final Class<T> componentType) {
        return null;
    }

    @Override
	public PicoContainer getParent() {
        return new EmptyPicoContainer();
    }

    private void processArgument(final String argument, final char separator) {
        String[] kvs = argument.split(Character.valueOf(separator).toString());
        if (kvs.length == 2) {
            addConfig(kvs[0], kvs[1]);
        } else if (kvs.length == 1) {
            addConfig(kvs[0], "true");
        } else if (kvs.length > 2) {
            throw new PicoCompositionException(
                "Argument name'"+separator+"'value pair '" + argument + "' has too many '"+separator+"' characters");
        }
    }

    @Override
	public MutablePicoContainer getDelegate() {
    	return (MutablePicoContainer) super.getDelegate();
    }

    public void setName(final String s) {
        getDelegate().setName(s);
    }


    @Override
    public String toString() {
        return "[CommandLine]:" + super.getDelegate().toString();
    }

}
