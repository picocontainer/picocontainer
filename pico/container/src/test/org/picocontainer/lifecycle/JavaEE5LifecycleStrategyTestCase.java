/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.lifecycle;

import static org.junit.Assert.assertEquals;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.junit.Before;
import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.monitors.NullComponentMonitor;

/**
 *
 * @author Mauro Talevi
 */
public class JavaEE5LifecycleStrategyTestCase {

    MutablePicoContainer pico;

    public static class ProPostAnnotationJava5Startable {

    	protected final StringBuilder sb;

        public ProPostAnnotationJava5Startable(final StringBuilder sb) {
            this.sb = sb;
        }

        @PostConstruct
        public void post() {
            sb.append("post()");
        }

        @PreDestroy
        public void pre() {
            sb.append("pre()");
        }

    }

    public static class ProPostAnnotationJava5Startable2 extends ProPostAnnotationJava5Startable {

        public ProPostAnnotationJava5Startable2(final StringBuilder sb) {
            super(sb);
        }

        @PostConstruct
        public void subPot() {
            sb.append("subPost()");
        }

        @PreDestroy
        public void subPre() {
            sb.append("subPre()");
        }

    }

    private LifecycleStrategy strategy;

    @Before
    public void setUp() {
        strategy = new JavaEE5LifecycleStrategy(new NullComponentMonitor());
        pico = new DefaultPicoContainer(new EmptyPicoContainer(), strategy, new Caching());
        pico.addComponent(StringBuilder.class);
        pico.addComponent(ProPostAnnotationJava5Startable.class);
    }

    @Test public void testStartable() {
        pico.start();
        assertEquals("post()", pico.getComponent(StringBuilder.class).toString());
    }

    @Test public void testStopHasNoMeaning() {
        pico.start();
        pico.stop();
        assertEquals("post()", pico.getComponent(StringBuilder.class).toString());
    }

    @Test public void testDispose() {
        pico.start();
        pico.dispose();
        assertEquals("post()pre()", pico.getComponent(StringBuilder.class).toString());
    }

    @Test public void testDisposeOfSubClass(){
        pico.removeComponent(ProPostAnnotationJava5Startable.class);
        pico.addComponent(ProPostAnnotationJava5Startable2.class);
        pico.start();
        pico.dispose();
        assertEquals("post()subPost()subPre()pre()", pico.getComponent(StringBuilder.class).toString());
    }
    @Test public void testSerializable() {
    }

    public static class ProPostAnnotationJava5Startable3 extends ProPostAnnotationJava5Startable {

        public ProPostAnnotationJava5Startable3(final StringBuilder sb) {
            super(sb);
        }

        @PostConstruct
        @Override
        public void post() {
            sb.append("subPost3()");
        }

        @PreDestroy
        public void subPre() {
            sb.append("subPre3()");
        }
    }

    @Test
    public void testLifecycleOfSubclassWhichOverrides(){
        pico.removeComponent(ProPostAnnotationJava5Startable.class);
        pico.addComponent(ProPostAnnotationJava5Startable3.class);
        pico.start();
        pico.dispose();
        assertEquals("subPost3()subPre3()pre()", pico.getComponent(StringBuilder.class).toString());
    }



    public static class PrivateMethodAnnotations {
    	private final StringBuilder sb;

		public PrivateMethodAnnotations(final StringBuilder sb) {
			this.sb = sb;

    	}

        @PostConstruct
        private void post() {
            sb.append("post()");
        }

        @PreDestroy
        private void pre() {
            sb.append("pre()");
        }
    }


    @Test
    public void testPrivateMethodInvocationWithJavaeelifecycle(){
        pico.removeComponent(ProPostAnnotationJava5Startable.class);
        pico.addComponent(PrivateMethodAnnotations.class);
        pico.start();
        pico.dispose();
        assertEquals("post()pre()", pico.getComponent(StringBuilder.class).toString());
    }

}