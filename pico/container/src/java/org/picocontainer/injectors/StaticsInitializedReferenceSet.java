/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.injectors;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;


/**
 * Wrapper around a set of references to static members.
 * @author Michael Rimov
 *
 */
public class StaticsInitializedReferenceSet {

	private Set<Member> referenceSet = null;


	public synchronized boolean isMemberAlreadyInitialized(final Member member) {
		if (member == null) {
			throw new NullPointerException("member");
		}
		return getReferenceSet().contains(member);
	}

	public synchronized void markMemberInitialized(final Member member) {

		if (member == null) {
			throw new NullPointerException("member");
		}

		if (!Modifier.isStatic(member.getModifiers())) {
			throw new IllegalArgumentException("Members should only be marked if they are static");
		}

		getReferenceSet().add(member);
	}

	protected Set<Member> getReferenceSet() {
		if (referenceSet == null) {
			referenceSet = new HashSet<Member>();
		}

		return referenceSet;

	}

	public synchronized void dispose() {
		referenceSet = null;
	}

}
