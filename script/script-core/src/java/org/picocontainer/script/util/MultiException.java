package org.picocontainer.script.util;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Constructs an exception that can be used to collect multiple exceptions and
 * displays then all in the error message.  This is mainly useful in a batch transaction
 * environment where you wish to perform multiple operations even if one operation failed.
 * 
 * @author Michael Rimov
 */
public class MultiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ArrayList< Throwable > errors = new ArrayList< Throwable >();

    public MultiException() {
        super();
    }

    public MultiException(final String operationDescription) {
        super(operationDescription);
    }

    /**
     * Adds an exception
     * 
     * @param error Throwable
     */
    public void addException(final Throwable... error) {
    	assert error != null && error.length > 0;
        errors.addAll(Arrays.asList(error));
    }

    /**
     * Retrieves the number of exceptions that have been added.
     * 
     * @return int
     */
    public int getErrorCount() {
        return errors.size();
    }

    /**
     * Returns a message with all collected exceptions and their appropriate
     * stack traces.
     * 
     * @return Message with all exceptions and their stack traces printed.
     */
    @Override
    public String getMessage() {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter);

        writer.println("The following errors occurred while performing the operation: " + super.getMessage());

        int i = 1;
        for (Throwable exception : errors) {
            writer.print("Exception #" + i + ": ");
            writer.println(exception.getMessage());
            exception.printStackTrace(writer);
            i++;
        }


        writer.close();
        return stringWriter.toString();
    }

    /**
     * Retrieves a list of all exceptions thrown without printing the stack
     * traces as well.
     * 
     * @return Message with all exceptions printed.
     */
    public String getMessageWithoutStackTrace() {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter);

        writer.println("The following errors occurred while performing the operation: " + super.getMessage());

        for (int i = 0; i < errors.size(); i++) {
            final Throwable exception = errors.get(i);

            writer.print("Exception #" + i + ": ");
            writer.println(exception.getMessage());
        }

        writer.close();
        return stringWriter.toString();
    }

    @Override
    public String toString() {
        return "CollectingException with error list of size: " + errors.size() + "\n Errors: " + getMessage();
    }

}
