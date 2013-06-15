package org.picocontainer.injectors.packageseparatetests;

import javax.inject.Inject;

import org.picocontainer.injectors.PackageSeparateBaseTestModel;

public class PackageSeparateDerivedTestModel extends PackageSeparateBaseTestModel {

	static String aValue;

	@Inject
	public void injectSomething() {

	}

	@Inject
	static void testSomethingElse() {

	}
}
