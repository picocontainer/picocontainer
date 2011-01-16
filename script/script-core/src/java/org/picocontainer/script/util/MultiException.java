package org.picocontainer.script.util;


import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Constructs an exception that can be used to collect multiple exceptions and
 * displays then all in the error message.  This is mainly useful in a batch transaction
 * environment where you wish to perform multiple operations even if one operation failed.
 * 
 * @author Michael Rimov
 */
public class MultiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ArrayList< ExceptionPair > errors = new ArrayList< ExceptionPair >();

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
    public MultiException addException(final String context, final Throwable error) {
    	errors.add(new ExceptionPair(context != null ? context : "", error));
    	return this;
    }
    
    public MultiException addException(final Throwable error) {
    	return this.addException("", error);
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
        for (ExceptionPair exceptionPair : errors) {
            writer.print("Exception #" + i + ": " + exceptionPair.subOperation + "\n\t");
            writer.println(exceptionPair.exception.getMessage());
            exceptionPair.exception.printStackTrace(writer);
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
            final ExceptionPair error = errors.get(i);

            writer.print("Exception #" + i + ": " + error.subOperation);
            writer.println(error.exception.getMessage());
        }

        writer.close();
        return stringWriter.toString();
    }

    @Override
    public String toString() {
        return "CollectingException with error list of size: " + errors.size() + "\n Errors: " + getMessage();
    }
    
    public List<Throwable> getNestedExceptions() {
    	ArrayList<Throwable> returnErrors = new ArrayList<Throwable>(errors.size());
    	for (ExceptionPair eachPair : errors) {
    		returnErrors.add(eachPair.exception);
    	}
    	return Collections.unmodifiableList(returnErrors);
    }

    @SuppressWarnings("serial")
	private static class ExceptionPair implements Serializable{
    	
    	final String subOperation;
    	final Throwable exception;
    	
    	ExceptionPair(String subOperation, Throwable exception) {
			this.subOperation = subOperation;
			this.exception = exception;
    		
    	}
    	
    }
}
