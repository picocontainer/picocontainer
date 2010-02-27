package org.picocontainer.containers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Properties;
import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.Characteristics;

import javax.inject.Named;

/**
 * test that properties container works properly
 * @author Konstantin Pribluda
 */
public class PropertiesPicoContainerTestCase {
	/**
	 * all properties specified in constructor shall be
	 * placed into container as strings
	 *
	 */
	@Test public void testThatAllPropertiesAreAdded() {
		Properties properties = new Properties();
		
		properties.put("foo","bar");
		properties.put("blurge","bang");
		
		
		PropertiesPicoContainer container = new PropertiesPicoContainer(properties);
		assertEquals("bar",container.getComponent("foo"));
		assertEquals("bang",container.getComponent("blurge"));
	}
	
	/**
	 * inquiry shall be delegated to parent container
	 */
	@Test public void testThatParentDelegationWorks() {
		DefaultPicoContainer parent = new DefaultPicoContainer();
		String stored = new String("glam");
		parent.addComponent("glam",stored);
		
		PropertiesPicoContainer contaienr = new PropertiesPicoContainer(new Properties(),parent);
		
		assertSame(stored,contaienr.getComponent("glam"));
	}


    @Test public void thatParanamerBehavesForASpecialCase() {

       Properties properties = new Properties();
       properties.put("portNumber", 1);
       properties.put("hostName", "string");
       properties.put("agentName", "agent0");
       DefaultPicoContainer container = new DefaultPicoContainer(new PropertiesPicoContainer(properties));
       container.as(Characteristics.USE_NAMES).addComponent(Dependant.class);
       container.as(Characteristics.USE_NAMES).addComponent(Dependency.class);
       Dependant dependant = (Dependant) container.getComponent(Dependant.class);
       assertEquals(1, dependant.pn);
       assertEquals("string", dependant.hn);

   }

    public static class Dependency {
        private final String name;

        public Dependency(final String agentName) {
            this.name = agentName;
        }

        public String toString() {
            return name;
        }
    }

    public static class Dependant {
        final int pn;
        final String hn;
        final Dependency dependency;

        public Dependant(final String hostName, final int portNumber, final Dependency dependency) {
            this.pn = portNumber;
            this.hn = hostName;
            this.dependency = dependency;
        }

        public String toString() {
            return "Number: " + pn + " String: " + hn + " Dependency: " + dependency;
        }
    }

    @Test
    public void thatParanamerHonorsNamedAnnotationFromJSR330() {

       Properties properties = new Properties();
       properties.put("portNumber", "1");
       properties.put("hostName", "string");
       properties.put("agentName", "agent0");
       DefaultPicoContainer container = new DefaultPicoContainer(new PropertiesPicoContainer(properties));
       container.as(Characteristics.USE_NAMES).addComponent(Dependant2.class);
       container.as(Characteristics.USE_NAMES).addComponent(Dependency.class);
       Dependant2 dependant = (Dependant2) container.getComponent(Dependant2.class);
       assertEquals(1, dependant.pn);
       assertEquals("string", dependant.hn);
   }


    public static class Dependant2 extends Dependant {
        public Dependant2(@Named("hostName") String hn, @Named("portNumber") String pn, Dependency d) {
            super(hn, Integer.parseInt(pn), d);
        }
    }

    @Test public void testRepresentationOfContainerTree() {
        Properties properties = new Properties();
        properties.put("portNumber", 1);
        properties.put("hostName", "string");
        properties.put("agentName", "agent0");

        PropertiesPicoContainer parent = new PropertiesPicoContainer(properties);
        parent.setName("parent");
        DefaultPicoContainer child = new DefaultPicoContainer(parent);
        child.setName("child");
		child.addComponent("hello", "goodbye");
        child.addComponent("bonjour", "aurevior");
        assertEquals("child:2<I<D<parent:3<|", child.toString());
    }


}
