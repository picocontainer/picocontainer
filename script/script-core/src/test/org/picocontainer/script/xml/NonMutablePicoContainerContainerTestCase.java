/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Aslak Hellesoy and Paul Hammant                          *
 *****************************************************************************/

package org.picocontainer.script.xml;

import org.junit.Test;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.NameBinding;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.PicoVisitor;
import org.picocontainer.TypeOf;
import org.picocontainer.script.AbstractScriptedContainerBuilderTestCase;
import org.picocontainer.script.testmodel.DefaultWebServerConfig;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Maarten Grootendorst
 */
// TODO to rename?
public class NonMutablePicoContainerContainerTestCase extends AbstractScriptedContainerBuilderTestCase {

    @SuppressWarnings("unchecked")
    private class TestPicoContainer implements PicoContainer {
        public Object getComponent(Object key) {
            return null;
        }

        public <T> T getComponent(Class<T> componentType) {
            return null;
        }

        public <T> T getComponent(TypeOf<T> componentType) {
            return null;
        }

		public <T> T getComponent(Class<T> componentType,
				Class<? extends Annotation> binding, Type into) {
			return null;
		}

		public <T> T getComponent(Class<T> componentType,
				Class<? extends Annotation> binding) {
			return null;
		}

		public <T> ComponentAdapter<T> getComponentAdapter(
				Class<T> componentType, Class<? extends Annotation> binding) {
			return null;
		}

		public <T> ComponentAdapter<T> getComponentAdapter(
				TypeOf<T> componentType, Class<? extends Annotation> binding) {
			return null;
		}

		public <T> List<ComponentAdapter<T>> getComponentAdapters(
				Class<T> componentType, Class<? extends Annotation> binding) {
			return null;
		}

		public <T> List<ComponentAdapter<T>> getComponentAdapters(
				TypeOf<T> componentType, Class<? extends Annotation> binding) {
			return null;
		}

		public Object getComponentInto(Object keyOrType, Type into) {
			return null;
		}

		public <T> T getComponentInto(Class<T> componentType, Type into) {
			return null;
		}

        public List getComponents() {
            return null;
        }

        public PicoContainer getParent() {
            return null;
        }

        public ComponentAdapter<?> getComponentAdapter(Object key) {
            return null;
        }

        public <T> ComponentAdapter<T> getComponentAdapter(Class<T> componentType, NameBinding nameBinding) {
            return null;
        }

        public <T> ComponentAdapter<T> getComponentAdapter(TypeOf<T> componentType, NameBinding componentNameBinding) {
            return null;  
        }

        public Collection<ComponentAdapter<?>> getComponentAdapters() {
            return null;
        }

        public <T> List<T> getComponents(Class<T> type) throws PicoException {
            return null;
        }

        public void accept(PicoVisitor containerVisitor) {
        }

        public <T> List<ComponentAdapter<T>> getComponentAdapters(Class<T> componentType) {
            return null;
        }

        public <T> List<ComponentAdapter<T>> getComponentAdapters(TypeOf<T> componentType) {
            return null;
        }
    }

    @Test public void testCreateSimpleContainerWithPicoContainer()
        throws ParserConfigurationException, SAXException, IOException, PicoCompositionException
    {
        Reader script = new StringReader("" +
                                         "<container>" +
                                         "  <component-implementation class='org.picocontainer.script.testmodel.DefaultWebServerConfig'/>" +
                                         "  <component-implementation key='org.picocontainer.script.testmodel.WebServer' class='org.picocontainer.script.testmodel.WebServerImpl'/>" +
                                         "</container>");

        PicoContainer pico = buildContainer(new XMLContainerBuilder(script, getClass().getClassLoader()),
                                            new TestPicoContainer(),
                                            "SOME_SCOPE");
        assertEquals(2, pico.getComponents().size());
        assertNotNull(pico.getComponent(DefaultWebServerConfig.class));
    }

    @Test public void testCreateSimpleContainerWithMutablePicoContainer()
        throws ParserConfigurationException, SAXException, IOException, PicoCompositionException
    {
        Reader script = new StringReader("" +
                                         "<container>" +
                                         "  <component-implementation class='org.picocontainer.script.testmodel.DefaultWebServerConfig'/>" +
                                         "  <component-implementation key='org.picocontainer.script.testmodel.WebServer' class='org.picocontainer.script.testmodel.WebServerImpl'/>" +
                                         "</container>");

        PicoContainer pico = buildContainer(new XMLContainerBuilder(script, getClass().getClassLoader()),
                                            new DefaultPicoContainer(),
                                            "SOME_SCOPE");
        assertEquals(2, pico.getComponents().size());
        assertNotNull(pico.getComponent(DefaultWebServerConfig.class));
        assertNotNull(pico.getParent());

    }
}
