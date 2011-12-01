/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.gems;

import org.picocontainer.Behavior;
import org.picocontainer.gems.adapters.ThreadLocalizing;
import org.picocontainer.gems.behaviors.AsmImplementationHiding;
import org.picocontainer.gems.behaviors.HotSwapping;
import org.picocontainer.gems.behaviors.Pooling;
import org.picocontainer.gems.jmx.JMXExposing;
import org.picocontainer.gems.monitors.CommonsLoggingComponentMonitor;
import org.picocontainer.gems.monitors.Log4JComponentMonitor;
import org.picocontainer.gems.monitors.Slf4jComponentMonitor;
import org.picocontainer.ComponentMonitor;


/**
 * Provides a series of factory methods to allow an &quot;index&quot; of the capabilities that you may find in 
 * PicoContainer-Gems.
 * @author Paul Hammant
 */
public class PicoGemsBuilder {
    
    /**
	 * Creates an {@link org.picocontainer.gems.behaviors.AsmImplementationHiding AsmImplementationHiding} behavior factory.
	 * @return a new AsmImplementationHiding() instance.
	 * @since PicoContainer-Gems 2.4
     */
    public static Behavior ASM_IMPL_HIDING() {
        return new AsmImplementationHiding();    	
    }
    
    /**
     * Creates a {@link org.picocontainer.gems.behaviors.HotSwapping HotSwapping} behavior factory.
     * @return
     */
    public static Behavior HOT_SWAPPING() {
    	return new HotSwapping();
    }
    
    /**
     * Only uses the system default mbean server.  See {@link org.picocontainer.gems.jmx.JMXExposing JMXExposing} for other 
     * constructors that give you more flexibility in exposing your objects.
     * @return JMX Exposing behavior factory.
     */
    public static Behavior JMX() {
    	return new JMXExposing();
    }

    /**
     * Creates a thread localizing adapter factory.
     * @return
     */
    public static Behavior THREAD_LOCAL() {
    	return new ThreadLocalizing();
    }
    
    /**
     * Creates an instance pooling adapter factory.
     * @return 
     */
    public static Behavior POOLING() {
    	return new Pooling();
    }
    
    /**
     * Creates a log4j component monitor instance.  You will need Log4j in your classpath for this method to work. 
     * @return Log4j-based component monitor.
     */
    public static ComponentMonitor LOG4J() {
        return new Log4JComponentMonitor();
    }
    
    /**
     * Creates a slf4j component monitor instance.  You will need SLF4j in your classpath for this method to work
     * properly.
     * @return SLF4j-based component monitor.
     */
    public static ComponentMonitor SLF4J() {
    	return new Slf4jComponentMonitor();
    }
    
    /**
     * Creates a Commons-Logging based component monitor instance.  You will need Apache Commons-Logging in your classpath
     * for this method to work properly.
     * @return Commons-Logging based component monitor.
     */
    public static ComponentMonitor COMMONS_LOGGING() {
    	return new CommonsLoggingComponentMonitor();
    }
    
    
    

}
