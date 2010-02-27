package org.picocontainer.script.groovy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentFactory;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.NameBinding;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.adapters.InstanceAdapter;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.AbstractInjector;
import org.picocontainer.injectors.SetterInjection;
import org.picocontainer.injectors.SetterInjector;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.script.AbstractScriptedContainerBuilderTestCase;
import org.picocontainer.classname.DefaultClassLoadingPicoContainer;
import org.picocontainer.script.ScriptedPicoContainerMarkupException;
import org.picocontainer.classname.ClassLoadingPicoContainer;
import org.picocontainer.script.TestHelper;
import org.picocontainer.script.groovy.GroovyCompilationException;
import org.picocontainer.script.groovy.GroovyContainerBuilder;
import org.picocontainer.script.testmodel.A;
import org.picocontainer.script.testmodel.B;
import org.picocontainer.script.testmodel.HasParams;
import org.picocontainer.script.testmodel.ParentAssemblyScope;
import org.picocontainer.script.testmodel.SomeAssemblyScope;
import org.picocontainer.script.testmodel.WebServerConfig;
import org.picocontainer.script.testmodel.X;

/**
 *
 * @author Paul Hammant
 * @author Mauro Talevi
 */
@RunWith(JMock.class)
public class GroovyNodeBuilderTestCase extends AbstractScriptedContainerBuilderTestCase {
	private Mockery mockery = mockeryWithCountingNamingScheme();
    private static final String ASSEMBLY_SCOPE = "SOME_SCOPE";

    @Test public void testInstantiateBasicScriptable() throws PicoCompositionException {
        Reader script = new StringReader("" +
                "import org.picocontainer.script.testmodel.*\n" +
                "X.reset()\n" +
                "scripted = builder.container {\n" +
                "    component(A)\n" +
                "}");

        MutablePicoContainer pico = (MutablePicoContainer) buildContainer(script, null, ASSEMBLY_SCOPE);
        // LifecyleContainerBuilder starts the container
        //Stop should be called by dispose since start is running.
        pico.dispose();

        assertEquals("Should match the expression", "<AA>!A", X.componentRecorder);
    }

