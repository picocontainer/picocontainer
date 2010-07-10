/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.picocontainer.PicoContainer;

/**
 * Abstract class for script-based container builders
 *
 * @author Aslak Helles&oslash;y
 * @author Obie Fernandez
 * @author Mauro Talevi
 */
public abstract class ScriptedContainerBuilder extends AbstractContainerBuilder {
    
    private final Reader scriptReader;
    private final URL scriptURL;
    private final ClassLoader classLoader;
    
    public ScriptedContainerBuilder(Reader script, ClassLoader classLoader) {
    	this(script,classLoader, LifecycleMode.AUTO_LIFECYCLE);
    }

    public ScriptedContainerBuilder(Reader script, ClassLoader classLoader, LifecycleMode lifecycleMode) {
        super(lifecycleMode);
    	this.scriptReader = script;
        if (script == null) {
            throw new NullPointerException("script");
        }
        this.scriptURL = null;
        this.classLoader = classLoader;
        if (classLoader == null) {
            throw new NullPointerException("classLoader");
        }
    }
    
    public ScriptedContainerBuilder(URL script, ClassLoader classLoader)  {
    	this(script,classLoader, LifecycleMode.AUTO_LIFECYCLE);
    }

    public ScriptedContainerBuilder(URL script, ClassLoader classLoader, LifecycleMode lifecycleMode) {
        super(lifecycleMode);
    	this.scriptReader = null;        
        this.scriptURL = script;
        if (script == null) {
            throw new NullPointerException("script");
        }
        this.classLoader = classLoader;
        if (classLoader == null) {
            throw new NullPointerException("classLoader");
        }
    }

    @Override
    protected final PicoContainer createContainer(PicoContainer parentContainer, Object assemblyScope) {
        try {
            return createContainerFromScript(parentContainer, assemblyScope);
        } finally {
            try {
                Reader reader = getScriptReader();
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                // do nothing. we've given it our best try, now get on with it
            }
        }
    }

    protected final ClassLoader getClassLoader() {
        return classLoader;
    }
    
    @SuppressWarnings("synthetic-access")
    protected final InputStream getScriptInputStream() throws IOException{
        if (scriptReader != null) {
            return new InputStream() {
                @Override
                public int read() throws IOException {
                    return scriptReader.read();
                }
            };
        }
        return scriptURL.openStream();
    }

    protected final Reader getScriptReader() throws IOException{
        if (scriptReader != null) {
            return scriptReader;
        }
        return new InputStreamReader(scriptURL.openStream());
    }
    
    protected abstract PicoContainer createContainerFromScript(PicoContainer parentContainer, Object assemblyScope);

}