package com.picocontainer.script.bsh;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;

import com.picocontainer.script.ScriptedContainerBuilder;
import com.picocontainer.script.ScriptedPicoContainerMarkupException;

import com.picocontainer.PicoContainer;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * {@inheritDoc}
 * The script has to assign a "pico" variable with an instance of
 * {@link com.picocontainer.PicoContainer}.
 * There is an implicit variable named "parent" that may contain a reference to a parent
 * container. It is recommended to use this as a constructor argument to the instantiated
 * PicoContainer.
 *
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 * @author Mauro Talevi
 */
public class BeanShellContainerBuilder extends ScriptedContainerBuilder {

    public BeanShellContainerBuilder(final Reader script, final ClassLoader classLoader) {
        super(script, classLoader);
    }

    public BeanShellContainerBuilder(final URL script, final ClassLoader classLoader) {
    	super(script, classLoader);
    }


    @Override
	protected PicoContainer createContainerFromScript(final PicoContainer parentContainer, final Object assemblyScope) {
        Interpreter i = new Interpreter();
        try {
            i.set("parent", parentContainer);
            i.set("assemblyScope", assemblyScope);
            i.setClassLoader(this.getClassLoader());
            i.eval(getScriptReader(), i.getNameSpace(), "picocontainer.bsh");
            return (PicoContainer) i.get("pico");
        } catch (EvalError e) {
            throw new ScriptedPicoContainerMarkupException(e);
        } catch (IOException e) {
            throw new ScriptedPicoContainerMarkupException(e);
        }
    }
}