    @Test public void testComponentInstances() throws PicoCompositionException {
        Reader script = new StringReader("" +
                "import org.picocontainer.script.testmodel.*\n" +
                "scripted = builder.container {\n" +
                "    component(key:'a', instance:'apple')\n" +
                "    component(key:'b', instance:'banana')\n" +
                "    component(instance:'noKeySpecified')\n" +
                "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        assertEquals("apple", pico.getComponent("a"));
        assertEquals("banana", pico.getComponent("b"));
        assertEquals("noKeySpecified", pico.getComponent(String.class));
    }

    @Test public void testShouldFailWhenNeitherClassNorInstanceIsSpecifiedForComponent() {
        Reader script = new StringReader("" +
                "scripted = builder.container {\n" +
                "    component(key:'a')\n" +
                "}");

        try {
            buildContainer(script, null, ASSEMBLY_SCOPE);
            fail("ScriptedPicoContainerMarkupException should have been raised");
        } catch (ScriptedPicoContainerMarkupException e) {
            // expected
        }
    }

    @Test public void testShouldAcceptConstantParametersForComponent() throws PicoCompositionException {
        Reader script = new StringReader("" +
                "import org.picocontainer.parameters.ConstantParameter\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "" +
                "scripted = builder.container {\n" +
                "    component(key:'byClass', class:HasParams, parameters:[ 'a', 'b', new ConstantParameter('c') ])\n" +
                "    component(key:'byClassString', class:'org.picocontainer.script.testmodel.HasParams', parameters:[ 'c', 'a', 't' ])\n" +
                "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        HasParams byClass = (HasParams) pico.getComponent("byClass");
        assertEquals("abc", byClass.getParams());

        HasParams byClassString = (HasParams) pico.getComponent("byClassString");
        assertEquals("cat", byClassString.getParams());
    }

    @Test public void testShouldAcceptComponentParametersForComponent() throws PicoCompositionException {
        Reader script = new StringReader("" +
                "import org.picocontainer.parameters.ComponentParameter\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "scripted = builder.container {\n" +
                "    component(key:'a1', class:A)\n" +
                "    component(key:'a2', class:A)\n" +
                "    component(key:'b1', class:B, parameters:[ new ComponentParameter('a1') ])\n" +
                "    component(key:'b2', class:B, parameters:[ new ComponentParameter('a2') ])\n" +
                "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        A a1 = (A) pico.getComponent("a1");
        A a2 = (A) pico.getComponent("a2");
        B b1 = (B) pico.getComponent("b1");
        B b2 = (B) pico.getComponent("b2");

        assertNotNull(a1);
        assertNotNull(a2);
        assertNotNull(b1);
        assertNotNull(b2);

        assertSame(a1, b1.a);
        assertSame(a2, b2.a);
        assertNotSame(a1, a2);
        assertNotSame(b1, b2);
    }

    @Test public void testShouldAcceptComponentParameter() throws PicoCompositionException {
        Reader script = new StringReader("" +
                "import org.picocontainer.parameters.ComponentParameter\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "" +
                "scripted = builder.container {\n" +
                "    component(class:A)\n" +
                "    component(key:B, class:B, parameters:[ new ComponentParameter(A) ])\n" +
                "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        A a = pico.getComponent(A.class);
        B b = pico.getComponent(B.class);

        assertNotNull(a);
        assertNotNull(b);
        assertSame(a, b.a);
    }

    @Test public void testShouldAcceptComponentParameterWithClassNameKey() throws PicoCompositionException {
        Reader script = new StringReader("" +
                "import org.picocontainer.script.testmodel.*\n" +
                "scripted = builder.container {\n" +
                "    component(classNameKey:'org.picocontainer.script.testmodel.A', class:A)\n" +
                "    component(key:B, class:B)\n" +
                "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        A a = pico.getComponent(A.class);
        B b = pico.getComponent(B.class);

        assertNotNull(a);
        assertNotNull(b);
        assertSame(a, b.a);
    }

    @Test public void testShouldAcceptComponentParameterWithClassNameKeyAndParameter() throws PicoCompositionException {
        Reader script = new StringReader("" +
                "import org.picocontainer.parameters.ComponentParameter\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "scripted = builder.container {\n" +
                "    component(classNameKey:'org.picocontainer.script.testmodel.A', class:A)\n" +
                "    component(key:B, class:B, parameters:[ new ComponentParameter(A) ])\n" +
                "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        A a = pico.getComponent(A.class);
        B b = pico.getComponent(B.class);

        assertNotNull(a);
        assertNotNull(b);
        assertSame(a, b.a);
    }

    public static class NeedsString{
        public final String foo;
        public NeedsString(String foo) {
            this.foo = foo;
        }
    }
    
    public static class ParameterTestingObject {
    	
    	public final String constructedString;
    	
    	public ParameterTestingObject() {
    		constructedString = null;
    	}
    	
    	public ParameterTestingObject(String parameterString) {
    		this.constructedString = parameterString;
    	}
    	
    }
    
    @Test
    public void canHandleZeroParameterArguments() {
        Reader script = new StringReader("" +
        		"import org.picocontainer.*\n\n" + 
                "scripted = builder.container {\n" +
                "    component(instance:'This is a test')\n" + 
                "    component(key: 'ConstructedAuto', class: org.picocontainer.script.groovy.GroovyNodeBuilderTestCase.ParameterTestingObject)\n" +
                "    component(key: 'ZeroParameter', class: org.picocontainer.script.groovy.GroovyNodeBuilderTestCase.ParameterTestingObject, parameters: Parameter.ZERO)\n" +
                "}");    	

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        assertNotNull(pico.getComponent(String.class));
        
        ParameterTestingObject testObject = (ParameterTestingObject)pico.getComponent("ConstructedAuto");
        assertEquals("This is a test", testObject.constructedString);
        
        testObject = (ParameterTestingObject)pico.getComponent("ZeroParameter");
        assertNull(testObject.constructedString);
    }

    @Test
    public void canActOnConfigAndParameterNameToResolveAmbiguity() throws PicoCompositionException, IOException {
        Reader script = new StringReader("" +
                "scripted = builder.container {\n" +
                "    config(key:'foo', value:'one')\n" +
                "    config(key:'bar', value:'two')\n" +
                "    component(class:'"+NeedsString.class.getName()+"', properties:[ org.picocontainer.Characteristics.USE_NAMES ])\n" +
                "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        NeedsString needsString = pico.getComponent(NeedsString.class);

        assertNotNull(needsString);
        assertEquals("one", needsString.foo);
    }

    @Test public void testComponentParametersScript() {
        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "import org.picocontainer.parameters.ComponentParameter\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "scripted = builder.container {\n" +
                "    component(key:'a', class:A)\n" +
                "    component(key:'b', class:B, parameters:[ new ComponentParameter('a') ])\n" +
                "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        A a = (A) pico.getComponent("a");
        B b = (B) pico.getComponent("b");
        assertSame(a, b.a);
    }

    @Test public void testShouldBeAbleToHandOffToNestedBuilder() {
        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "builder.registerBuilder(name:'foo', class:'org.picocontainer.script.groovy.TestingChildBuilder')\n" +
                "scripted = builder.container {\n" +
                "    component(key:'a', class:A)\n" +
                "    foo {\n" +
                "      component(key:'b', class:B)\n" +
                "    }\n" +
                "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        Object a = pico.getComponent("a");
        Object b = pico.getComponent("b");

        assertNotNull(a);
        assertNotNull(b);
    }

    @Test public void testShouldBeAbleToHandOffToNestedBuilderTheInlinedWay() {
        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "scripted = builder.container {\n" +
                "    component(key:'a', class:A)\n" +
                "    newBuilder(class:'org.picocontainer.script.groovy.TestingChildBuilder') {\n" +
                "      component(key:'b', class:B)\n" +
                "    }\n" +
                "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        Object a = pico.getComponent("a");
        Object b = pico.getComponent("b");

        assertNotNull(a);
        assertNotNull(b);
    }


    @Test public void testInstantiateBasicComponentInDeeperTree() {
        X.reset();
        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "scripted = builder.container {\n" +
                "    container() {\n" +
                "        component(A)\n" +
                "    }\n" +
                "}");

        MutablePicoContainer pico = (MutablePicoContainer) buildContainer(script, null, ASSEMBLY_SCOPE);
        pico.dispose();
        assertEquals("Should match the expression", "<AA>!A", X.componentRecorder);
    }

    @Test public void testCustomComponentFactoryCanBeSpecified() {
        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "scripted = builder.container(componentFactory:assemblyScope) {\n" +
                "    component(A)\n" +
                "}");

        final A a = new A();
        final ComponentFactory componentFactory = mockery.mock(ComponentFactory.class);
        mockery.checking(new Expectations(){{
        	one(componentFactory).createComponentAdapter(with(any(ComponentMonitor.class)), with(any(LifecycleStrategy.class)), with(any(Properties.class)), with(same(A.class)), with(same(A.class)), with(aNull(Parameter[].class)));
            will(returnValue(new InstanceAdapter<A>(A.class, a, new NullLifecycleStrategy(), new NullComponentMonitor())));
        }});
        PicoContainer pico = buildContainer(script, null, componentFactory);
        assertSame(a, pico.getComponent(A.class));
    }

    @Test public void testCustomComponentMonitorCanBeSpecified() {
        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "import java.io.StringWriter\n" +
                "import org.picocontainer.monitors.WriterComponentMonitor\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "writer = new StringWriter()\n" +
                "monitor = new WriterComponentMonitor(writer) \n"+
                "scripted = builder.container(componentMonitor: monitor) {\n" +
                "    component(A)\n" +
                "    component(key:StringWriter, instance:writer)\n" +
                "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        pico.getComponent(WebServerConfig.class);
        StringWriter writer = pico.getComponent(StringWriter.class);
        String s = writer.toString();
        assertTrue(s.length() > 0);
    }

    @Test public void testCustomComponentMonitorCanBeSpecifiedWhenCAFIsSpecified() {
        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "import java.io.StringWriter\n" +
                "import org.picocontainer.behaviors.Caching\n" +
                "import org.picocontainer.injectors.ConstructorInjection\n" +
                "import org.picocontainer.monitors.WriterComponentMonitor\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "writer = new StringWriter()\n" +
                "monitor = new WriterComponentMonitor(writer) \n"+
                "scripted = builder.container(componentFactory: new Caching().wrap(new ConstructorInjection()), componentMonitor: monitor) {\n" +
                "    component(A)\n" +
                "    component(key:StringWriter, instance:writer)\n" +
                "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        pico.getComponent(WebServerConfig.class);
        StringWriter writer = pico.getComponent(StringWriter.class);
        assertTrue(writer.toString().length() > 0);
    }

    @Test public void testCustomComponentMonitorCanBeSpecifiedWhenParentIsSpecified() {
        DefaultClassLoadingPicoContainer parent = new DefaultClassLoadingPicoContainer();
        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "import java.io.StringWriter\n" +
                "import org.picocontainer.monitors.WriterComponentMonitor\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "writer = new StringWriter()\n" +
                "monitor = new WriterComponentMonitor(writer) \n"+
                "scripted = builder.container(parent:parent, componentMonitor: monitor) {\n" +
                "    component(A)\n" +
                "    component(key:StringWriter, instance:writer)\n" +
                "}");

        PicoContainer pico = buildContainer(script, parent, ASSEMBLY_SCOPE);
        pico.getComponent(WebServerConfig.class);
        StringWriter writer = pico.getComponent(StringWriter.class);
        assertTrue(writer.toString().length() > 0);
    }

    @Test public void testCustomComponentMonitorCanBeSpecifiedWhenParentAndCAFAreSpecified() {
        DefaultClassLoadingPicoContainer parent = new DefaultClassLoadingPicoContainer();
        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "import java.io.StringWriter\n" +
                "import org.picocontainer.behaviors.Caching\n" +
                "import org.picocontainer.injectors.ConstructorInjection\n" +
                "import org.picocontainer.monitors.WriterComponentMonitor\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "writer = new StringWriter()\n" +
                "monitor = new WriterComponentMonitor(writer) \n"+
                "scripted = builder.container(parent:parent, componentFactory: new Caching().wrap(new ConstructorInjection()), componentMonitor: monitor) {\n" +
                "    component(A)\n" +
                "    component(key:StringWriter, instance:writer)\n" +
                "}");

        PicoContainer pico = buildContainer(script, parent, ASSEMBLY_SCOPE);
        pico.getComponent(WebServerConfig.class);
        StringWriter writer = pico.getComponent(StringWriter.class);
        assertTrue(writer.toString().length() > 0);
    }

    @Test public void testInstantiateWithImpossibleComponentDependenciesConsideringTheHierarchy() {
        X.reset();
        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "scripted = builder.container {\n" +
                "    component(B)\n" +
                "    container() {\n" +
                "        component(A)\n" +
                "    }\n" +
                "    component(C)\n" +
                "}");

        try {
            buildContainer(script, null, ASSEMBLY_SCOPE);
            fail("Should not have been able to instansiate component tree due to visibility/parent reasons.");
        } catch (AbstractInjector.UnsatisfiableDependenciesException expected) {
        }
    }

    @Test public void testInstantiateWithChildContainerAndStartStopAndDisposeOrderIsCorrect() {
        X.reset();
        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "scripted = builder.container {\n" +
                "    component(A)\n" +
                "    container() {\n" +
                "         component(B)\n" +
                "    }\n" +
                "    component(C)\n" +
                "}\n");

        // A and C have no no dependancies. B Depends on A.
        MutablePicoContainer pico = (MutablePicoContainer) buildContainer(script, null, ASSEMBLY_SCOPE);
        //pico.start();
        pico.stop();
        pico.dispose();

        assertEquals("Should match the expression", "<A<C<BB>C>A>!B!C!A", X.componentRecorder);
    }

    @Test public void testBuildContainerWithParentAttribute() {
        DefaultClassLoadingPicoContainer parent = new DefaultClassLoadingPicoContainer();
        parent.addComponent("hello", "world");

        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "scripted = builder.container(parent:parent) {\n" +
                "    component(A)\n" +
                "}\n");

        PicoContainer pico = buildContainer(script, parent, ASSEMBLY_SCOPE);
        // Should be able to get instance that was registered in the parent container
        assertEquals("world", pico.getComponent("hello"));
    }


    @Test public void testBuildContainerWithParentDependencyAndAssemblyScope() {
        DefaultClassLoadingPicoContainer parent = new DefaultClassLoadingPicoContainer();
        parent.addComponent("a", A.class);

        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "scripted = builder.container(parent:parent) {\n" +
                "  if ( assemblyScope instanceof SomeAssemblyScope ){\n "+
                "    component(B)\n" +
                "  }\n "+
                "}\n");

        PicoContainer pico = buildContainer(script, parent, new SomeAssemblyScope());
        assertNotNull(pico.getComponent(B.class));
    }

    @Test public void testBuildContainerWithParentAndChildAssemblyScopes() throws IOException {
        String scriptValue = ("" +
                "package org.picocontainer.script.groovy\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "scripted = builder.container(parent:parent) {\n" +
                "  System.out.println('assemblyScope:'+assemblyScope)\n " +
                "  if ( assemblyScope instanceof ParentAssemblyScope ){\n "+
                "    System.out.println('parent scope')\n " +
                "    component(A)\n" +
                "  } else if ( assemblyScope instanceof SomeAssemblyScope ){\n "+
                "    System.out.println('child scope')\n " +
                "    component(B)\n" +
                "  } else { \n" +
                "     throw new IllegalArgumentException('Invalid Scope: ' +  assemblyScope.getClass().getName())\n" +
                "      System.out.println('Invalid scope')\n " +
                "  } \n "+
                "}\n");

        Reader script = new StringReader(scriptValue);
        ClassLoadingPicoContainer parent = new DefaultClassLoadingPicoContainer(
            buildContainer(script, null, new ParentAssemblyScope()));

        assertNotNull(parent.getComponentAdapter(A.class, (NameBinding) null));

        script = new StringReader(scriptValue);
        PicoContainer pico = buildContainer(script, parent,  new SomeAssemblyScope());
        assertNotNull(pico.getComponent(B.class));
    }



    @Test public void testBuildContainerWhenExpectedParentDependencyIsNotFound() {
        DefaultClassLoadingPicoContainer parent = new DefaultClassLoadingPicoContainer(new Caching());

        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "scripted = builder.container(parent:parent) {\n" +
                "  if ( assemblyScope instanceof SomeAssemblyScope ){\n "+
                "    component(B)\n" +
                "  }\n "+
                "}\n");

        try {
            buildContainer(script, parent, new SomeAssemblyScope());
            fail("UnsatisfiableDependenciesException expected");
        } catch (AbstractInjector.UnsatisfiableDependenciesException e) {
            // expected
        }
    }

    @Test public void testBuildContainerWithParentAttributesPropagatesComponentFactory() {
        DefaultClassLoadingPicoContainer parent = new DefaultClassLoadingPicoContainer(new SetterInjection() );
        Reader script = new StringReader("" +
                "scripted = builder.container(parent:parent) {\n" +
                "}\n");

        MutablePicoContainer pico = (MutablePicoContainer)buildContainer(script, parent, ASSEMBLY_SCOPE);
        // Should be able to get instance that was registered in the parent container
        ComponentAdapter<String> componentAdapter = pico.addComponent(String.class).getComponentAdapter(String.class, (NameBinding) null);        
        assertNotNull("ComponentAdapter should be originally defined by parent" , componentAdapter.findAdapterOfType(SetterInjector.class));
    }



    @Test public void testExceptionThrownWhenParentAttributeDefinedWithinChild() {
        DefaultClassLoadingPicoContainer parent = new DefaultClassLoadingPicoContainer(new SetterInjection() );
        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "scripted = builder.container() {\n" +
                "    component(A)\n" +
                "    container(parent:parent) {\n" +
                "         component(B)\n" +
                "    }\n" +
                "}\n");

        try {
            buildContainer(script, parent, ASSEMBLY_SCOPE);
            fail("ScriptedPicoContainerMarkupException should have been thrown.");
        } catch (ScriptedPicoContainerMarkupException ignore) {
            // expected
        }
    }

    // TODO
    @Test public void testSpuriousAttributes() {
        DefaultClassLoadingPicoContainer parent = new DefaultClassLoadingPicoContainer();

        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "import org.picocontainer.script.testmodel.*\n" +
                "scripted = builder.container(jim:'Jam', foo:'bar') {\n" +

                "}\n");
            try {
                buildContainer(script, parent, ASSEMBLY_SCOPE);
                //fail("Should throw exception upon spurious attributes?");
            } catch (ScriptedPicoContainerMarkupException ex) {
                //ok?
            }
    }




