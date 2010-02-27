package org.picocontainer;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import org.junit.Test;

public class CharacteristicsTestCase  {

    @Test(expected=UnsupportedOperationException.class)    
    public void testCharacteristicsAreImmutable() {
        assertNotNull(Characteristics.CDI.toString());
        Characteristics.CDI.remove("injection");
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testSetPropertyIsNotAllowed() {
        assertNotNull(Characteristics.CDI.toString());
        Characteristics.CDI.setProperty("injection","true");    	
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testHashmapPutIsNotAllowed() {
        assertNotNull(Characteristics.CDI.toString());
        Characteristics.CDI.put("injection","true");    	
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testHashMapPutAllIsNotAllowed() {
        assertNotNull(Characteristics.CDI.toString());
        Characteristics.CDI.putAll(new HashMap<String,String>());    	
    }

}
