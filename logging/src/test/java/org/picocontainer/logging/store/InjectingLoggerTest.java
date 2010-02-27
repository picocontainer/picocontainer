package org.picocontainer.logging.store;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.picocontainer.logging.Logger;
import org.picocontainer.logging.loggers.NullLogger;

/**
 * 
 * @author Paul Hammant
 */
public class InjectingLoggerTest {

    @Test
    public void canInjectLogger() {

        ComponentWithInjectedLogger c = new ComponentWithInjectedLogger(new NullLogger());
        assertNotNull(c.getLogger());
    }

    @Test
    public void canInjectStore() {

        ComponentWithInjectedStore c = new ComponentWithInjectedStore(new LoggerStore() {
            public Logger getLogger() {
                return new NullLogger();
            }

            public Logger getLogger(String categoryName) {
                return getLogger();
            }

            public void close() {
            }
        });
        assertNotNull(c.getLogger());
    }


    public static class ComponentWithInjectedLogger {
        private final Logger logger;

        public ComponentWithInjectedLogger(Logger logger) {
            this.logger = logger;
        }
        
        public Logger getLogger(){
            return logger;
        }
    }

    public static class ComponentWithInjectedStore {
        private final Logger logger;

        public ComponentWithInjectedStore(LoggerStore loggerStore) {
            this.logger = loggerStore.getLogger("someLogger");
        }

        public Logger getLogger(){
            return logger;
        }
    }

}
