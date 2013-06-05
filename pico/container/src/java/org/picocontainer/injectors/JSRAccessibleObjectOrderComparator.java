package org.picocontainer.injectors;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;

import org.picocontainer.PicoCompositionException;


/**
 * Allows sort on fields and methods.  
 * <p>Sorting Rules</p>
 * <ol>
 * 	<li>Base Class Accessible Objects come first</li>
 *  <li>Static AccessibleObjects are used first before non static accessible objects if they'r ein the same class</li>
 * </ol>
 * @author Mike
 *
 */
public class JSRAccessibleObjectOrderComparator implements Comparator<AccessibleObject> {

	public int compare(AccessibleObject o1, AccessibleObject o2) {
		if (o1 == o2) {
			return 0;
		}
		
		if (o1 == null && o2 != null) {
			return -1;
		}
		
		if (o1 != null && o2 == null) {
			return 1;
		}
		
		if (isComparableOrderType(o1.getClass()) && isComparableOrderType(o2.getClass())) {
			
		} else  if (!(o1.getClass().equals(o2.getClass()) )) {
			throw new IllegalArgumentException("Both arguments need to be the same type");
		}
		
		Integer o1Distance = getDistanceToJavaLangObject(o1);
		Integer o2Distance = getDistanceToJavaLangObject(o2);
		
		int comparisonResult = o1Distance.compareTo(o2Distance);
		
		if (comparisonResult != 0) {
			return comparisonResult;
		}
		
		comparisonResult = compareFieldMethodOrder(o1.getClass(), o2.getClass());
		if (comparisonResult != 0) {
			return comparisonResult;
		}
		

		return compareStatics(o1,o2);
	}
	

	private int compareFieldMethodOrder(Class<?> o1, Class<?> o2) {
		if (Field.class.isAssignableFrom(o1) && Method.class.isAssignableFrom(o2)) {
			return -1;
		}
		
		if (Method.class.isAssignableFrom(o1) && Field.class.isAssignableFrom(o2)) {
			return 1;
		}

		//Otherwsie they're both field or both method.
		return 0;
	}


	private boolean isComparableOrderType(Class<?> type) {
		if (Field.class.isAssignableFrom(type) || Method.class.isAssignableFrom(type)) {
			return true;
		}
		
		return false;
	}


	private int getDistanceToJavaLangObject(AccessibleObject ao) {
		Class<?> currentType = getDeclaringClass(ao);
		int count = 0;
		
		while (!Object.class.equals(currentType)) {
			count++;
			currentType = currentType.getSuperclass();
		}
		
		return count;
	}

	
	private Class<?> getDeclaringClass(AccessibleObject ao) {
		if (ao instanceof Field) {
			return ((Field)ao).getDeclaringClass();
		} else if (ao instanceof Constructor) {
			return ((Constructor)ao).getDeclaringClass();
		} else if (ao instanceof Method) {
			return ((Method)ao).getDeclaringClass();
		}
		
		throw new PicoCompositionException(ao.getClass() + " does not appear to be a field, method, or constructor");
		
	}
	
	private int getModifiers(AccessibleObject ao) {
		if (ao instanceof Field) {
			return ((Field)ao).getModifiers();
		} else if (ao instanceof Constructor) {
			return ((Constructor)ao).getModifiers();
		} else if (ao instanceof Method) {
			return ((Method)ao).getModifiers();
		}
		
		throw new PicoCompositionException(ao.getClass() + " does not appear to be a field, method, or constructor");
	}
	
	private int compareStatics(AccessibleObject o1, AccessibleObject o2) {
		int o1Modifiers = getModifiers(o1);
		int o2Modifiers = getModifiers(o2);
		
		boolean o1Static = Modifier.isStatic(o1Modifiers);
		boolean o2Static = Modifier.isStatic(o2Modifiers);

		
		if (o1Static && !o2Static) {
			return -1;
		}
		
		if (!o1Static && o2Static) {
			return 1;
		}
		
		//Both are static
		return 0;
			
	}

}
