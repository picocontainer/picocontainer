package com.picocontainer.web.sample.struts.selenium;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.thoughtworks.selenium.Selenium;

public class ListAddCheese {

	@Test
	public void smokeTestForPromptPage() throws InterruptedException {
		Selenium selenium = SeleniumTestSuite.selenium();
		selenium.open("/struts2-webapp/cheeses.action");
		assertTrue(selenium.isTextPresent("Cheese!"));
		assertTrue(selenium.isElementPresent("css=input[type=\"submit\"]"));
		assertTrue(selenium.isTextPresent("Brie"));
		assertTrue(selenium.isTextPresent("France"));
	}
}
