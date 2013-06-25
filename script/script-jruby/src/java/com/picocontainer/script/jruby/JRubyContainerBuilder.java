/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 * Original code by Nick Sieger                                                                          *
 *****************************************************************************/

package com.picocontainer.script.jruby;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collections;

import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.exceptions.RaiseException;
import org.jruby.internal.runtime.GlobalVariable.Scope;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import com.picocontainer.script.ScriptedContainerBuilder;
import com.picocontainer.script.ScriptedPicoContainerMarkupException;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.behaviors.Caching;
import com.picocontainer.classname.ClassLoadingPicoContainer;
import com.picocontainer.classname.DefaultClassLoadingPicoContainer;
import com.picocontainer.containers.EmptyPicoContainer;

/**
 * The script uses the {@code scriptedcontainer.rb} script to create an instance of
 * {@link PicoContainer}. There are implicit variables named "$parent" and
 * "$assembly_scope".
 *
 * @author Nick Sieger
 */
public final class JRubyContainerBuilder extends ScriptedContainerBuilder {
	public static final String MARKUP_EXCEPTION_PREFIX = "scriptedbuilder: ";

	public static final String MARKUP_EXCEPTION_RUNTIME_ERROR = "(RuntimeError) ";

	private final String script;

	public JRubyContainerBuilder(final Reader script, final ClassLoader classLoader) {
		super(script, classLoader);
		this.script = toString(script);
	}

	private String toString(final Reader script) {
		int charsRead;
		char[] chars = new char[1024];
		StringWriter writer = new StringWriter();
		try {
			while ((charsRead = script.read(chars)) != -1) {
				writer.write(chars, 0, charsRead);
			}
		} catch (IOException e) {
			throw new RuntimeException("unable to read script from reader", e);
		}
		return writer.toString();
	}

	/**
	 * {@inheritDoc}
	 * <p>Latest method of invoking jruby script have been adapted from <a
	 * href="http://wiki.jruby.org/wiki/Java_Integration" title="Click to visit JRuby Wiki">
	 * JRuby wiki:</a></p>
	 * @todo create a way to prevent initialization and shutdown with each script invocation.
	 */
	@Override
	protected PicoContainer createContainerFromScript(PicoContainer parentContainer, final Object assemblyScope) {
        if (parentContainer == null) {
            parentContainer = new DefaultClassLoadingPicoContainer(getClassLoader(), new DefaultPicoContainer(new EmptyPicoContainer(), new Caching()));
        }

        if (! (parentContainer instanceof ClassLoadingPicoContainer)) {
        	if (parentContainer instanceof MutablePicoContainer) {
        		parentContainer = new DefaultClassLoadingPicoContainer(getClassLoader(), (MutablePicoContainer)parentContainer);
        	} else {
        		//We want this last because it will never propagate parent behaviors
        		parentContainer = new DefaultClassLoadingPicoContainer(getClassLoader(), new DefaultPicoContainer(parentContainer, new Caching()));
        	}
        }

		RubyInstanceConfig rubyConfig = new RubyInstanceConfig();

		Ruby ruby = JavaEmbedUtils.initialize(Collections.EMPTY_LIST, rubyConfig);
		ruby.getLoadService().require("com/picocontainer/script/jruby/scriptedbuilder");
		ruby.defineReadonlyVariable("$parent", JavaEmbedUtils.javaToRuby(ruby, parentContainer), Scope.GLOBAL);
		ruby.defineReadonlyVariable("$assembly_scope", JavaEmbedUtils.javaToRuby(ruby, assemblyScope), Scope.GLOBAL);

		try {

			//IRubyObject result = ruby.executeScript(script);
			IRubyObject result = JavaEmbedUtils.newRuntimeAdapter().eval(ruby, script);
			return (PicoContainer) JavaEmbedUtils.rubyToJava(ruby, result, PicoContainer.class);
		} catch (RaiseException re) {
			if (re.getCause() instanceof ScriptedPicoContainerMarkupException) {
				throw (ScriptedPicoContainerMarkupException) re.getCause();
			}

			String message = re.getMessage();
			if (message.startsWith(MARKUP_EXCEPTION_RUNTIME_ERROR + MARKUP_EXCEPTION_PREFIX)) {
				throw new ScriptedPicoContainerMarkupException(message.substring((MARKUP_EXCEPTION_RUNTIME_ERROR + MARKUP_EXCEPTION_PREFIX).length()));
			} else {
				throw new PicoCompositionException(message, re);
			}

		} finally {
			JavaEmbedUtils.terminate(ruby);
		}
	}
}
