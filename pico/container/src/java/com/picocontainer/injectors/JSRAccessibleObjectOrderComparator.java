/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.injectors;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;

import com.picocontainer.PicoCompositionException;


/**
 * Allows sort on fields and methods.
 * <p>Sorting Rules</p>
 * <ol>
 * 	<li>Base Class Accessible Objects come first</li>
 *  <li>Static AccessibleObjects are used first before non static accessible objects if they'r ein the same class</li>
 * </ol>
 * @author Michael Rimov
 *
 */
public class JSRAccessibleObjectOrderComparator implements Comparator<AccessibleObject> {

	public int compare(final AccessibleObject o1, final AccessibleObject o2) {
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


	/**
	 * In JSR-330, if they're in the same class, fields are injected
	 * before methods.
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int compareFieldMethodOrder(final Class<?> o1, final Class<?> o2) {
		if (Field.class.isAssignableFrom(o1) && Method.class.isAssignableFrom(o2)) {
			return -1;
		}

		if (Method.class.isAssignableFrom(o1) && Field.class.isAssignableFrom(o2)) {
			return 1;
		}

		//Otherwsie they're both field or both method.
		return 0;
	}


	/**
	 * Currently this comparator only handles fields and methods.
	 * @param type
	 * @return
	 */
	private boolean isComparableOrderType(final Class<?> type) {
		if (Field.class.isAssignableFrom(type) || Method.class.isAssignableFrom(type)) {
			return true;
		}

		return false;
	}


	/**
	 * Computes a number that represents the # of classes between the owning class
	 * of the member being checked and java.lang.Object.  Further away gets a
	 * higher score.
	 * @param ao
	 * @return
	 */
	private int getDistanceToJavaLangObject(final AccessibleObject ao) {
		Class<?> currentType = getDeclaringClass(ao);
		int count = 0;

		while (!Object.class.equals(currentType)) {
			count++;
			currentType = currentType.getSuperclass();
		}

		return count;
	}


	private Class<?> getDeclaringClass(final AccessibleObject ao) {
		if (ao instanceof Member) {
			return ((Member)ao).getDeclaringClass();
		}

		throw new PicoCompositionException(ao.getClass() + " does not appear to be a field, method, " +
				"or constructor (or anything that implements Member interface)");

	}

	private int getModifiers(final AccessibleObject ao) {
		if (ao instanceof Member) {
			return ((Member)ao).getModifiers();
		}

		throw new PicoCompositionException(ao.getClass() + " does not appear to be a field, method, " +
				"or constructor (or anything that implements the Member interface)");
	}

	private int compareStatics(final AccessibleObject o1, final AccessibleObject o2) {
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
