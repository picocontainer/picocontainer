package org.picocontainer.converters;

import org.picocontainer.Converters;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides some built-in converters used by {@link DefaultPicoContainer}. It
 * supports by default primitive types (and boxed equivalents) and for
 * {@link File} and {@link URL} types. Built-in converters can be changed by
 * extending the class and overriding the method {@link #addBuiltInConverters()}.
 */
@SuppressWarnings("serial")
public class BuiltInConverters implements Converters, Serializable {

    private final Map<Class<?>, Converter<?>> converters = new HashMap<Class<?>, Converter<?>>();

    public BuiltInConverters() {
        addBuiltInConverters();
    }

    protected void addBuiltInConverters() {
        addMultiTypeConverter(new IntegerConverter(), Integer.class, Integer.TYPE);
        addMultiTypeConverter(new DoubleConverter(), Double.class, Double.TYPE);
        addMultiTypeConverter(new BooleanConverter(), Boolean.class, Boolean.TYPE);
        addMultiTypeConverter(new LongConverter(), Long.class, Long.TYPE);
        addMultiTypeConverter(new FloatConverter(), Float.class, Float.TYPE);
        addMultiTypeConverter(new CharacterConverter(), Character.class, Character.TYPE);
        addMultiTypeConverter(new ByteConverter(), Byte.class, Byte.TYPE);
        addMultiTypeConverter(new ShortConverter(), Short.class, Short.TYPE);
        addConverter(new FileConverter(), File.class);
        addConverter(new UrlConverter(), URL.class);
    }

    private void addMultiTypeConverter(Converter<?> converter, Class<?>... types) {
        for (Class<?> type : types) {
            addConverter(converter, type);
        }
    }

    protected void addConverter(Converter<?> converter, Class<?> key) {
        converters.put(key, converter);
    }

    public boolean canConvert(Type type) {
        return converters.containsKey(type);
    }

    public Object convert(String paramValue, Type type) {
        Converter<?> converter = converters.get(type);
        if (converter == null) {
            return null;
        }
        return converter.convert(paramValue);
    }

}
