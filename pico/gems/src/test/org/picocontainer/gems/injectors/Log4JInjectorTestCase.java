package org.picocontainer.gems.injectors;

import com.thoughtworks.xstream.XStream;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;

public class Log4JInjectorTestCase {

    public static class Foo {
        private Logger logger;
        public Foo(final Logger logger) {
            this.logger = logger;
        }
    }

    @Test
    public void thatItInjectsTheApplicableInstance() throws NoSuchFieldException, IllegalAccessException {

        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(new Log4JInjector());
        pico.addComponent(Foo.class);

        Foo foo = pico.getComponent(Foo.class);

        assertNotNull(foo);
        assertNotNull(foo.logger);

        assertTrue(new XStream().toXML(foo.logger).contains("<name>"+Foo.class.getName()+"</name>"));

    }



}