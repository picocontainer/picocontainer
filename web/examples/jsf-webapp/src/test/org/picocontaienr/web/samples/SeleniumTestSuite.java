package org.picocontaienr.web.samples;

import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openqa.selenium.server.SeleniumServer;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

@RunWith( Suite.class)
@Suite.SuiteClasses ({
	ListAddCheese.class
})
public class SeleniumTestSuite {

	public static Selenium selenium;
	
	public static SeleniumServer seleniumServer;
	
	@ClassRule
	public static ExternalResource seleniumResource = new ExternalResource() {

		@Override
		protected void before() throws Throwable {
			
			seleniumServer = new SeleniumServer();
			seleniumServer.start();
			
			
			String browser = System.getProperty("selenium.browser", "*googlechrome");
			if ("${selenium.browser}".equals(browser)) {
				browser = "*googlechrome";
			} 
	        
			selenium = new DefaultSelenium("localhost", 4444, browser, "http://localhost:8080");
			selenium.start();
		}

		@Override
		protected void after() {
			try {
				selenium.close();
			} finally {
				seleniumServer.stop();
			}
		}

	};	
	
	public static Selenium selenium() {
		
		if (selenium == null) {
			throw new IllegalStateException("Selenium Test Suite External Resources have not been invoked");
		}
		
		return selenium;
	}

}
