/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.behaviors;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JLabel;

import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentFactory;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.injectors.AdaptingInjection;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.tck.AbstractComponentFactoryTest;
import org.picocontainer.testmodel.SimpleTouchable;
import org.picocontainer.testmodel.Touchable;


/**
 * @author Aslak Helles&oslash;y
 * @author Mirko Novakovic
 */
public class PropertyApplyingTestCase extends AbstractComponentFactoryTest {

    public static class Foo {
        public String message;

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class Failing {
        public void setMessage(String message) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Class that contains all types of Java primitives, to test if they are
     * set correctly.
     *
     * @author Mirko Novakovic
     */
    public static class Primitives {
        public byte byte_;
        public short short_;
        public int int_;
        public long long_;
        public float float_;
        public double double_;
        public boolean boolean_;
        public char char_;
        public File file_;
        public URL url_;
        public Class class_;
        public String string_;

        public void setClass_(Class class_) {
            this.class_ = class_;
        }

        public void setString_(String string_) {
            this.string_ = string_;
        }

        public void setBoolean_(boolean boolean_) {
            this.boolean_ = boolean_;
        }

        public void setByte_(byte byte_) {
            this.byte_ = byte_;
        }

        public void setChar_(char char_) {
            this.char_ = char_;
        }

        public void setDouble_(double double_) {
            this.double_ = double_;
        }

        public void setFloat_(float float_) {
            this.float_ = float_;
        }

        public void setInt_(int int_) {
            this.int_ = int_;
        }

        public void setLong_(long long_) {
            this.long_ = long_;
        }

        public void setShort_(short short_) {
            this.short_ = short_;
        }

        public void setFile_(File file_) {
            this.file_ = file_;
        }

        public void setUrl_(URL url_) {
            this.url_ = url_;
        }
    }

    public static class A {
        private B b;

        public void setB(B b) {
            this.b = b;
        }
    }

    public static class B {
    }

    @Test public void testSetProperties() {
        ComponentAdapter adapter = createAdapterCallingSetMessage(Foo.class);
        Foo foo = (Foo)adapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);
        assertNotNull(foo);
        assertEquals("hello", foo.message);
    }

    @Test public void testFailingSetter() {
        ComponentAdapter adapter = createAdapterCallingSetMessage(Failing.class);
        try {
            adapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);
            fail();
        } catch (PicoCompositionException e) {
        }
    }

    protected ComponentFactory createComponentFactory() {
        return new PropertyApplying().wrap(new AdaptingInjection());
    }

    @Test public void testPropertiesSetAfterAdapterCreationShouldBeTakenIntoAccount() {
        PropertyApplying factory = (PropertyApplying)createComponentFactory();

        PropertyApplying.PropertyApplicator adapter =
            (PropertyApplying.PropertyApplicator)factory.createComponentAdapter(new NullComponentMonitor(),
                                                                     new NullLifecycleStrategy(),
                                                                     new Properties(Characteristics
                                                                         .CDI),
                                                                     "foo",
                                                                     Foo.class,
                                                                     null, null, null);

        Map properties = new HashMap();
        properties.put("message", "hello");
        adapter.setProperties(properties);

        Foo foo = (Foo)adapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);

