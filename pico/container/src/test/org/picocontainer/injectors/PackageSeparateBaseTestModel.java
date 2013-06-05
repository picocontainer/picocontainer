package org.picocontainer.injectors;

import javax.inject.Inject;

public class PackageSeparateBaseTestModel {

	static String aValue;
	
	
	@Inject
	private void testSomething() {
		
	}
	
	@Inject
	static void testSomethingElse() {
		
	}

}
