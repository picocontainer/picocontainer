/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.script.jython;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;

import org.picocontainer.PicoContainer;
import org.picocontainer.script.ScriptedContainerBuilder;
import org.picocontainer.script.ScriptedPicoContainerMarkupException;
import org.python.util.PythonInterpreter;

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

    public JythonContainerBuilder(Reader script, ClassLoader classLoader) {
    	super(script,classLoader);
    }

    public JythonContainerBuilder(URL script, ClassLoader classLoader) {
        super(script, classLoader);
    }

    protected PicoContainer createContainerFromScript(PicoContainer parentContainer, Object assemblyScope) {
        try {
            PythonInterpreter interpreter = new PythonInterpreter();
            interpreter.set("parent", parentContainer);
            interpreter.set("assemblyScope", assemblyScope);
            interpreter.execfile(getScriptInputStream(), "picocontainer.py");
            return (PicoContainer) interpreter.get("pico", PicoContainer.class);
        } catch (IOException e) {
            throw new ScriptedPicoContainerMarkupException(e);
        }
    }
}