        assertEquals("hello", foo.message);
    }

    @Test public void testPropertySetAfterAdapterCreationShouldBeTakenIntoAccount() {
        PropertyApplying factory = (PropertyApplying)createComponentFactory();

        PropertyApplying.PropertyApplicator adapter =
            (PropertyApplying.PropertyApplicator)factory.createComponentAdapter(new NullComponentMonitor(),
                                                                     new NullLifecycleStrategy(),
                                                                     new Properties(Characteristics
                                                                         .CDI),
                                                                     "foo",
                                                                     Foo.class,
                                                                     null, null, null);
        adapter.setProperty("message", "hello");

        Foo foo = (Foo)adapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);

        assertEquals("hello", foo.message);
    }


    @Test public void testPropertiesTidiedUpAfterPicoUsage() {
        DefaultPicoContainer pico = new DefaultPicoContainer(createComponentFactory());
        pico.as(Characteristics.PROPERTY_APPLYING).addComponent("foo", Foo.class);
        Foo foo = (Foo) pico.getComponent("foo");
    }


    @Test public void testDelegateIsAccessible() {
        AbstractBehavior.AbstractChangedBehavior componentAdapter =
            (AbstractBehavior.AbstractChangedBehavior)createComponentFactory().createComponentAdapter(new NullComponentMonitor(),
                                                                              new NullLifecycleStrategy(),
                                                                              new Properties(Characteristics
                                                                                  .CDI),
                                                                              Touchable.class,
                                                                              SimpleTouchable.class,
                                                                              null, null, null);

        assertNotNull(componentAdapter.getDelegate());
    }

    private ComponentAdapter createAdapterCallingSetMessage(Class impl) {
        PropertyApplying factory = (PropertyApplying)createComponentFactory();

        Map properties = new HashMap();
        properties.put("message", "hello");

        PropertyApplying.PropertyApplicator adapter =
            (PropertyApplying.PropertyApplicator)factory.createComponentAdapter(new NullComponentMonitor(),
                                                                     new NullLifecycleStrategy(),
                                                                     new Properties(Characteristics
                                                                         .CDI),
                                                                     impl,
                                                                     impl,
                                                                     null, null, null);
        adapter.setProperties(properties);
        return adapter;
    }

    @Test public void testAllJavaPrimitiveAttributesShouldBeSetByTheAdapter() throws MalformedURLException {
        PropertyApplying factory = (PropertyApplying)createComponentFactory();
        Map properties = new HashMap();
        properties.put("byte_", "1");
        properties.put("short_", "2");
        properties.put("int_", "3");
        properties.put("long_", "4");
        properties.put("float_", "5.0");
        properties.put("double_", "6.0");
        properties.put("char_", "a");
        properties.put("boolean_", "true");
        properties.put("file_", "/foo/bar");
        properties.put("url_", "http://www.picocontainer.org/");
        properties.put("string_", "g string");
        properties.put("class_", "javax.swing.JLabel");
        PropertyApplying.PropertyApplicator adapter =
            (PropertyApplying.PropertyApplicator)factory.createComponentAdapter(new NullComponentMonitor(),
                                                                     new NullLifecycleStrategy(),
                                                                     new Properties(Characteristics
                                                                         .CDI),
                                                                     Primitives.class,
                                                                     Primitives.class,
                                                                     null, null, null);
        adapter.setProperties(properties);
        Primitives primitives = (Primitives)adapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);

        assertNotNull(primitives);
        assertEquals(1, primitives.byte_);
        assertEquals(2, primitives.short_);
        assertEquals(3, primitives.int_);
        assertEquals(4, primitives.long_);
        assertEquals(5.0, primitives.float_, 0.1);
        assertEquals(6.0, primitives.double_, 0.1);
        assertEquals('a', primitives.char_);
        assertEquals(true, primitives.boolean_);
        assertEquals(new File("/foo/bar"), primitives.file_);
        assertEquals(new URL("http://www.picocontainer.org/"), primitives.url_);
        assertEquals("g string", primitives.string_);
        assertEquals(JLabel.class, primitives.class_);
    }

    @Test public void testSetDependenComponentWillBeSetByTheAdapter() {
        picoContainer.addComponent("b", B.class);
        PropertyApplying factory = (PropertyApplying)createComponentFactory();
        Map properties = new HashMap();

        // the second b is the key of the B implementation
        properties.put("b", "b");
        PropertyApplying.PropertyApplicator adapter =
            (PropertyApplying.PropertyApplicator)factory.createComponentAdapter(new NullComponentMonitor(),
                                                                     new NullLifecycleStrategy(),
                                                                     new Properties(Characteristics
                                                                         .CDI),
                                                                     A.class,
                                                                     A.class,
                                                                     null, null, null);
        adapter.setProperties(properties);
        picoContainer.addAdapter(adapter);
        A a = picoContainer.getComponent(A.class);

        assertNotNull(a);
        assertNotNull(a.b);
    }

    @Test public void testPropertySetAfterWrappedAdapterCreationShouldBeTakenIntoAccount() {
        Caching factory = (Caching) new Caching().wrap(createComponentFactory());

        ComponentAdapter<?> adapter =
            factory.createComponentAdapter(new NullComponentMonitor(),
                                                                     new NullLifecycleStrategy(),
                                                                     new Properties(Characteristics
                                                                         .CDI),
                                                                     "foo",
                                                                     Foo.class,
                                                                     null, null, null);


        PropertyApplying.PropertyApplicator pa = adapter.findAdapterOfType(PropertyApplying.PropertyApplicator.class);

        pa.setProperty("message", "hello");

        Foo foo = (Foo)adapter.getComponentInstance(null, ComponentAdapter.NOTHING.class);

        assertEquals("hello", foo.message);
    }

    @Test public void testSetBeanPropertiesWithValueObjects() {
        PropertyApplying factory = (PropertyApplying)createComponentFactory();

        Map properties = new HashMap();
        properties.put("lenient", Boolean.FALSE);
        properties.put("2DigitYearStart", new Date(0));

        PropertyApplying.PropertyApplicator adapter =
            (PropertyApplying.PropertyApplicator)factory.createComponentAdapter(new NullComponentMonitor(),
                                                                     new NullLifecycleStrategy(),
                                                                     new Properties(Characteristics
                                                                         .CDI),
                                                                     SimpleDateFormat.class,
                                                                     SimpleDateFormat.class,
                                                                     null, null, null);
        adapter.setProperties(properties);
        picoContainer.addAdapter(adapter);


        SimpleDateFormat dateFormat = picoContainer.getComponent(SimpleDateFormat.class);
        assertNotNull(dateFormat);
        assertEquals(false, dateFormat.isLenient());
        assertEquals(new Date(0), dateFormat.get2DigitYearStart());
    }


    /** todo Is this test duplicated elsewhere?  --MR */
    @Test public void testSetBeanPropertiesWithWrongNumberOfParametersThrowsPicoInitializationException() {
        Object testBean = new Object() {
            public void setMultiValues(String val1, String Val2) {
                throw new IllegalStateException("Setter should never have been called");
            }

            public void setSomeString(String val1) {
                throw new IllegalStateException("Setter should never have been called");
            }
        };

        PropertyApplying factory = (PropertyApplying)createComponentFactory();


        PropertyApplying.PropertyApplicator adapter =
            (PropertyApplying.PropertyApplicator)factory.createComponentAdapter(new NullComponentMonitor(),
                                                                     new NullLifecycleStrategy(),
                                                                     new Properties(Characteristics
                                                                         .CDI),
                                                                     "TestBean",
                                                                     testBean.getClass(),
                                                                     null, null, null);

        Map properties = new HashMap();
        properties.put("multiValues", "abcdefg");
        adapter.setProperties(properties);

        picoContainer.addAdapter(adapter);

        try {
            Object testResult = picoContainer.getComponent("TestBean");
            fail(
                "Getting a bad test result through PropertyApplicator should have thrown exception.  Instead got:" +
                testResult);
        } catch (PicoCompositionException ex) {
            //A-ok
        }

    }


    @Test public void testSetBeanPropertiesWithInvalidValueTypes() {
        PropertyApplying factory = (PropertyApplying)createComponentFactory();


        Map properties = new HashMap();

        // Set two digit year to a boolean (should throw error)
        properties.put("2DigitYearStart", Boolean.FALSE);
        PropertyApplying.PropertyApplicator adapter =
            (PropertyApplying.PropertyApplicator)factory.createComponentAdapter(new NullComponentMonitor(),
                                                                     new NullLifecycleStrategy(),
                                                                     new Properties(Characteristics
                                                                         .CDI),
                                                                     SimpleDateFormat.class,
                                                                     SimpleDateFormat.class,
                                                                     null, null, null);
        adapter.setProperties(properties);
        picoContainer.addAdapter(adapter);


        try {
            SimpleDateFormat dateFormat = picoContainer.getComponent(SimpleDateFormat.class);
            fail(
                "Getting a bad test result through PropertyApplicator should have thrown exception.  Instead got:" +
                dateFormat);
        } catch (ClassCastException ex) {
            //A-ok
        }

    }
}
