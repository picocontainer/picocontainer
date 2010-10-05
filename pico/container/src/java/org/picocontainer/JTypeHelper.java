package org.picocontainer;

import com.googlecode.jtype.Generic;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@SuppressWarnings("rawtypes")
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
            while (types.length == 0 && canGetSuperClass(aClass)) {
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

    /**
     * Checks for conditions where aClass.getSuperClass() would
     * return null.
     * <a href="http://download.oracle.com/javase/1.4.2/docs/api/java/lang/Class.html#getSuperclass()">
     * Referring Javadocs
     * </a>
     * <p>Todo: check for void</p>  
     */
    private static boolean canGetSuperClass(Class<?> aClass) {
    	
    	if (aClass.isInterface()) {
    		return false;
    	}
    	
    	if (aClass.getSuperclass() == Object.class) {
    		return false;
    	}
    	
    	if (aClass.isPrimitive()) {
    		return false;
    	}
    	
		return true;
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
