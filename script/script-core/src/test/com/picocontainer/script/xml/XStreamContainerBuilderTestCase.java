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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;
import com.picocontainer.script.AbstractScriptedContainerBuilderTestCase;
import com.picocontainer.script.testmodel.DefaultWebServerConfig;
import com.picocontainer.script.testmodel.ThingThatTakesParamsInConstructor;
import com.picocontainer.script.testmodel.WebServerImpl;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.NameBinding;
import com.picocontainer.PicoBuilder;
import com.picocontainer.PicoContainer;
import com.picocontainer.behaviors.AbstractBehavior;
import com.picocontainer.behaviors.Caching;
import com.picocontainer.behaviors.Locking;

public class XStreamContainerBuilderTestCase extends AbstractScriptedContainerBuilderTestCase {

    @Test public void testContainerBuilding() {

        Reader script = new StringReader("" +
                "<container>" +
                "    <instance key='foo'>" +
                "    	<string>foo bar</string>" +
                "    </instance>" +
                "    <instance key='bar'>" +
                "    	<int>239</int>" +
                "    </instance>" +
                "    <instance>" +
                "    	<com.picocontainer.script.testmodel.DefaultWebServerConfig>" +
                " 			<port>555</port>" +
                "    	</com.picocontainer.script.testmodel.DefaultWebServerConfig>" +
                "    </instance>" +
                "	 <implementation class='com.picocontainer.script.testmodel.WebServerImpl'>" +
                "		<dependency class='com.picocontainer.script.testmodel.DefaultWebServerConfig'/>" +
                "	 </implementation>" +
                "	 <implementation key='konstantin needs beer' class='com.picocontainer.script.testmodel.ThingThatTakesParamsInConstructor'>" +
                "		<constant>" +
                "			<string>it's really late</string>" +
                "		</constant>" +
                "		<constant>" +
                "			<int>239</int>" +
                "		</constant>" +
                "	 </implementation>" +
                "</container>");

        PicoContainer pico = buildContainer(new XStreamContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        assertEquals(5, pico.getComponents().size());
        assertEquals("foo bar", pico.getComponent("foo"));
        assertEquals(239, pico.getComponent("bar"));
        assertEquals(555, pico.getComponent(DefaultWebServerConfig.class).getPort());

        assertNotNull(pico.getComponent(WebServerImpl.class));
        assertNotNull(pico.getComponent(ThingThatTakesParamsInConstructor.class));
        final Object o = pico.getComponent("konstantin needs beer");
        final ThingThatTakesParamsInConstructor o2 = pico.getComponent(ThingThatTakesParamsInConstructor.class);
        assertSame(o, o2);
        assertEquals("it's really late239", ((ThingThatTakesParamsInConstructor) pico.getComponent("konstantin needs beer")).getValue());
    }

    @Test public void testComponentAdapterInjection() throws Throwable {
        Reader script = new StringReader("<container>" +
                "<adapter key='testAdapter'>" +
                "<instance key='firstString'>" +
                "<string>bla bla</string>" +
                "</instance>" +
                "<instance key='secondString' >" +
                "<string>glarch</string>" +
                "</instance>" +
                "<instance key='justInt'>" +
                "<int>777</int>" +
                "</instance>" +
                "<implementation key='testAdapter' class='com.picocontainer.script.xml.TestAdapter'>" +
                "<dependency key='firstString'/>" +
                "<dependency key='justInt'/>" +
                "<dependency key='secondString'/>" +
                "</implementation>" +
                "</adapter>" +
                "</container>");

        PicoContainer pico = buildContainer(new XStreamContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        Caching.Cached<TestAdapter> ca = (Caching.Cached<TestAdapter>) pico.getComponentAdapter(TestAdapter.class, (NameBinding) null);

        assertNotNull(ca.getDelegate());
    }

    @Test public void testInstantiationOfComponentsWithInstancesOfSameComponent() throws Exception {
        Reader script = new StringReader("" +
                "<container>" +
                "  <instance key='bean1'>" +
                "	<com.picocontainer.script.xml.TestBean>" +
                "		<foo>10</foo>" +
                "		<bar>hello1</bar>" +
                "	</com.picocontainer.script.xml.TestBean>" +
                "  </instance>" +
                "  <instance key='bean2'>" +
                "	<com.picocontainer.script.xml.TestBean>" +
                "		<foo>10</foo>" +
                "		<bar>hello2</bar>" +
                "	</com.picocontainer.script.xml.TestBean>" +
                "  </instance>" +
                "  <implementation class='com.picocontainer.script.xml.TestBeanComposer'>" +
                "		<dependency key='bean1'/>" +
                "		<dependency key='bean2'/>" +
                "  </implementation>" +
                "</container>");
        PicoContainer pico = buildContainer(new XStreamContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        assertNotNull(pico.getComponent(TestBeanComposer.class));
        TestBeanComposer composer = pico.getComponent(TestBeanComposer.class);
        assertEquals("bean1", "hello1", composer.getBean1().getBar());
        assertEquals("bean2", "hello2", composer.getBean2().getBar());
    }

    // do not know how to extract parameters off adapter....
    @Test public void testThatDependencyUsesClassAsKey() {
        Reader script = new StringReader("" +
        "<container>" +
        "   <implementation class='java.lang.String'/>" +
        "   <implementation key='foo' class='com.picocontainer.script.xml.TestBean'>" +
        "       <dependency class='java.lang.String'/>" +
        "   </implementation>" +
        "</container>"
       );

        PicoContainer pico = buildContainer(new XStreamContainerBuilder(script, getClass().getClassLoader()), null,
                null);
        ComponentAdapter<?> componentAdapter = pico.getComponentAdapter("foo");
        AbstractBehavior.AbstractChangedBehavior<?> adapter = (AbstractBehavior.AbstractChangedBehavior<?>) componentAdapter;
        assertNotNull(adapter);
    }


    @Test public void testDefaultContsructorRegistration() throws Exception {

        Reader script = new StringReader(
        "<container>" +
        "   <implementation class='com.picocontainer.script.xml.TestBean' constructor='default'/>" +
        "   <instance>" +
        "       <string>blurge</string>" +
        "   </instance>" +
        "</container>"
        );


        PicoContainer pico = buildContainer(new XStreamContainerBuilder(script, getClass().getClassLoader()), null,null);
        TestBean bean = pico.getComponent(TestBean.class);
        assertEquals("default",bean.getConstructorCalled());
    }


    @Test
    public void testInheritanceOfBehaviorsFromParentContainer() {
    	Reader comparison = new StringReader("" +
        		"<container inheritBehaviors=\"false\">\n" +
                "  <implementation class='com.picocontainer.script.testmodel.DefaultWebServerConfig'/>" +
                "</container>"
        	);

    	MutablePicoContainer parent = new PicoBuilder().withLocking().build();
    	PicoContainer comparisonPico = buildContainer(new XStreamContainerBuilder(comparison, getClass().getClassLoader()), parent, "SOME_SCOPE");
    	//Verify not locking by default
    	//assertTrue(comparisonPico.getComponent(DefaultWebServerConfig.class) != comparisonPico.getComponent(DefaultWebServerConfig.class));
    	assertNull(comparisonPico.getComponentAdapter(DefaultWebServerConfig.class).findAdapterOfType(Locking.Locked.class));

    	//Verify parent caching propagates to child.
    	Reader script = new StringReader("" +
    		"<container inheritBehaviors=\"true\">\n" +
            "  <implementation class='com.picocontainer.script.testmodel.DefaultWebServerConfig'/>" +
            "</container>"
    	);

    	parent = new PicoBuilder().withLocking().build();
    	PicoContainer pico = buildContainer(new XStreamContainerBuilder(script, getClass().getClassLoader()), parent, "SOME_SCOPE");

    	assertNotNull(pico.getComponentAdapter(DefaultWebServerConfig.class).findAdapterOfType(Locking.Locked.class));
    }

    @Test
    public void testParameterZero() {
    	Reader script = new StringReader("" +
                "<container>\n" +
	    			"<implementation key='java.util.List' class='java.util.ArrayList'> \n" +
	    			"    <parameter-zero/>\n" +
	    			"</implementation> \n" +
	    			"<implementation key='java.util.Set' class='java.util.HashSet'> \n" +
	    			"    <parameter-zero/>\n" +
	    			"</implementation>\n" +
                "</container>\n"
    	);
    	PicoContainer pico = buildContainer(new XStreamContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
    	assertNotNull(pico.getComponent(java.util.List.class));
    	assertNotNull(pico.getComponent(java.util.Set.class));
    }


}

