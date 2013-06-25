package com.picocontainer.gems.injectors;

import static junit.framework.Assert.assertNotNull;

import java.util.logging.Logger;

import org.junit.Test;

import com.picocontainer.DefaultPicoContainer;

public class JavaLoggingInjectorTestCase {

    public static class Foo {
        private final Logger logger;
        public Foo(final Logger logger) {
            this.logger = logger;
        }
    }


    @Test
    public void thatItInjectsTheApplicableInstance() {

        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(new JavaLoggingInjector());
        pico.addComponent(Foo.class);

        Foo foo = pico.getComponent(Foo.class);

        assertNotNull(foo);
        assertNotNull(foo.logger);

    }



}