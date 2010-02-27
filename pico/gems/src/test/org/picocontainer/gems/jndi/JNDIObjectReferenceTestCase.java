/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.gems.jndi;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;

/**
 * test capabilities of object reference storing stuff in JNDI
 * @author Konstantin Pribluda
 *
 */
public class JNDIObjectReferenceTestCase {

	Context ctx;
	JNDIObjectReference reference;
	
	@Before
	public void setUp() throws Exception {
		Hashtable ht = new Hashtable();
		ht.put("java.naming.factory.initial","org.osjava.sj.memory.MemoryContextFactory");
		ctx = new InitialContext(ht);

	}
	/**
	 * object shall be stored and returned back
	 * @throws NamingException
	 */
	@Test public void testStorageAndRetrieval() throws NamingException {
		reference = new JNDIObjectReference("glee:/glum/glarch/blurge", ctx);
		String obj = new String("that's me");		
		reference.set(obj);
		// shall be the same object - from reference or from 
		// context itself
		assertSame(obj,reference.get());
		assertSame(obj,ctx.lookup("glee:/glum/glarch/blurge"));
		
		// try to rebind context
		
		Integer glum = new Integer(239);
		reference.set(glum);
		assertSame(glum,reference.get());
		assertSame(glum,ctx.lookup("glee:/glum/glarch/blurge"));
		
		
		// and also unbind
		reference.set(null);
		assertNull(ctx.lookup("glee:/glum/glarch/blurge"));
	}
	
	/**
	 * test that object is safely stored in root context
	 */
	@Test public void testStorageInRoot() {
		reference = new JNDIObjectReference("glarch", ctx);
		String obj = new String("that's me");		
		reference.set(obj);
		
		assertSame(obj,reference.get());
	}


}