    @Test public void testWithDynamicClassPathThatDoesNotExist() {
        DefaultClassLoadingPicoContainer parent = new DefaultClassLoadingPicoContainer();
        try {
            Reader script = new StringReader("" +
                    "        child = null\n" +
                    "        pico = builder.container {\n" +
                    "            classPathElement(path:'this/path/does/not/exist.jar')\n" +
                    "            component(class:\"FooBar\") " +
                    "        }");

            buildContainer(script, parent, ASSEMBLY_SCOPE);
            fail("should have barfed with bad path exception");
        } catch (ScriptedPicoContainerMarkupException e) {
            // excpected
        }

    }


    @Test public void testWithDynamicClassPath() {
        DefaultClassLoadingPicoContainer parent = new DefaultClassLoadingPicoContainer(new Caching());
        Reader script = new StringReader(
                "        builder = new org.picocontainer.script.groovy.GroovyNodeBuilder()\n"
              + "        File testCompJar = org.picocontainer.script.TestHelper.getTestCompJarFile()\n"
              + "        compJarPath = testCompJar.getCanonicalPath()\n"
              + "        child = null\n"
              + "        pico = builder.container {\n"
              + "            classPathElement(path:compJarPath)\n"
              + "            component(class:\"TestComp\")\n"
              + "        }");

        MutablePicoContainer pico = (MutablePicoContainer) buildContainer(script, parent, ASSEMBLY_SCOPE);

        assertTrue(pico.getComponents().size() == 1);
        assertEquals("TestComp", pico.getComponents().get(0).getClass()
                .getName());
    }




