package com.picocontaienr.web.samples;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.picocontainer.web.test.SeleniumResource;

import com.thoughtworks.selenium.Selenium;

@RunWith( Suite.class)
@Suite.SuiteClasses ({
	ListAddCheese.class
})
public class SeleniumTestSuite {

	@ClassRule
	public static SeleniumResource seleniumResource = new SeleniumResource();
	
	
	public static Selenium selenium() {
		return seleniumResource.getSelenium();
	}

}
