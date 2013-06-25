/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Leo Simons                                               *
 *****************************************************************************/
package com.picocontainer.script.bsh;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

import com.picocontainer.Parameter;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.adapters.AbstractAdapter;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * This adapter relies on <a href="http://beanshell.org/">Bsh</a> for instantiation
 * (and possibly also initialisation) of component instances.
 * <p/>
 * When {@link com.picocontainer.ComponentAdapter#getComponentInstance} is called (by PicoContainer),
 * the adapter instance will look for a script with the same name as the component implementation
 * class (but with the .bsh extension). This script must reside in the same folder as the class.
 * (It's ok to have them both in a jar).
 * <p/>
 * The bsh script's only contract is that it will have to instantiate a bsh variable called
 * "instance".
 * <p/>
 * The script will have access to the following variables:
 * <ul>
 * <li>addAdapter - the adapter calling the script</li>
 * <li>picoContainer - the MutablePicoContainer calling the addAdapter</li>
 * <li>key - the component key</li>
 * <li>impl - the component implementation</li>
 * <li>parameters - the ComponentParameters (as a List)</li>
 * </ul>
 * @author <a href="mail at leosimons dot com">Leo Simons</a>
 * @author Aslak Hellesoy
 */
@SuppressWarnings({ "unchecked", "serial" })
public class BeanShellAdapter extends AbstractAdapter {
    private final Parameter[] parameters;

    private Object instance = null;

    /**
     * Classloader to set for the BeanShell interpreter.
     */
    private final ClassLoader classLoader;

    public BeanShellAdapter(final Object key, final Class<?> impl, final Parameter[] parameters, final ClassLoader classLoader) {
        super(key, impl);
        this.parameters = parameters;
        this.classLoader = classLoader;
    }

    public BeanShellAdapter(final Object key, final Class<?> impl, final Parameter... parameters) {
        this(key, impl, parameters, BeanShellAdapter.class.getClassLoader());
    }

    public Object getComponentInstance(final PicoContainer pico, final Type into)
            throws PicoCompositionException
    {

        if (instance == null) {
            try {
                Interpreter i = new Interpreter();
                i.setClassLoader(classLoader);
                i.set("addAdapter", this);
                i.set("picoContainer", pico);
                i.set("key", getComponentKey());
                i.set("impl", getComponentImplementation());
                i.set("parameters", parameters != null ? Arrays.asList(parameters) : Collections.EMPTY_LIST);
                i.eval("import " + getComponentImplementation().getName() + ";");

                String scriptPath = "/" + getComponentImplementation().getName().replace('.', '/') + ".bsh";

                // Inside IDEA, this relies on the compilation output path being the same directory as the source path.
                // kludge - copy ScriptableDemoBean.bsh to the same location in the test output compile class path.
                // the same problem exists for maven, but some custom jelly script will be able to fix that.
                URL scriptURL = getComponentImplementation().getResource(scriptPath);
                if (scriptURL == null) {
                    throw new BeanShellScriptCompositionException("Couldn't load script at path " + scriptPath);
                }
                Reader sourceReader = new InputStreamReader(scriptURL.openStream());
                i.eval(sourceReader, i.getNameSpace(), scriptURL.toExternalForm());

                instance = i.get("instance");
                if (instance == null) {
                    throw new BeanShellScriptCompositionException("The 'instance' variable was not instantiated");
                }
            } catch (EvalError e) {
                throw new BeanShellScriptCompositionException(e);
            } catch (IOException e) {
                throw new BeanShellScriptCompositionException(e);
            }
        }
        return instance;
    }

    public void verify(final PicoContainer pico) {
    }

    public String getDescriptor() {
        return "BeanShellConsole";
    }
}
