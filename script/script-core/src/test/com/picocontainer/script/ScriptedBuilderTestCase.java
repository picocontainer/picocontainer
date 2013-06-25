package com.picocontainer.script;

import static com.picocontainer.behaviors.Behaviors.caching;
import static com.picocontainer.behaviors.Behaviors.implementationHiding;
import static com.picocontainer.injectors.Injectors.SDI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;

import com.picocontainer.ComponentFactory;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoContainer;
import com.picocontainer.behaviors.Caching;
import com.picocontainer.behaviors.ImplementationHiding;
import com.picocontainer.behaviors.Synchronizing;
import com.picocontainer.classname.ClassLoadingPicoContainer;
import com.picocontainer.classname.DefaultClassLoadingPicoContainer;
import com.picocontainer.containers.EmptyPicoContainer;
import com.picocontainer.injectors.AdaptingInjection;
import com.picocontainer.injectors.AnnotatedMethodInjection;
import com.picocontainer.injectors.ConstructorInjection;
import com.picocontainer.injectors.SetterInjection;
import com.picocontainer.lifecycle.NullLifecycleStrategy;
import com.picocontainer.lifecycle.ReflectionLifecycleStrategy;
import com.picocontainer.lifecycle.StartableLifecycleStrategy;
import com.picocontainer.monitors.ConsoleComponentMonitor;
import com.picocontainer.monitors.NullComponentMonitor;
import com.thoughtworks.xstream.XStream;

public class ScriptedBuilderTestCase {

    XStream xs = new XStream();

    @Test public void testBasic() throws IOException {
        ClassLoadingPicoContainer nc = new ScriptedBuilder().build();
        NullComponentMonitor cm = new NullComponentMonitor();
        ClassLoadingPicoContainer expected = new DefaultClassLoadingPicoContainer(new AdaptingInjection(),new NullLifecycleStrategy(), new EmptyPicoContainer(), null, cm);
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }

    @Test public void testWithStartableLifecycle() throws IOException {
        ClassLoadingPicoContainer nc = new ScriptedBuilder().withLifecycle().build();
        NullComponentMonitor cm = new NullComponentMonitor();
        ClassLoadingPicoContainer expected = new DefaultClassLoadingPicoContainer(new AdaptingInjection(),new StartableLifecycleStrategy(cm), new EmptyPicoContainer(), null, cm);
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }

    @Test public void testWithReflectionLifecycle() throws IOException {
        ClassLoadingPicoContainer nc = new ScriptedBuilder().withReflectionLifecycle().build();
        NullComponentMonitor cm = new NullComponentMonitor();
        ClassLoadingPicoContainer expected = new DefaultClassLoadingPicoContainer(new AdaptingInjection(),new ReflectionLifecycleStrategy(cm), new EmptyPicoContainer(), null, cm);
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }

    @Test public void testWithConsoleMonitor() throws IOException {
        ClassLoadingPicoContainer nc = new ScriptedBuilder().withConsoleMonitor().build();
        ConsoleComponentMonitor cm = new ConsoleComponentMonitor();
        ClassLoadingPicoContainer expected = new DefaultClassLoadingPicoContainer(new AdaptingInjection(),new NullLifecycleStrategy(), new EmptyPicoContainer(), null, cm);
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }

    @Test public void testWithCustomMonitorByClass() throws IOException {
        ClassLoadingPicoContainer nc = new ScriptedBuilder().withMonitor(ConsoleComponentMonitor.class).build();
        ConsoleComponentMonitor cm = new ConsoleComponentMonitor();
        ClassLoadingPicoContainer expected = new DefaultClassLoadingPicoContainer(new AdaptingInjection(),new NullLifecycleStrategy(), new EmptyPicoContainer(), null, cm);
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }

    @SuppressWarnings({ "unchecked" })
    @Test public void testWithBogusCustomMonitorByClass() {
        try {
            Class aClass = HashMap.class;
            new ScriptedBuilder().withMonitor(aClass).build();
            fail("should have barfed");
        } catch (ClassCastException e) {
            // expected
        }
    }

    @Test public void testWithImplementationHiding() throws IOException {
        ClassLoadingPicoContainer nc = new ScriptedBuilder().withHiddenImplementations().build();
        ComponentMonitor cm = new NullComponentMonitor();
        ClassLoadingPicoContainer expected = new DefaultClassLoadingPicoContainer(new ImplementationHiding().wrap(new AdaptingInjection()),new NullLifecycleStrategy(), new EmptyPicoContainer(), null, cm);
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }


    @Test public void testWithImplementationHidingInstance() throws IOException {
        ClassLoadingPicoContainer nc = new ScriptedBuilder().withComponentFactory(new ImplementationHiding()).build();
        ComponentMonitor cm = new NullComponentMonitor();
        ClassLoadingPicoContainer expected = new DefaultClassLoadingPicoContainer(new ImplementationHiding().wrap(new AdaptingInjection()),new NullLifecycleStrategy(), new EmptyPicoContainer(), null, cm);
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }

    @Test public void testWithComponentFactoriesListChainThingy() throws IOException{
        ClassLoadingPicoContainer nc = new ScriptedBuilder(SDI()).withBehaviors(caching(), implementationHiding()).build();
        ComponentMonitor cm = new NullComponentMonitor();
        ClassLoadingPicoContainer expected = new DefaultClassLoadingPicoContainer(new Caching().wrap(new ImplementationHiding().wrap(new SetterInjection())),new NullLifecycleStrategy(), new EmptyPicoContainer(), null, cm);
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }

