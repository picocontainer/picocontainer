/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script.groovy;

import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.ProcessingUnit;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.picocontainer.script.ScriptedPicoContainerMarkupException;

/**
 * Thrown when a groovy compilation error occurs
 * 
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public final class GroovyCompilationException extends ScriptedPicoContainerMarkupException {
    private final CompilationFailedException compilationFailedException;

    public GroovyCompilationException(String message, CompilationFailedException e) {
        super(message,e);
        this.compilationFailedException = e;
    }

    public String getMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.getMessage()).append("\n");
        List<?> errors = getErrors(compilationFailedException);
        for (Object error : errors) {
            if (error instanceof ExceptionMessage) {
                ExceptionMessage em = (ExceptionMessage) error;
                sb.append(em.getCause().getMessage()).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Extract errors from groovy exception, coding defensively against
     * possible null values.
     * @param e the CompilationFailedException
     * @return A List of errors
     */
    private List<?> getErrors(CompilationFailedException e) {
        ProcessingUnit unit = e.getUnit();
        if (unit != null) {
            ErrorCollector collector = unit.getErrorCollector();
            if (collector != null) {
                List<?> errors = collector.getErrors();
                if (errors != null) {
                    return errors;
                }
            }
        }
        return Collections.EMPTY_LIST;
    }
}
