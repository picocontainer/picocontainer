/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Aslak Hellesoy and Paul Hammant                          *
 *****************************************************************************/

package com.picocontainer.script.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import com.picocontainer.script.AbstractScriptedContainerBuilderTestCase;
import com.picocontainer.script.testmodel.DefaultWebServerConfig;
import org.xml.sax.SAXException;

import com.googlecode.jtype.Generic;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.NameBinding;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.PicoException;
import com.picocontainer.PicoVisitor;

/**
 * @author Maarten Grootendorst
 */
// TODO to rename?
public class NonMutablePicoContainerContainerTestCase extends AbstractScriptedContainerBuilderTestCase {

    @SuppressWarnings("unchecked")
    private class TestPicoContainer implements PicoContainer {
        public Object getComponent(final Object key) {
            return null;
        }

        public <T> T getComponent(final Class<T> componentType) {
            return null;
        }

        public <T> T getComponent(final Generic<T> componentType) {
            return null;
        }

		public <T> T getComponent(final Class<T> componentType,
				final Class<? extends Annotation> binding, final Type into) {
			return null;
		}

		public <T> T getComponent(final Class<T> componentType,
				final Class<? extends Annotation> binding) {
			return null;
		}

		public <T> ComponentAdapter<T> getComponentAdapter(
				final Class<T> componentType, final Class<? extends Annotation> binding) {
			return null;
		}

		public <T> ComponentAdapter<T> getComponentAdapter(
				final Generic<T> componentType, final Class<? extends Annotation> binding) {
			return null;
		}

		public <T> List<ComponentAdapter<T>> getComponentAdapters(
				final Class<T> componentType, final Class<? extends Annotation> binding) {
			return null;
		}

		public <T> List<ComponentAdapter<T>> getComponentAdapters(
				final Generic<T> componentType, final Class<? extends Annotation> binding) {
			return null;
		}

		public Object getComponentInto(final Object keyOrType, final Type into) {
			return null;
		}

		public <T> T getComponentInto(final Class<T> componentType, final Type into) {
			return null;
		}

		public <T> T getComponentInto(final Generic<T> componentType, final Type into) {
			return null;
		}

        public List getComponents() {
            return null;
        }

        public PicoContainer getParent() {
            return null;
        }

        public ComponentAdapter<?> getComponentAdapter(final Object key) {
            return null;
        }

        public <T> ComponentAdapter<T> getComponentAdapter(final Class<T> componentType, final NameBinding nameBinding) {
            return null;
        }

        public <T> ComponentAdapter<T> getComponentAdapter(final Generic<T> componentType, final NameBinding componentNameBinding) {
            return null;
        }

        public Collection<ComponentAdapter<?>> getComponentAdapters() {
            return null;
        }

        public <T> List<T> getComponents(final Class<T> type) throws PicoException {
            return null;
        }

        public void accept(final PicoVisitor containerVisitor) {
        }

        public <T> List<ComponentAdapter<T>> getComponentAdapters(final Class<T> componentType) {
            return null;
        }

        public <T> List<ComponentAdapter<T>> getComponentAdapters(final Generic<T> componentType) {
            return null;
        }
    }

    @Test public void testCreateSimpleContainerWithPicoContainer()
        throws ParserConfigurationException, SAXException, IOException, PicoCompositionException {
        Reader script = new StringReader("" +
                                         "<container>" +
                                         "  <component-implementation class='com.picocontainer.script.testmodel.DefaultWebServerConfig'/>" +
                                         "  <component-implementation key='com.picocontainer.script.testmodel.WebServer' class='com.picocontainer.script.testmodel.WebServerImpl'/>" +
                                         "</container>");

        PicoContainer pico = buildContainer(new XMLContainerBuilder(script, getClass().getClassLoader()),
                                            new TestPicoContainer(),
                                            "SOME_SCOPE");
        assertEquals(2, pico.getComponents().size());
        assertNotNull(pico.getComponent(DefaultWebServerConfig.class));
    }

    @Test public void testCreateSimpleContainerWithMutablePicoContainer()
        throws ParserConfigurationException, SAXException, IOException, PicoCompositionException {
        Reader script = new StringReader("" +
                                         "<container>" +
                                         "  <component-implementation class='com.picocontainer.script.testmodel.DefaultWebServerConfig'/>" +
                                         "  <component-implementation key='com.picocontainer.script.testmodel.WebServer' class='com.picocontainer.script.testmodel.WebServerImpl'/>" +
                                         "</container>");

        PicoContainer pico = buildContainer(new XMLContainerBuilder(script, getClass().getClassLoader()),
                                            new DefaultPicoContainer(),
                                            "SOME_SCOPE");
        assertEquals(2, pico.getComponents().size());
        assertNotNull(pico.getComponent(DefaultWebServerConfig.class));
        assertNotNull(pico.getParent());

    }
}