    @SuppressWarnings("serial")
	public static class CustomParentcontainer extends EmptyPicoContainer {
    }

    @Test public void testWithCustomParentContainer() throws IOException {
        ClassLoadingPicoContainer nc = new ScriptedBuilder(new CustomParentcontainer()).build();
        ComponentMonitor cm = new NullComponentMonitor();
        ClassLoadingPicoContainer expected = new DefaultClassLoadingPicoContainer(new AdaptingInjection(),new NullLifecycleStrategy(), new CustomParentcontainer(), null, cm);
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }

    @Test public void testWithBogusParentContainerBehavesAsIfNotSet() throws IOException {
        ClassLoadingPicoContainer nc = new ScriptedBuilder((PicoContainer)null).build();
        ComponentMonitor cm = new NullComponentMonitor();
        ClassLoadingPicoContainer expected = new DefaultClassLoadingPicoContainer(new AdaptingInjection(),new NullLifecycleStrategy(), new EmptyPicoContainer(), null, cm);
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }


    @Test public void testWithSetterDI() throws IOException {
        ClassLoadingPicoContainer nc = new ScriptedBuilder().withSetterInjection().build();
        ComponentMonitor cm = new NullComponentMonitor();
        ClassLoadingPicoContainer expected = new DefaultClassLoadingPicoContainer(new SetterInjection(),new NullLifecycleStrategy(), new EmptyPicoContainer(), null, cm);
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }

    @Test public void testWithAnnotationDI() throws IOException {
        ClassLoadingPicoContainer nc = new ScriptedBuilder().withAnnotatedMethodInjection().build();
        ComponentMonitor cm = new NullComponentMonitor();
        ClassLoadingPicoContainer expected = new DefaultClassLoadingPicoContainer(new AnnotatedMethodInjection(),new NullLifecycleStrategy(), new EmptyPicoContainer(), null, cm);
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }

    @Test public void testWithCtorDI() throws IOException {
        ClassLoadingPicoContainer nc = new ScriptedBuilder().withConstructorInjection().build();
        ComponentMonitor cm = new NullComponentMonitor();
        ClassLoadingPicoContainer expected = new DefaultClassLoadingPicoContainer(new ConstructorInjection(),new NullLifecycleStrategy(), new EmptyPicoContainer(), null, cm);
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }

    @Test public void testWithImplementationHidingAndSetterDI() throws IOException {
        ClassLoadingPicoContainer nc = new ScriptedBuilder().withHiddenImplementations().withSetterInjection().build();
        ComponentMonitor cm = new NullComponentMonitor();
        ClassLoadingPicoContainer expected = new DefaultClassLoadingPicoContainer(new ImplementationHiding().wrap(new SetterInjection()),new NullLifecycleStrategy(), new EmptyPicoContainer(), null, cm);
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }

    @Test public void testWithCachingImplementationHidingAndSetterDI() throws IOException {
        ClassLoadingPicoContainer nc = new ScriptedBuilder().withCaching().withHiddenImplementations().withSetterInjection().build();
        ComponentMonitor cm = new NullComponentMonitor();
        ClassLoadingPicoContainer expected = new DefaultClassLoadingPicoContainer(new Caching().wrap(new ImplementationHiding().wrap(new SetterInjection())),new NullLifecycleStrategy(), new EmptyPicoContainer(), null, cm);
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }

    @Test public void testWithThreadSafety() throws IOException {
        ClassLoadingPicoContainer nc = new ScriptedBuilder().withSynchronizing().build();
        ComponentMonitor cm = new NullComponentMonitor();
        ClassLoadingPicoContainer expected = new DefaultClassLoadingPicoContainer(new Synchronizing().wrap(new AdaptingInjection()),new NullLifecycleStrategy(), new EmptyPicoContainer(), null, cm);
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }

    @Test public void testWithCustomScriptedContainer() throws IOException {
        ClassLoadingPicoContainer nc = new ScriptedBuilder().implementedBy(TestScriptedContainer.class).build();
        ClassLoadingPicoContainer expected = new TestScriptedContainer(null,new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new AdaptingInjection()));
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }


    @SuppressWarnings("serial")
	public static class TestScriptedContainer extends DefaultClassLoadingPicoContainer {
        public TestScriptedContainer(final ClassLoader classLoader, final MutablePicoContainer delegate) {
            super(classLoader, delegate);
        }
    }

    @Test public void testWithCustomScriptedAndPicoContainer() throws IOException {
        ClassLoadingPicoContainer nc = new ScriptedBuilder().implementedBy(TestScriptedContainer.class).picoImplementedBy(TestPicoContainer.class).build();
        ClassLoadingPicoContainer expected = new TestScriptedContainer(null, new TestPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new NullComponentMonitor(), new AdaptingInjection()));
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }

    @SuppressWarnings("serial")
	public static class TestPicoContainer extends DefaultPicoContainer {
        public TestPicoContainer(final PicoContainer parent, final LifecycleStrategy lifecycle, final ComponentMonitor monitor, final ComponentFactory componentFactory) {
            super(parent, lifecycle, monitor, componentFactory);
        }
    }




}
