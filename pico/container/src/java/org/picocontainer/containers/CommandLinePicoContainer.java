/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.containers;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.TypeOf;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.List;

/**
 * CommandLineArgumentsPicoContainer configured itself from array of strings
 * which are most likely coming in as command line arguments
 * 
 */
@SuppressWarnings("serial")
public class CommandLinePicoContainer extends AbstractDelegatingPicoContainer {
    public CommandLinePicoContainer(char separator, String... arguments) {
    	this(separator, (PicoContainer) null, arguments);
    }

    public CommandLinePicoContainer(char separator, PicoContainer parent, String... arguments) {
    	super(new DefaultPicoContainer(parent));
        for (String argument : arguments) {
            processArgument(argument, separator);
        }
    }
    public CommandLinePicoContainer(char separator, StringReader argumentsProps) throws IOException {
        this(separator, argumentsProps, new String[0]);
    }
    
    public CommandLinePicoContainer(char separator, StringReader argumentProperties, String... arguments) throws IOException{
    	this(separator, argumentProperties, null, arguments);
    }

    public CommandLinePicoContainer(char separator, StringReader argumentProperties, PicoContainer parent, String... arguments)
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
    
    public CommandLinePicoContainer(String... arguments) {
        this('=', arguments);
    }

    public CommandLinePicoContainer(PicoContainer parent, String... arguments) {
    	this('=', parent, arguments);
    }

    private void addConfig(String key, Object val) {
        if (getDelegate().getComponentInto(key, ComponentAdapter.NOTHING.class) != null) {
            getDelegate().removeComponent(key);
        }
        getDelegate().addConfig(key, val);
    }

    public <T> T getComponentInto(Class<T> componentType, Type into) {
        return null;
    }

    public <T> List<ComponentAdapter<T>> getComponentAdapters(TypeOf<T> componentType) {
        return null;
    }

    public <T> List<ComponentAdapter<T>> getComponentAdapters(Class<T> componentType) {
        return null;
    }

    public PicoContainer getParent() {
        return new EmptyPicoContainer();
    }

    private void processArgument(String argument, char separator) {
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
    
    public MutablePicoContainer getDelegate() {
    	return (MutablePicoContainer) super.getDelegate();
    }

    public void setName(String s) {
        ((DefaultPicoContainer)getDelegate()).setName(s);
    }

}
