package com.picocontainer.defaults.issues;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.picocontainer.DefaultPicoContainer;

public class Issue0382TestCase {

    public interface Paramaterized<T> {
        T get();
    }

    public static class StringParameterized implements Paramaterized<String> {

        public String get() {
            return "Hello World";
        }
    }

    public static class AcceptsParameterized {
        private final Paramaterized<?> _paramaterized;
        private final String[] _strings;

        public AcceptsParameterized(final Paramaterized<?> paramaterized, final String...strings) {
            _paramaterized = paramaterized;
            _strings = strings;
        }
    }

    public static class AcceptsParameterizedWithWildcardArray extends AcceptsParameterized {
        public AcceptsParameterizedWithWildcardArray(final Paramaterized<?> paramaterized, final String...strings) {
            super(paramaterized, strings);
        }
    }

    public static class AcceptsParameterizedWithoutWildcardArray extends AcceptsParameterized {
        // Note wildcard is omitted deliberately.
        public AcceptsParameterizedWithoutWildcardArray(@SuppressWarnings("rawtypes") final Paramaterized paramaterized, final String...strings) {
            super(paramaterized, strings);
        }
    }


    public static class AcceptsParameterizedWithWildcardList extends AcceptsParameterized {
        public AcceptsParameterizedWithWildcardList(final Paramaterized<?> paramaterized, final List<String> strings) {
            super(paramaterized, strings.toArray(new String[3]));
        }
    }

    public static class AcceptsParameterizedWithoutWildcardList extends AcceptsParameterized {
        // Note wildcard is omitted deliberately.
        public AcceptsParameterizedWithoutWildcardList(@SuppressWarnings("rawtypes") final Paramaterized paramaterized, final List<String> strings) {
            super(paramaterized, strings.toArray(new String[3]));
        }
    }

    @Test
    public void testWithWildcardArray() throws Exception {
        Class<? extends AcceptsParameterized> type = AcceptsParameterizedWithWildcardArray.class;
        assertConstructs(type);
    }

    @Test
    public void testWithoutWildcardArray() throws Exception {
        Class<? extends AcceptsParameterized> type = AcceptsParameterizedWithoutWildcardArray.class;
        assertConstructs(type);
    }


    @Test
    public void testWithWildcardList() throws Exception {
        Class<? extends AcceptsParameterized> type = AcceptsParameterizedWithWildcardList.class;
        assertConstructs(type);
    }

    @Test
    public void testWithoutWildcardList() throws Exception {
        Class<? extends AcceptsParameterized> type = AcceptsParameterizedWithoutWildcardList.class;
        assertConstructs(type);
    }

    private void assertConstructs(final Class<? extends AcceptsParameterized> type) {
        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(Paramaterized.class, StringParameterized.class);
        pico.addComponent(type);
        pico.addComponent(1, "one");
        pico.addComponent(2, "two");
        pico.addComponent(3, "three");
        AcceptsParameterized component = pico.getComponent(AcceptsParameterized.class);
        assertEquals("Hello World", component._paramaterized.get());
        assertEquals(asList("one", "two", "three"), asList(component._strings));
    }


}
