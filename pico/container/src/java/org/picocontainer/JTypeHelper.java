package org.picocontainer;

import com.googlecode.jtype.Generic;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class JTypeHelper {

    public static final Generic INTEGER = Generic.get(Integer.class);
    public static final Generic LONG = Generic.get(Long.class);
    public static final Generic FLOAT = Generic.get(Float.class);
    public static final Generic DOUBLE = Generic.get(Double.class);
    public static final Generic BOOLEAN = Generic.get(Boolean.class);
    public static final Generic CHARACTER = Generic.get(Character.class);
    public static final Generic SHORT = Generic.get(Short.class);
    public static final Generic BYTE = Generic.get(Byte.class);
    public static final Generic VOID = Generic.get(Void.TYPE);

    public static boolean isAssignableFrom(Generic<?> generic, Class<?> aClass) {
        Type type = generic.getType();
        if (type instanceof Class) {
            return ((Class) type).isAssignableFrom(aClass);
        } else if (type instanceof ParameterizedType) {
            Generic g = Generic.get(aClass);
            Type[] types = aClass.getGenericInterfaces();
            while (types.length == 0 && aClass.getSuperclass() != Object.class) {
                aClass = aClass.getSuperclass();
                types = aClass.getGenericInterfaces();
            }
            if (types.length == 0) {
                return false;
            }
            Generic aClassGeneric = Generic.get(types[0]);
            boolean b = generic.equals(aClassGeneric);
            boolean from = generic.getRawType().isAssignableFrom(aClass);
            return b || from;
        }
        return false;
    }

    public static boolean isAssignableTo(Generic<?> generic, Class aClass) {
        if (generic.getType() instanceof Class) {
            return aClass.isAssignableFrom((Class<?>) generic.getType());
        }
        return false;
    }

    public static boolean isPrimitive(Generic<?> generic) {
        return generic.getType() instanceof Class && ((Class) generic.getType()).isPrimitive();
    }

}
