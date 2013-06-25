package com.picocontainer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import com.googlecode.jtype.Generic;

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

    /**
     *
     * @param generic
     * @param aClass
     * @return
     */
    @SuppressWarnings("unchecked")
	public static boolean isAssignableFrom(final Generic<?> generic, final Class<?> aClass) {
        Type type = generic.getType();
        Class<?> typeToCompare = aClass;
        if (type instanceof ParameterizedType) {
            //Generic g = Generic.get(aClass);
        	//Recursively look for first super class that has a a parameterized type argument.

        	ParameterizedType castType = (ParameterizedType)type;
        	boolean isWildcardType = false;
        	if (castType.getActualTypeArguments()[0] instanceof WildcardType) {
        		isWildcardType= true;
        	}


            Type[] types = typeToCompare.getGenericInterfaces();
            while (types.length == 0 && canGetSuperClass(typeToCompare)) {
            	typeToCompare = typeToCompare.getSuperclass();
                types = typeToCompare.getGenericInterfaces();
            }
            if (types.length == 0) {
            	//Parameter aClass doesn't have a type assigned to it, if the types are compatible
            	//then we'll ignore the generic and hope for the best because aClass is a raw type.
            	return generic.getRawType().isAssignableFrom(aClass);
            }
            Generic aClassGeneric = Generic.get(types[0]);
            boolean b = generic.equals(aClassGeneric);


            //boolean from = generic.getRawType().isAssignableFrom(aClass);
            boolean from = false;
            if (isWildcardType || isRawType(aClass)) {
            	from = generic.getRawType().isAssignableFrom(aClass);
            }

            return b || from;
        } else if (type instanceof Class) {
            return ((Class) type).isAssignableFrom(typeToCompare);
        }
        return false;
    }

    /**
     * Returns true if the type inspected is a raw type.  Example:  List, as opposed to a List<String>
     * @todo I am NOT a generic expert, I came up with this code by watching the debugger, and reading javadocs.  If
     * there is a better way to determine if something is new List() vs new List<String> I'd love to see it.
     * @param aClass
     * @return
     */
    public static boolean isRawType(final Class<?> aClass) {
		Class<?> typeToCompare = aClass;
	    Type[] types = typeToCompare.getGenericInterfaces();
	    while (types.length == 0 && canGetSuperClass(typeToCompare)) {
	    	typeToCompare = typeToCompare.getSuperclass();
	        types = typeToCompare.getGenericInterfaces();
	    }

	    if (types.length == 0) {
        	return true;
        }

	    //
	    // List is a good example
	    //
	    Type typeToExamine = types[0];
	    if (typeToExamine instanceof ParameterizedType) {
	    	ParameterizedType pt = (ParameterizedType)typeToExamine;
	    	Type arg = pt.getActualTypeArguments()[0];
	    	if (arg instanceof TypeVariable) {
	    		TypeVariable tv = (TypeVariable)arg;
	    		//best I can figure out, if the declaration has a "< such as List<String>
	    		//then its not a raw type, if it doesn't, then its a raw type. -MR
	    		if (!tv.getGenericDeclaration().toString().contains("<")) {
	    			return true;
	    		}
	    	}


	    	return false;
	    }



		return true;
	}

	/**
     * Checks for conditions where aClass.getSuperClass() would
     * return null.
     * <a href="http://download.oracle.com/javase/1.4.2/docs/api/java/lang/Class.html#getSuperclass()">
     * Referring Javadocs
     * </a>
     * <p>Todo: check for void</p>
     */
    private static boolean canGetSuperClass(final Class<?> aClass) {

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

    /**
     * Checks that the generic's type is a class, and performs a direct
     * <code>aClass.isAssignableFrom(genericType)</code>
     * @param generic the generic type to check
     * @param aClass the type to check for compatibility/
     * @return
     */
	public static boolean isAssignableTo(final Generic<?> generic, final Class<?> aClass) {
        if (generic.getType() instanceof Class) {
            return aClass.isAssignableFrom((Class<?>) generic.getType());
        }
        return false;
    }

    public static boolean isPrimitive(final Generic<?> generic) {
        return generic.getType() instanceof Class && ((Class) generic.getType()).isPrimitive();
    }

}
