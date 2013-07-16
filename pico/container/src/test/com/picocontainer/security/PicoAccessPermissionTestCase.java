package com.picocontainer.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PicoAccessPermissionTestCase {

	@Test
	public void testWildcardScopesAndReadWriteIsVeryPermissive() {
		final PicoAccessPermission mainTest = new PicoAccessPermission("*", "read,write");
		
		PicoAccessPermission readTest = new PicoAccessPermission("request", "read");
		assertTrue(mainTest.implies(readTest));
		
		PicoAccessPermission writeTest = new PicoAccessPermission("session", "write");
		assertTrue(mainTest.implies(writeTest));
		
		PicoAccessPermission readWriteTest = new PicoAccessPermission("application", "read,write");
		assertTrue(mainTest.implies(readWriteTest));
	}
	
	@Test
	public void testScopeDefinesTheValue() {
		final PicoAccessPermission mainTest = new PicoAccessPermission("request", "read,write");
		
		PicoAccessPermission requestTest = new PicoAccessPermission("request", "read");
		assertTrue(mainTest.implies(requestTest));
		
		PicoAccessPermission sessionTest = new PicoAccessPermission("session", "read");
		assertFalse(mainTest.implies(sessionTest));
	}
	
	@Test
	public void testSameValuesResultsInSameHashCode() {
		final PicoAccessPermission mainTest = new PicoAccessPermission("request", "read,write");
		final PicoAccessPermission testTwo = new PicoAccessPermission("request", "read,write");
		
		
		assertTrue(mainTest.hashCode() > 0);
		assertEquals(mainTest.hashCode(), testTwo.hashCode());
	}
	
	@Test
	public void testEquals() {
		final PicoAccessPermission mainTest = new PicoAccessPermission("request", "read,write");
		final PicoAccessPermission testTwo = new PicoAccessPermission("request", "read,write");
		
		assertTrue(mainTest.equals(testTwo));
		
		final PicoAccessPermission testThree = new PicoAccessPermission("session", "read,write");
		assertFalse(mainTest.equals(testThree));
		
		final PicoAccessPermission testFour = new PicoAccessPermission("request", "read");
		assertFalse(mainTest.equals(testFour));
	}
	
	@Test
	public void testNullScopeEqualsEmptyStringScope() {
		final PicoAccessPermission mainTest = new PicoAccessPermission(null, "read,write");
		final PicoAccessPermission testTwo = new PicoAccessPermission("", "read,write");
		assertEquals(mainTest, testTwo);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidAccessMode() {
		new PicoAccessPermission(null, "read,fwibble");
	}
	
	@Test(expected=NullPointerException.class)
	public void testNullAccessModeNotAllowed() {
		new PicoAccessPermission("request", null);
	}

	
	@Test
	public void testAccessModeCombinationsForReadWritePermissions() {
		final PicoAccessPermission readWrite = new  PicoAccessPermission(null, "read,write");
		
		final PicoAccessPermission permissionTwo = new PicoAccessPermission(null, "read");
		final PicoAccessPermission permissionThree = new PicoAccessPermission(null, "write");
		final PicoAccessPermission permissionFour = new PicoAccessPermission(null, "write, read");
		
		assertTrue(readWrite.implies(permissionTwo));
		assertTrue(readWrite.implies(permissionThree));
		assertTrue(readWrite.implies(permissionFour));
	}
	
	@Test
	public void testAccessModeCombinationsForWriteOnlyPermissions() {
		final PicoAccessPermission readWrite = new  PicoAccessPermission(null, "write");
		
		final PicoAccessPermission permissionTwo = new PicoAccessPermission(null, "read");
		final PicoAccessPermission permissionThree = new PicoAccessPermission(null, "write");
		final PicoAccessPermission permissionFour = new PicoAccessPermission(null, "write, read");
		
		assertFalse(readWrite.implies(permissionTwo));
		assertTrue(readWrite.implies(permissionThree));
		assertFalse(readWrite.implies(permissionFour));
	}
	
	@Test
	public void testAccessModeCombinationsForReadOnlyPermissions() {
		final PicoAccessPermission readWrite = new  PicoAccessPermission(null, "read");
		
		final PicoAccessPermission permissionTwo = new PicoAccessPermission(null, "read");
		final PicoAccessPermission permissionThree = new PicoAccessPermission(null, "write");
		final PicoAccessPermission permissionFour = new PicoAccessPermission(null, "write, read");
		
		assertTrue(readWrite.implies(permissionTwo));
		assertFalse(readWrite.implies(permissionThree));
		assertFalse(readWrite.implies(permissionFour));
	}
	
}
