package org.picocontainer.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.picocontainer.behaviors.Behaviors.caching;
import static org.picocontainer.behaviors.Behaviors.implementationHiding;
import static org.picocontainer.injectors.Injectors.SDI;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;
import org.picocontainer.ComponentFactory;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.classname.ClassLoadingPicoContainer;
import org.picocontainer.classname.DefaultClassLoadingPicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.behaviors.ImplementationHiding;
import org.picocontainer.behaviors.Synchronizing;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.injectors.AdaptingInjection;
import org.picocontainer.injectors.AnnotatedMethodInjection;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.injectors.SetterInjection;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.lifecycle.ReflectionLifecycleStrategy;
import org.picocontainer.lifecycle.StartableLifecycleStrategy;
import org.picocontainer.monitors.ConsoleComponentMonitor;
import org.picocontainer.monitors.NullComponentMonitor;

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
        ClassLoadingPicoContainer expected = new TestScriptedContainer(null,new DefaultPicoContainer(new AdaptingInjection(),new NullLifecycleStrategy(), new EmptyPicoContainer()));
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }


    @SuppressWarnings("serial")
	public static class TestScriptedContainer extends DefaultClassLoadingPicoContainer {
        public TestScriptedContainer(ClassLoader classLoader, MutablePicoContainer delegate) {
            super(classLoader, delegate);
        }
    }

    @Test public void testWithCustomScriptedAndPicoContainer() throws IOException {
        ClassLoadingPicoContainer nc = new ScriptedBuilder().implementedBy(TestScriptedContainer.class).picoImplementedBy(TestPicoContainer.class).build();
        ClassLoadingPicoContainer expected = new TestScriptedContainer(null, new TestPicoContainer(new AdaptingInjection(), new NullComponentMonitor(), new NullLifecycleStrategy(), new EmptyPicoContainer()));
        assertEquals(xs.toXML(expected),xs.toXML(nc));
    }

    @SuppressWarnings("serial")
	public static class TestPicoContainer extends DefaultPicoContainer {
        public TestPicoContainer(ComponentFactory componentFactory, ComponentMonitor monitor, LifecycleStrategy lifecycleStrategy, PicoContainer parent) {
            super(componentFactory, lifecycleStrategy, parent, monitor);
        }
    }




}
