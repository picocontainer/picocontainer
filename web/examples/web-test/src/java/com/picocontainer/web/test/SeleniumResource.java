package com.picocontainer.web.test;


import org.junit.rules.ExternalResource;
import org.openqa.selenium.server.SeleniumServer;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

public class SeleniumResource extends ExternalResource {

	public Selenium selenium;
	
	public SeleniumServer seleniumServer;
	

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
    
	public Selenium getSelenium() {
		if (selenium == null) {
			throw new IllegalStateException("Selenium Test Suite External Resources have not been invoked");
		}
		return selenium;
	}
	
}