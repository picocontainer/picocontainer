/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.script.jython;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;

import com.picocontainer.script.ScriptedContainerBuilder;
import com.picocontainer.script.ScriptedPicoContainerMarkupException;
import org.python.core.Py;
import org.python.util.PythonInterpreter;

import com.picocontainer.PicoContainer;

/**
 * {@inheritDoc}
 * The script has to assign a "pico" variable with an instance of
 * {@link PicoContainer}.
 * There is an implicit variable named "parent" that may contain a reference to a parent
 * container. It is recommended to use this as a constructor argument to the instantiated
 * PicoContainer.
 *
 * @author Paul Hammant
 * @author Mike Royle
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 */
public class JythonContainerBuilder extends ScriptedContainerBuilder {

    public JythonContainerBuilder(final Reader script, final ClassLoader classLoader) {
    	super(script,classLoader);
    }

    public JythonContainerBuilder(final URL script, final ClassLoader classLoader) {
        super(script, classLoader);
    }

    @Override
	protected PicoContainer createContainerFromScript(final PicoContainer parentContainer, final Object assemblyScope) {
    	ClassLoader  oldClassLoader = Thread.currentThread().getContextClassLoader();
    	ClassLoader pyClassLoader = Py.getSystemState().getClassLoader();
        try {
        	Thread.currentThread().setContextClassLoader(getClassLoader());
        	Py.getSystemState().setClassLoader(getClassLoader());

            PythonInterpreter interpreter = new PythonInterpreter();

            interpreter.set("parent", parentContainer);
            interpreter.set("assemblyScope", assemblyScope);
            interpreter.execfile(getScriptInputStream(), "picocontainer.py");
            return interpreter.get("pico", PicoContainer.class);
        } catch (IOException e) {
            throw new ScriptedPicoContainerMarkupException(e);
        } finally {
        	Thread.currentThread().setContextClassLoader(oldClassLoader);
        	Py.getSystemState().setClassLoader(pyClassLoader);
        }
    }
}
