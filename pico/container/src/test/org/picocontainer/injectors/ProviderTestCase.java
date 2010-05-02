/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import org.junit.Test;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.annotations.Nullable;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.behaviors.ThreadCaching;
import org.picocontainer.lifecycle.ReflectionLifecycleStrategy;
import org.picocontainer.monitors.LifecycleComponentMonitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ProviderTestCase {
    
    @Test
    public void provideMethodCanParticipateInInjection() {
        DefaultPicoContainer dpc = new DefaultPicoContainer();
        dpc.addAdapter(new Chocolatier(true));
        dpc.addComponent(NeedsChocolate.class);
        dpc.addComponent(CocaoBeans.class);
        dpc.addComponent(String.class, "Cadbury's"); // the only string in the set of components
        NeedsChocolate needsChocolate = dpc.getComponent(NeedsChocolate.class);
        assertNotNull(needsChocolate);
        assertNotNull(needsChocolate.choc);
        assertEquals(true, needsChocolate.choc.milky);
        assertNotNull(needsChocolate.choc.cocaoBeans);
        assertEquals("Cadbury's", needsChocolate.choc.name);
    }

    @Test
    public void provideMethodCanDisambiguateUsingParameterNames() {
        DefaultPicoContainer dpc = new DefaultPicoContainer();
        dpc.addAdapter(new Chocolatier(true));
        dpc.addComponent(NeedsChocolate.class);
        dpc.addComponent(CocaoBeans.class);
        dpc.addComponent("color", "Red"); // not used by virtue of key
        dpc.addComponent("name", "Cadbury's");
        dpc.addComponent("band", "Abba"); // not used by virtue of key
        NeedsChocolate needsChocolate = dpc.getComponent(NeedsChocolate.class);
        assertNotNull(needsChocolate);
        assertNotNull(needsChocolate.choc);
        assertEquals(true, needsChocolate.choc.milky);
        assertNotNull(needsChocolate.choc.cocaoBeans);
        assertEquals("Cadbury's", needsChocolate.choc.name);
    }

    @Test
    public void providerBarfsIfProvideMethodsParamsCanNotBeSatisfied() {
        DefaultPicoContainer dpc = new DefaultPicoContainer();
        dpc.addAdapter(new Chocolatier(true));
        dpc.addComponent(NeedsChocolate.class);
        try {
            dpc.getComponent(NeedsChocolate.class);
        } catch (PicoCompositionException e) {
            assertTrue(e.getMessage().contains("Parameter 0 "));
            assertTrue(e.getMessage().contains("cannot be null"));
        }
    }

    @Test
    public void providerDoesNotBarfIfProvideMethodsParamsCanNotBeSatisfiedButNullbleAnnotationUsed() {
        DefaultPicoContainer dpc = new DefaultPicoContainer();
        dpc.addAdapter(new NullableChocolatier());
        dpc.addComponent(NeedsChocolate.class);
        NeedsChocolate nc = dpc.getComponent(NeedsChocolate.class);
        assertNotNull(nc);
        assertNotNull(nc.choc);
        assertTrue(nc.choc.cocaoBeans == null);
    }

    @Test
    public void testHasLifecycle() {
        DefaultPicoContainer dpc = new DefaultPicoContainer(new Caching());
        dpc.addAdapter(new NullableChocolatier());
        dpc.addComponent(NeedsChocolate.class);
        NeedsChocolate nc = dpc.getComponent(NeedsChocolate.class);
        dpc.start();
        dpc.stop();
        dpc.dispose();
    }

    public static class CocaoBeans {
    }

    public static class Chocolate {
        private boolean milky;
        private final CocaoBeans cocaoBeans;
        private final String name;

        public Chocolate(String name) {
            this(true, new CocaoBeans(), name);
        }

        public Chocolate(boolean milky, CocaoBeans cocaoBeans, String name) {
            this.milky = milky;
            this.cocaoBeans = cocaoBeans;
            this.name = name;
        }
    }

    public static class Chocolatier extends ProviderAdapter {
        private final boolean milky;
        public Chocolatier(boolean milky) {
            this.milky = milky;
        }
        public Chocolate provide(CocaoBeans cocaoBeans, String name) {
            return new Chocolate(milky, cocaoBeans, name);
        }
        @Override
        protected boolean useNames() {
            return true;
        }
    }

    public static class NullableChocolatier extends Chocolatier {
        public NullableChocolatier() {
            super(true);
        }

        public Chocolate provide(@Nullable CocaoBeans cocaoBeans, @Nullable String name) {
            return super.provide(cocaoBeans, name);
        }
    }

    public static class NeedsChocolate {
        private Chocolate choc;
        public NeedsChocolate(Chocolate choc) {
            this.choc = choc;
        }
    }

    @Test
    public void providerBarfsIfNoProvideMethod() {
        DefaultPicoContainer dpc = new DefaultPicoContainer();
        try {
            dpc.addAdapter(new ProviderWithoutProvideMethod());
            fail("should have barfed");
        } catch (PicoCompositionException e) {
            assertEquals("There must be a method named 'provide' in the AbstractProvider implementation", e.getMessage());
        }
    }

    @Test
    public void providerBarfsIfBadProvideMethod() {
        DefaultPicoContainer dpc = new DefaultPicoContainer();
        try {
            dpc.addAdapter(new ProviderWithBadProvideMethod());
            fail("should have barfed");
        } catch (PicoCompositionException e) {
            assertEquals("There must be a non void returning method named 'provide' in the AbstractProvider implementation", e.getMessage());
        }
    }

    @Test
    public void providerBarfsIfTooManyProvideMethod() {
        DefaultPicoContainer dpc = new DefaultPicoContainer();
        try {
            dpc.addAdapter(new ProviderWithTooManyProvideMethods());
            fail("should have barfed");
        } catch (PicoCompositionException e) {
            assertEquals("There must be only one method named 'provide' in the AbstractProvider implementation", e.getMessage());
        }
    }

    public static class ProviderWithoutProvideMethod extends ProviderAdapter {
    }
    public static class ProviderWithBadProvideMethod extends ProviderAdapter {
        public void provide() {

        }
    }
    public static class ProviderWithTooManyProvideMethods extends ProviderAdapter {
        public String provide(String str) {
            return null;
        }
        public Integer provide() {
            return null;
        }
    }

    @Test
    public void provideMethodCanParticipateInInjectionWhenNotExtendingProviderAdapter() {
        DefaultPicoContainer dpc = new DefaultPicoContainer();
        dpc.addAdapter(new ProviderAdapter(new Chocolatier2(true)));
        dpc.addComponent(NeedsChocolate.class);
        dpc.addComponent(CocaoBeans.class);
        dpc.addComponent(String.class, "Cadbury's"); // the only string in the set of components
        NeedsChocolate needsChocolate = dpc.getComponent(NeedsChocolate.class);
        assertNotNull(needsChocolate);
        assertNotNull(needsChocolate.choc);
        assertEquals(true, needsChocolate.choc.milky);
        assertNotNull(needsChocolate.choc.cocaoBeans);
        assertEquals("Cadbury's", needsChocolate.choc.name);
    }

    public static class Chocolatier2 implements Provider {
        private final boolean milky;
        public Chocolatier2(boolean milky) {
            this.milky = milky;
        }
        public Chocolate provide(CocaoBeans cocaoBeans, String name) {
            return new Chocolate(milky, cocaoBeans, name);
        }
    }

    @Test
    public void providedTypeCanBeDyanamicallyDeterminedFromInstanceRatherThanType() {
        DefaultPicoContainer dpc = new DefaultPicoContainer();

        // a simlation of what a web framework would essentially do in a thread-local way
        dpc.addComponent(new StubHttpRequest("chocolate", "Lindt"));

        // this is the style being recomended for automatic request-params -> beans
        dpc.addAdapter(new ExampleRequestReader(Chocolate.class, "chocolate"));

        dpc.addComponent(NeedsChocolate.class);
        NeedsChocolate needsChocolate = dpc.getComponent(NeedsChocolate.class);
        assertNotNull(needsChocolate);
        assertNotNull(needsChocolate.choc);
        assertEquals(true, needsChocolate.choc.milky);
        assertNotNull(needsChocolate.choc.cocaoBeans);
        assertEquals("Lindt", needsChocolate.choc.name);
    }


    public static class StubHttpRequest {
        private final String key;
        private final String value;

        public StubHttpRequest(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getParameter(String name) {
            return name.equals(key) ? value : null;
        }
    }

    public static class ExampleRequestReader extends ProviderAdapter {
        private final Class clazz;
        private final String paramName;

        public ExampleRequestReader(Class clazz, String paramName) {
            this.clazz = clazz;
            this.paramName = paramName;
        }

        public Class getComponentImplementation() {
            return clazz;
        }

        public Object provide(StubHttpRequest req) {
            try {
            	return clazz.getConstructor(String.class).newInstance(req.getParameter(paramName));
            } catch (Exception e) {
                throw new RuntimeException(e);  
            }
        }
    }

    @Test
    public void providersCanHaveLifecyclesToo() {
        ComponentMonitor monitor = new LifecycleComponentMonitor();
        LifecycleStrategy lifecycle = new
                ReflectionLifecycleStrategy(monitor);

        MutablePicoContainer pico = new DefaultPicoContainer(null, lifecycle, new
                ThreadCaching());

        StringBuilder sb = new StringBuilder();
        pico.addComponent(Configuration.class);
        pico.addAdapter(new ProviderAdapter(lifecycle, new ComponentProvider(sb)));
        Object foo = pico.getComponent(Component.class);
        pico.start();
        pico.stop();
        assertEquals("@<>", sb.toString());

    }

    public class ComponentProvider implements Provider {
        private StringBuilder sb;

        public ComponentProvider(StringBuilder sb) {
            this.sb = sb;
        }

        public Component provide(Configuration config) {
            return new ComponentImpl(sb, config.getHost(), config.getPort());
        }
    }

    public static class Configuration {

        public String getHost() {
            return "hello";
        }

        public int getPort() {
            return 99;
        }

        public void start() {
        }

        public void stop() {
        }

    }

    public static interface Component {

        public void start();

        public void stop();

    }

    public static class ComponentImpl implements Component {

        private StringBuilder sb;

        public ComponentImpl(StringBuilder sb, String host, int port) {
            this.sb = sb.append("@");
        }

        public void start() {
            sb.append("<");
        }
        public void stop() {
            sb.append(">");
        }

    }


}
