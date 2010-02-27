package org.picocontainer.converters;

import org.junit.Test;

import javax.swing.JPanel;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class BuiltInConverterTestCase {
    BuiltInConverters bic = new BuiltInConverters();

    @Test
    public void canConvertAllPrimitiveTypes() {
        assertTrue(bic.canConvert(Integer.class));
        assertTrue(bic.canConvert(Integer.TYPE));
        assertTrue(bic.canConvert(Long.class));
        assertTrue(bic.canConvert(Long.TYPE));
        assertTrue(bic.canConvert(Byte.class));
        assertTrue(bic.canConvert(Byte.TYPE));
        assertTrue(bic.canConvert(Double.class));
        assertTrue(bic.canConvert(Double.TYPE));
        assertTrue(bic.canConvert(Float.class));
        assertTrue(bic.canConvert(Float.TYPE));
        assertTrue(bic.canConvert(Character.class));
        assertTrue(bic.canConvert(Character.TYPE));
        assertTrue(bic.canConvert(Short.class));
        assertTrue(bic.canConvert(Short.TYPE));
        assertTrue(bic.canConvert(Boolean.class));
        assertTrue(bic.canConvert(Boolean.TYPE));

        assertEquals(12, bic.convert("12", Integer.class));
        assertEquals(12, bic.convert("12", Integer.TYPE));
        assertEquals(12345678901L, bic.convert("12345678901",Long.TYPE));
        assertEquals(12345678901L, bic.convert("12345678901",Long.class));
        assertEquals((byte)12, bic.convert("12", Byte.class));
        assertEquals((byte)12, bic.convert("12", Byte.TYPE));
        assertEquals(2.22, bic.convert("2.22", Double.class));
        assertEquals(2.22, bic.convert("2.22", Double.TYPE));
        assertEquals(1.11F, bic.convert("1.11", Float.class));
        assertEquals(1.11F, bic.convert("1.11", Float.TYPE));
        assertEquals('a', bic.convert("a", Character.class));
        assertEquals('a', bic.convert("a", Character.TYPE));
        assertEquals((short)12, bic.convert("12", Short.class));
        assertEquals((short)12, bic.convert("12", Short.TYPE));
        assertEquals(Boolean.TRUE, bic.convert("TRUE", Boolean.class));
        assertEquals(true, bic.convert("TRUE", Boolean.TYPE));
    }

    
    @Test
    public void canConvertFileAndURL() throws MalformedURLException {
        assertTrue(bic.canConvert(File.class));
        assertTrue(bic.canConvert(URL.class));
        assertEquals(new File("c:\\foo"), bic.convert("c:\\foo", File.class));
        assertEquals(new URL("http://example.com"), bic.convert("http://example.com", URL.class));
    }

    @Test
    public void canAddCustomConverter() {
        bic.addConverter(new JPanelConverter(), JPanel.class);
        assertTrue(bic.convert("anything", JPanel.class) instanceof JPanel);
    }

    @SuppressWarnings("serial")
    @Test
    public void canSupplementBuiltInConverters() {
        assertFalse(bic.canConvert(JPanel.class));

        BuiltInConverters bicWithJPanel = new BuiltInConverters() {
            @Override
            protected void addBuiltInConverters() {
                super.addBuiltInConverters();
                super.addConverter(new JPanelConverter(), JPanel.class);
            }
        };
        assertEquals(Boolean.TRUE, bicWithJPanel.convert("TRUE", Boolean.class));
        assertTrue(bicWithJPanel.convert("anything", JPanel.class) instanceof JPanel);

    }

    private static class JPanelConverter implements Converter<JPanel> {
        public JPanel convert(String paramValue) {
            return new JPanel();
        }
    }
    
    
}
