package com.picocontainer.monitors;

import static com.picocontainer.Characteristics.USE_NAMES;
import static com.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.googlecode.jtype.Generic;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.composers.RegexComposer;
import com.picocontainer.monitors.ComposingMonitor;

@RunWith(JMock.class)
public class RegexComposerTestCase extends TestCase {

    private final Mockery mockery = mockeryWithCountingNamingScheme();

    @Test
    public void regexWorksInGetComponentCalls() {
        MutablePicoContainer pico = new DefaultPicoContainer(new ComposingMonitor(new RegexComposer()));
        pico.addComponent("apple1", "Braeburn");
        pico.addComponent("apple2", "Granny Smith");
        pico.addComponent("plum", "Victoria");

        List apples = (List) pico.getComponent("apple[1-9]");
        assertEquals("[Braeburn, Granny Smith]", apples.toString());
    }

    @Test
    public void canReturningDifferentListsForDifferentComposers() {
        MutablePicoContainer pico = new DefaultPicoContainer(
                new ComposingMonitor(new RegexComposer("apple[1-9]", "apples"), new RegexComposer("plum*", "plums")));
        pico.addComponent("apple1", "Braeburn")
                .addComponent("apple2", "Granny Smith")
                .addComponent("plumV", "Victoria");
        pico.as(USE_NAMES).addComponent(NeedsApples.class)
                .as(USE_NAMES).addComponent(NeedsPlums.class);

        assertEquals("[Braeburn, Granny Smith]", pico.getComponent(NeedsApples.class).apples.toString());

        assertEquals("[Victoria]", pico.getComponent(NeedsPlums.class).plums.toString());
    }

    @Test
    public void nonMatchingCanFallThroughToAnotherComponentMonitor() throws NoSuchMethodException {

        final List<String> apples = new ArrayList<String>();
        apples.add("Cox's");

        final ComponentMonitor fallThru = mockery.mock(ComponentMonitor.class);
        mockery.checking(new Expectations() {{
            one(fallThru).noComponentFound(with(any(MutablePicoContainer.class)), with(equal(new Generic<List<String>>(){}.getType())));
            will(returnValue(null));
            one(fallThru).noComponentFound(with(any(MutablePicoContainer.class)), with(equal("apples")));
            will(returnValue(apples));
            one(fallThru).instantiating(with(any(MutablePicoContainer.class)), with(any(ComponentAdapter.class)), with(any(Constructor.class)));
            will(returnValue(NeedsApples.class.getConstructor(List.class)));
            one(fallThru).instantiated(with(any(MutablePicoContainer.class)), with(any(ComponentAdapter.class)), with(any(Constructor.class)), with(any(NeedsApples.class)), with(any(Object[].class)), with(any(int.class)));
        }});

        final MutablePicoContainer pico = new DefaultPicoContainer(new ComposingMonitor(fallThru, new RegexComposer("qqq[1-9]", "qqq")));
        pico.addComponent("apple1", "Braeburn")
                .addComponent("integer", 1)
                .addComponent("apple2", "Granny Smith")
                .addComponent("plum", "Victoria");

        pico.as(USE_NAMES).addComponent(NeedsApples.class);

        assertEquals("[Cox's]", pico.getComponent(NeedsApples.class).apples.toString());
    }

    public static class NeedsApples {
        private final List<String> apples;

        public NeedsApples(final List<String> apples) {
            this.apples = apples;
        }
    }

    public static class NeedsPlums {
        private final List<String> plums;

        public NeedsPlums(final List<String> plums) {
            this.plums = plums;
        }
    }


}