    @Test public void testWithDynamicClassPathWithPermissions() {
        DefaultClassLoadingPicoContainer parent = new DefaultClassLoadingPicoContainer(new Caching());
        Reader script = new StringReader(
                ""
                + "        builder = new org.picocontainer.script.groovy.GroovyNodeBuilder()\n"
                        + "        File testCompJar = org.picocontainer.script.TestHelper.getTestCompJarFile()\n"
                        + "        compJarPath = testCompJar.getCanonicalPath()\n"
                        + "        child = null\n"
                        + "        pico = builder.container {\n"
                        + "            classPathElement(path:compJarPath) {\n"
                        + "                grant(new java.net.SocketPermission('google.com','connect'))\n"
                        + "            }\n"
                        + "            component(class:\"TestComp\")\n"
                        + "        }" + "");

        MutablePicoContainer pico = (MutablePicoContainer)buildContainer(script, parent, ASSEMBLY_SCOPE);

        assertTrue(pico.getComponents().size() == 1);
        // can't actually test the permission under JUNIT control. We're just
        // testing the syntax here.
    }


    @Test public void testGrantPermissionInWrongPlace() {
        DefaultClassLoadingPicoContainer parent = new DefaultClassLoadingPicoContainer(new Caching());
        try {
            Reader script = new StringReader("" +
                    "        builder = new org.picocontainer.script.groovy.GroovyNodeBuilder()\n" +
                    "        File testCompJar = org.picocontainer.script.TestHelper.getTestCompJarFile()\n" +
                    "        compJarPath = testCompJar.getCanonicalPath()\n" +
                    "        child = null\n" +
                    "        pico = builder.container {\n" +
                    "            grant(new java.net.SocketPermission('google.com','connect'))\n" +
                    "        }" +
                    "");

            buildContainer(script, parent, ASSEMBLY_SCOPE);
            fail("should barf with [Don't know how to create a 'grant' child] exception");
        } catch (ScriptedPicoContainerMarkupException e) {
            assertTrue(e.getMessage().indexOf("Don't know how to create a 'grant' child") > -1);
        }

    }

