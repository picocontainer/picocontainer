package com.picocontainer.injectors.packageseparatetests;

import javax.inject.Inject;

import com.picocontainer.injectors.InjectableMethodSelectorTestCase.PackagePrivateBase1;

public class PackagePrivateDerivedTest1 extends PackagePrivateBase1 {

	public PackagePrivateDerivedTest1() {
	}


	@Inject
	void doSomething() {

	}
}
