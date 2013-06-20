package org.picocontaienr.web.samples;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.thoughtworks.selenium.Selenium;

public class ListAddCheese {


	@Test
	public void smokeTestForPromptPage() throws InterruptedException {
		Selenium selenium = SeleniumTestSuite.selenium();
		selenium.open("/jsf-webapp/cheese.jsf");
		for (int second = 0;; second++) {
			if (second >= 15) fail("timeout");
			try { if (selenium.isTextPresent("Add a Cheese")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}

		assertTrue(selenium.isTextPresent("Cheeses of The World Sample"));
		assertTrue(selenium.isTextPresent("Brie"));
		assertTrue(selenium.isTextPresent("France"));
		assertTrue(selenium.isTextPresent("Add a Cheese"));

	}
	
	
}
