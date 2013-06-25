package com.picocontainer.web.sample.struts2.pwr.selenium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.thoughtworks.selenium.Selenium;

public class ListAddCheese {

	@Test
	public void smokeTestForPromptPage() throws InterruptedException {
		Selenium selenium = SeleniumTestSuite.selenium();
		selenium.open("/struts2-webapp-with-remoting/cheeses.action");
		assertTrue(selenium.isTextPresent("Brie"));
		assertTrue(selenium.isTextPresent("France"));
		assertTrue(selenium.isElementPresent("css=input[type=\"submit\"]"));
		
		
		assertTrue(selenium.isElementPresent("name=count"));
		selenium.waitForCondition("selenium.browserbot.getCurrentWindow().document.countForm.count.value == '4'", "10000");
		assertEquals("4", selenium.getValue("name=count"));
	}
	
	

}
