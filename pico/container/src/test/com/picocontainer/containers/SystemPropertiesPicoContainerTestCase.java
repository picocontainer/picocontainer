package com.picocontainer.containers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.containers.SystemPropertiesPicoContainer;

/**
 * test capabilities of system properties providing container.
 * @author Konstantin Pribluda
 *
 */
public class SystemPropertiesPicoContainerTestCase {


	/**
	 * all the content of system properties shall be made available
	 *  through this contaienr.
	 */
	@Test public void testThatAllSystemPropertiesAreCopied() {
		SystemPropertiesPicoContainer container = new SystemPropertiesPicoContainer();
		for(Object key: System.getProperties().keySet()) {
			assertSame(System.getProperties().get(key),container.getComponent(key));
		}
	}

    @Test public void testRepresentationOfContainerTree() {
        SystemPropertiesPicoContainer parent = new SystemPropertiesPicoContainer();
        parent.setName("parent");
        DefaultPicoContainer child = new DefaultPicoContainer(parent);
        child.setName("child");
		child.addComponent("hello", "goodbye");
        child.addComponent("bonjour", "aurevior");
        int num = System.getProperties().size();
        assertEquals("child:2<[Immutable]:[SysProps]:parent:"+num+"<|", child.toString());
    }


}