    /**
     * Santity check to make sure testcomp doesn't exist in the testing classpath
     * otherwise all the tests that depend on the custom classpaths are suspect.
     */
    @Test public void testTestCompIsNotAvailableViaSystemClassPath() {
        try {
            getClass().getClassLoader().loadClass("TestComp");
            fail("Invalid configuration TestComp exists in system classpath. ");
        } catch (ClassNotFoundException ex) {
            //ok.
        }

    }

    @Test public void testWithParentClassPathPropagatesWithNoParentContainer()throws IOException {
    	System.err.println("testcomp.jar:" + System.getProperty("testcomp.jar"));
    	Class<?> aClass = TestHelper.class;
    	File base = new File(aClass.getProtectionDomain().getCodeSource().getLocation().getFile());
    	System.err.println("base:" + base);
        File testCompJar = TestHelper.getTestCompJarFile();

        URLClassLoader classLoader = new URLClassLoader(new URL[] {testCompJar.toURL()}, this.getClass().getClassLoader());
        Class<?> testComp = null;

        try {
            testComp = classLoader.loadClass("TestComp");
        } catch (ClassNotFoundException ex) {
            fail("Unable to load test component from the jar using a url classloader");
        }
        Reader script = new StringReader(""
                + "pico = builder.container(parent:parent) {\n"
                + "         component(class:\"TestComp\")\n"
                + "}");

        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, classLoader), null, null);
        assertNotNull(pico);
        Object testCompInstance = pico.getComponent(testComp.getName());
        assertSame(testCompInstance.getClass(), testComp);

    }

    @Test public void testValidationTurnedOnThrowsExceptionForUnknownAttributes() {
        DefaultClassLoadingPicoContainer parent = new DefaultClassLoadingPicoContainer();
        Reader script = new StringReader(
            "import org.picocontainer.script.NullNodeBuilderDecorator\n" +
            "import org.picocontainer.script.groovy.GroovyNodeBuilder\n" +
            "import org.picocontainer.script.testmodel.*\n" +
            "builder = new GroovyNodeBuilder(new NullNodeBuilderDecorator(), GroovyNodeBuilder.PERFORM_ATTRIBUTE_VALIDATION)\n" +
            "scripted = builder.container {\n" +
            "    component(key:'a', instance:'apple', badAttribute:'foo')\n" +
            "}");

        try {
            buildContainer(script, parent, ASSEMBLY_SCOPE);
            fail("GroovyNodeBuilder with validation should have thrown ScriptedPicoContainerMarkupException");
        } catch(GroovyCompilationException ex) {
            //Weed out the groovy compilation exceptions
            throw ex;
        } catch (ScriptedPicoContainerMarkupException ex) {
            //a-ok
        }
    }

    @Test public void testValidationTurnedOffDoesntThrowExceptionForUnknownAttributes() {
        DefaultClassLoadingPicoContainer parent = new DefaultClassLoadingPicoContainer();
        Reader script = new StringReader(
            "import org.picocontainer.script.NullNodeBuilderDecorator\n" +
            "import org.picocontainer.script.groovy.GroovyNodeBuilder\n" +
            "import org.picocontainer.script.testmodel.*\n" +
            "builder = new GroovyNodeBuilder(new NullNodeBuilderDecorator(), GroovyNodeBuilder.SKIP_ATTRIBUTE_VALIDATION)\n" +
            "scripted = builder.container {\n" +
            "    component(key:'a', instance:'apple', badAttribute:'foo')\n" +
            "}");

        try {
            buildContainer(script, parent, ASSEMBLY_SCOPE);
            //a-ok
        } catch(GroovyCompilationException ex) {
            //Weed out the groovy compilation exceptions
            throw ex;
        } catch (ScriptedPicoContainerMarkupException ex) {
            fail("GroovyNodeBuilder with validation turned off should never have thrown ScriptedPicoContainerMarkupException: " + ex.getMessage());
        }

    }


    @Test public void testComponentAdapterIsPotentiallyScriptable() throws PicoCompositionException {
        Reader script = new StringReader("" +
                "import org.picocontainer.script.testmodel.*\n" +
                "X.reset()\n" +
                "scripted = builder.container {\n" +
                "    ca = component(java.lang.Object).getComponentAdapter(java.lang.Object) \n" +
                "    component(instance:ca.getClass().getName())\n" +
                "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        // LifecyleContainerBuilder starts the container
        Object one = pico.getComponents().get(1);
        assertEquals("org.picocontainer.behaviors.Cached", one.toString());
    }


    private PicoContainer buildContainer(Reader script, PicoContainer parent, Object scope) {
        return buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), parent, scope);
    }










}
