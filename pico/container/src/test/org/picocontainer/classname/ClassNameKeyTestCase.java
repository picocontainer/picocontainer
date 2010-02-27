package org.picocontainer.classname;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.picocontainer.classname.ClassName;

/**
 * 
 * @author Mauro Talevi
 */
public class ClassNameKeyTestCase {

    @Test public void testGetClassName(){
        String className = ClassName.class.getName();
        ClassName key = new ClassName(className);
        assertEquals(className, key.toString());
    }
}
