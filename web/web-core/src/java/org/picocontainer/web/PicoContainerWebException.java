package org.picocontainer.web;

/**
 * Base class runtime exception for the Pico web version.
 */
@SuppressWarnings("serial")
public class PicoContainerWebException extends RuntimeException{
	
    public PicoContainerWebException(String s) {
        super(s);
    }
}
