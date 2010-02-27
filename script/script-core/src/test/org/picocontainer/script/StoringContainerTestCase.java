package org.picocontainer.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.behaviors.Storing;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.script.testmodel.FredImpl;
import org.picocontainer.script.testmodel.ThingThatTakesParamsInConstructor;
import org.picocontainer.script.testmodel.Wilma;
import org.picocontainer.script.testmodel.WilmaImpl;
import org.picocontainer.script.xml.XMLContainerBuilder;

/**
 * Test case to prove that the DefaultContainerRecorder can be replaced by use of Storing behaviours.
 * 
 * @author Konstantin Pribluda
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 */
public class StoringContainerTestCase {
    
    @Test public void testInvocationsCanBeRecordedAndReplayedOnADifferentContainerInstance() throws Exception {

        // This test case is not testing Storing. Its just testing that a Caching parent does so.
        DefaultPicoContainer parent = new DefaultPicoContainer(new Caching());
        parent.addComponent("fruit", "apple");
        parent.addComponent("int", 239);
        parent.addComponent("thing",
                ThingThatTakesParamsInConstructor.class,
                ComponentParameter.DEFAULT,
                ComponentParameter.DEFAULT);

        Storing storing1 = new Storing();
        DefaultPicoContainer child1 = new DefaultPicoContainer(storing1, parent);
        assertEquals("store should be empty", 0, storing1.getCacheSize());
        Object a1 = child1.getComponent("fruit");
        assertEquals("store should still be empty: its not used", 0, storing1.getCacheSize());
        ThingThatTakesParamsInConstructor a2 = (ThingThatTakesParamsInConstructor) child1.getComponent("thing");
        assertEquals("apple", a1);
        assertEquals("apple239", a2.getValue());

        // test that we can replay once more
        Storing storing2 = new Storing();
        DefaultPicoContainer child2 = new DefaultPicoContainer(storing2, parent);
        assertEquals("store should be empty", 0, storing2.getCacheSize());
        Object b1 = child2.getComponent("fruit");
        assertEquals("store should still be empty: its not used", 0, storing2.getCacheSize());
        ThingThatTakesParamsInConstructor b2 = (ThingThatTakesParamsInConstructor) child2.getComponent("thing");
        assertEquals("apple", b1);
        assertEquals("apple239", b2.getValue());

        assertSame("cache of 'recording' parent container should be caching", a1,b1); 
        assertSame("cache of 'recording' parent container should be caching", a2,b2);
    }

    @Test public void testRecorderWorksAfterSerialization() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {
        DefaultPicoContainer recorded = new DefaultPicoContainer(new Caching());
        recorded.addComponent("fruit", "apple");
        DefaultPicoContainer replayed = new DefaultPicoContainer(new Storing(), recorded);
        DefaultPicoContainer serializedReplayed = (DefaultPicoContainer) serializeAndDeserialize(replayed);
        assertEquals("apple", serializedReplayed.getComponent("fruit"));
    }

    private Object serializeAndDeserialize(Object o) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(o);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));

        return ois.readObject();
    }


    @Test public void scriptedPopulationOfContainerHierarchy() {

        MutablePicoContainer parent = new DefaultPicoContainer(new Caching());

        // parent has nothing populated in it
        DefaultPicoContainer child = new DefaultPicoContainer(new Storing(), parent);

        new XMLContainerBuilder(new StringReader(""
                + "<container>"
                + "  <component-implementation key='wilma' class='"+WilmaImpl.class.getName()+"'/>"
                + "</container>"
                ), Thread.currentThread().getContextClassLoader()).populateContainer(child);

        assertNull(child.getComponent("fred"));
        assertNotNull(child.getComponent("wilma"));

        DefaultPicoContainer grandchild = new DefaultPicoContainer(new Storing(), child);

        new XMLContainerBuilder(new StringReader(
                  "<container>"
                + "  <component-implementation key='fred' class='"+FredImpl.class.getName()+"'>"
                + "     <parameter key='wilma'/>"
                + "  </component-implementation>"
                + "</container>"
                ), Thread.currentThread().getContextClassLoader()).populateContainer(grandchild);

        assertNotNull(grandchild.getComponent("fred"));
        assertNotNull(grandchild.getComponent("wilma"));

        FredImpl fred = (FredImpl)grandchild.getComponent("fred");
        Wilma wilma = (Wilma)grandchild.getComponent("wilma");

        assertSame(wilma, fred.wilma());
    }

}
