package org.picocontainer.jetty.groovy;

import static org.junit.Assert.assertEquals;

public class PlatformAssert {
    /**
     * Compares Strings, but ignores carriage returns since some platforms returned '\n' and
     * some '\r'.
     * @param expected
     * @param result
     */
    public static void assertSameExceptCarriageReturns(String expected, String result) {
    	if (expected == null && result == null) {
			return;
		}
		
		if ( (expected != null && result == null) || (expected == null && result != null) ) {
			//That way we get a nice error message.
			assertEquals(expected,result);    		
		}
		
    	StringBuilder expectedWithoutCarriageReturns = new StringBuilder(expected.length());
    	StringBuilder resultWithoutCarriageReturns = new StringBuilder(result.length());
    	
    	for (int i = 0; i < expected.length(); i++) {
    		char aChar = expected.charAt(i);
    		if (aChar == '\n' || aChar == '\r') {
    			continue;
    		}
    		expectedWithoutCarriageReturns.append(aChar);
    	}
    	
    	for (int i = 0; i < result.length(); i++) {
    		char aChar = result.charAt(i);
    		if (aChar == '\n' || aChar == '\r') {
    			continue;
    		}
    		resultWithoutCarriageReturns.append(aChar);
    	}
    	
    	assertEquals(expectedWithoutCarriageReturns.toString(), resultWithoutCarriageReturns.toString());
    }


}
