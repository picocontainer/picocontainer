/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script;

import org.picocontainer.PicoException;

/**
 * Thrown when a given script type extension has no corresponding builder. The
 * message will also indicate all supported builders.
 * 
 * @author Michael Rimov
 */
@SuppressWarnings("serial")
public class UnsupportedScriptTypeException extends PicoException {

    private final String specifiedFileExtension;

    private final String[] allSupportedFileExtensions;

    public UnsupportedScriptTypeException(String specifiedFileExtension, String[] allSupportedFileExtensions) {
        super();
        this.specifiedFileExtension = specifiedFileExtension;
        this.allSupportedFileExtensions = allSupportedFileExtensions;
    }

    /**
     * Builds the exception message from the fields
     * 
     * @return The exception message
     */
    private String buildExceptionMessage() {
        StringBuffer message = new StringBuffer(48);
        message.append("Unsupported file extension '");
        message.append(specifiedFileExtension);
        message.append("'.  Supported extensions are: [");

        if (allSupportedFileExtensions != null) {
            boolean needPipe = false;
            for (String allSupportedFileExtension : allSupportedFileExtensions) {
                if (needPipe) {
                    message.append("|");
                } else {
                    needPipe = true;
                }

                message.append(allSupportedFileExtension);
            }

            message.append("].");
        } else {
            message.append(" null ");
        }

        return message.toString();
    }

    @Override
    public String getMessage() {
        return buildExceptionMessage();
    }

    public String[] getSystemSupportedExtensions() {
        return allSupportedFileExtensions;
    }

    public String getRequestedExtension() {
        return specifiedFileExtension;
    }

}
