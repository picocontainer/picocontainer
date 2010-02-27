package org.picocontainer.script;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Exception Tests.
 * @author Michael Rimov
 */
public class UnsupportedScriptTypeExceptionTestCase {
    private UnsupportedScriptTypeException unsupportedScriptTypeException = null;

    private final String[] supportedParams = new String[]{".groovy",".py",".xml"};

    @Before public void setUp() throws Exception {
        unsupportedScriptTypeException = new UnsupportedScriptTypeException("test.txt", supportedParams);
    }

    @After public void tearDown() throws Exception {
        unsupportedScriptTypeException = null;
    }

    @Test public void testGetMessage() {
        String actualReturn = unsupportedScriptTypeException.getMessage();
        assertNotNull(actualReturn);
        assertTrue(actualReturn.indexOf(".groovy") > -1);
        assertTrue(actualReturn.indexOf(".py") > -1) ;
        assertTrue(actualReturn.indexOf(".xml") > -1);
        assertTrue(actualReturn.indexOf("test.txt") > -1);
    }

    @Test public void testGetRequestedExtension() {
        String expectedReturn = "test.txt";
        String actualReturn = unsupportedScriptTypeException.getRequestedExtension();
        assertEquals("return value", expectedReturn, actualReturn);
    }

    @Test public void testGetSystemSupportedExtensions() {
        String[] expectedReturn = supportedParams;
        String[] actualReturn = unsupportedScriptTypeException.getSystemSupportedExtensions();
        assertEquals("return value", asList(expectedReturn), asList(actualReturn));
    }


}
