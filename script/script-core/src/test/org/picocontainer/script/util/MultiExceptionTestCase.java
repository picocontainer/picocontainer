package org.picocontainer.script.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Rimov
 *
 */
public class MultiExceptionTestCase {

    /**
     *
     */
    private static final String COLLECTING_MESSAGE = "Collecting Message";

    /**
     *
     */
    private static final String TEST_STRING_1 = "Test String 1";

    /**
     *
     */
    private static final String TEST_STRING_2 = "Test String 2";

    private final Throwable throwable1 = new Throwable(TEST_STRING_1);

    private final Throwable throwable2 = new IllegalArgumentException(TEST_STRING_2);

    private MultiException ex;

    private MultiException empty;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        empty = new MultiException();
        ex = new MultiException(COLLECTING_MESSAGE);
        ex.addException(throwable1);
        ex.addException(throwable2);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        ex = null;
        empty = null;
    }

    @Test
    public void testErrorCount() {
        assertEquals(2, ex.getErrorCount());
        assertEquals(0, empty.getErrorCount());
    }

    /**
     * Test method for {@link org.picocontainer.script.util.MultiException.core.datatypes.CollectingException#toString()}.
     */
    @Test
    public void testToString() {
        String emptyString = empty.toString();
        assertNotNull(emptyString);
        assertTrue(emptyString.contains("0"));

        String toString = ex.toString();
        assertNotNull(toString);
        assertTrue(toString.contains(Integer.toString(ex.getErrorCount())));
        assertTrue(toString.contains(COLLECTING_MESSAGE));
        assertTrue(toString.contains(TEST_STRING_1));
        assertTrue(toString.contains(TEST_STRING_2));
    }

    /**
     * Test method for {@link org.picocontainer.script.util.MultiException.core.datatypes.CollectingException#getMessage()}.
     */
    @Test
    public void testGetMessage() {
        String emptyMessage = empty.getMessage();
        assertNotNull(emptyMessage);
        assertTrue(emptyMessage.contains("null"));

        String message = ex.getMessage();
        assertNotNull(message);
        assertTrue(message.contains(this.getClass().getName()));  //Tests for stack trace.
        assertTrue(message.contains(COLLECTING_MESSAGE));
        assertTrue(message.contains(TEST_STRING_1));
        assertTrue(message.contains(TEST_STRING_2));
    }

    /**
     * Test method for {@link org.picocontainer.script.util.MultiException.core.datatypes.CollectingException#getMessageWithoutStackTrace()}.
     */
    @Test
    public void testGetMessageWithoutStackTrace() {
        String emptyMessage = empty.getMessageWithoutStackTrace();
        assertNotNull(emptyMessage);
        assertTrue(emptyMessage.contains("null"));

        String message = ex.getMessageWithoutStackTrace();
        assertNotNull(message);
        assertFalse(message.contains(this.getClass().getName()));  //Tests for stack trace.
        assertTrue(message.contains(COLLECTING_MESSAGE));
        assertTrue(message.contains(TEST_STRING_1));
        assertTrue(message.contains(TEST_STRING_2));
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
    	Throwable exceptionOne = new OutOfMemoryError();
    	Throwable exceptionTwo = new IllegalArgumentException();

    	MultiException exception = new MultiException();
    	exception.addException(exceptionOne).addException(exceptionTwo);

    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	ObjectOutputStream oos = new ObjectOutputStream(bos);
    	oos.writeObject(exception);
    	oos.close();

    	ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    	ObjectInputStream ois = new ObjectInputStream(bis);

    	MultiException deserialized = (MultiException) ois.readObject();
    	assertNotNull(deserialized);
    	//OutOfMemoryError shouldn't be serializable
    	assertEquals(2, deserialized.getErrorCount());

    }

}
